package edu.univ.erp.ui.student;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.opencsv.CSVWriter;

import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.data.AssessmentRepository;
import edu.univ.erp.data.CourseRepository;
import edu.univ.erp.data.EnrollmentRepository;
import edu.univ.erp.data.FinalGradeRepository;
import edu.univ.erp.data.GradeRepository;
import edu.univ.erp.data.SectionRepository;
import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.domain.Assessment;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.EnrollmentStatus;
import edu.univ.erp.domain.FinalGrade;
import edu.univ.erp.domain.GradeEntry;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Term;

/**
 * Panel for viewing grades and downloading transcript.
 */
public class GradesPanel extends JPanel {

    private final long userId;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final AssessmentRepository assessmentRepository;
    private final GradeRepository gradeRepository;
    private final FinalGradeRepository finalGradeRepository;
    private final JTable gradesTable;
    private final DefaultTableModel tableModel;
    private JComboBox<Term> termCombo;
    private JComboBox<Integer> yearCombo;

    public GradesPanel(ApplicationContext context, long userId) {
        this.userId = userId;
        this.studentRepository = context.repositoryFactory().studentRepository();
        this.enrollmentRepository = context.repositoryFactory().enrollmentRepository();
        this.sectionRepository = context.repositoryFactory().sectionRepository();
        this.courseRepository = context.repositoryFactory().courseRepository();
        this.assessmentRepository = context.repositoryFactory().assessmentRepository();
        this.gradeRepository = context.repositoryFactory().gradeRepository();
        this.finalGradeRepository = context.repositoryFactory().finalGradeRepository();
        
        this.tableModel = new DefaultTableModel(new String[]{
                "Course", "Section", "Component", "Score", "Final Grade"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.gradesTable = new JTable(tableModel);

        initializeUI();
        loadGrades(Term.MONSOON, 2025); // Default load
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Top panel: filters
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Term:"));
        termCombo = new JComboBox<>(Term.values());
        termCombo.setSelectedItem(Term.MONSOON);
        termCombo.addActionListener(e -> loadGrades());
        topPanel.add(termCombo);

        topPanel.add(new JLabel("Year:"));
        yearCombo = new JComboBox<>(new Integer[]{2024, 2025, 2026});
        yearCombo.setSelectedItem(2025);
        yearCombo.addActionListener(e -> loadGrades());
        topPanel.add(yearCombo);

        JButton loadButton = new JButton("Load Grades");
        loadButton.addActionListener(e -> loadGrades());
        topPanel.add(loadButton);

        add(topPanel, BorderLayout.NORTH);

        // Center: grades table
        gradesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom: action buttons
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton downloadTranscriptButton = new JButton("Download Transcript (CSV)");
        downloadTranscriptButton.addActionListener(e -> downloadTranscript());
        bottomPanel.add(downloadTranscriptButton);

        JButton downloadPdfButton = new JButton("Download Transcript (PDF)");
        downloadPdfButton.addActionListener(e -> downloadTranscriptPdf());
        bottomPanel.add(downloadPdfButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadGrades() {
        Term term = (Term) termCombo.getSelectedItem();
        Integer year = (Integer) yearCombo.getSelectedItem();
        if (term != null && year != null) {
            loadGrades(term, year);
        }
    }

    private void loadGrades(Term term, int year) {
        try {
            Student student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Student not found"));
            
            List<Enrollment> enrollments = enrollmentRepository.findByStudent(student.id());
            updateTable(enrollments, term, year);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading grades: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Enrollment> enrollments, Term term, int year) {
        tableModel.setRowCount(0);
        
        boolean isWinter = (term == Term.WINTER);
        
        for (Enrollment enrollment : enrollments) {
            if (enrollment.status() != EnrollmentStatus.ENROLLED) {
                continue;
            }
            
            Section section = sectionRepository.findById(enrollment.sectionId())
                    .orElse(null);
            if (section == null || section.term() != term || section.year() != year) {
                continue;
            }
            
            Course course = courseRepository.findById(section.courseId())
                    .orElse(null);
            String courseName = course != null ? course.title() : "Unknown Course";
            String sectionCode = section.sectionCode();
            
            if (isWinter) {
                // WINTER: Show actual grades
                List<Assessment> assessments = assessmentRepository.findBySection(section.id());
                List<GradeEntry> grades = gradeRepository.findByEnrollment(enrollment.id());
                Optional<FinalGrade> finalGrade = finalGradeRepository.findByEnrollment(enrollment.id());
                
                if (assessments.isEmpty() && !finalGrade.isPresent()) {
                    // No assessments or final grade, show course with TBD
                    tableModel.addRow(new Object[]{
                            courseName,
                            sectionCode,
                            "N/A",
                            "TBD",
                            "TBD"
                    });
                } else {
                    // Show assessment scores
                    for (Assessment assessment : assessments) {
                        GradeEntry grade = grades.stream()
                                .filter(g -> g.assessmentId() == assessment.id())
                                .findFirst()
                                .orElse(null);
                        
                        String score = grade != null 
                                ? String.format("%.2f / %.2f", grade.score(), assessment.maxScore())
                                : "TBD";
                        
                        tableModel.addRow(new Object[]{
                                courseName,
                                sectionCode,
                                assessment.name(),
                                score,
                                "" // Final grade shown separately
                        });
                    }
                    
                    // Show final grade
                    String finalGradeStr = finalGrade.map(fg -> 
                            String.format("%.2f (%s)", fg.finalScore(), fg.letterGrade()))
                            .orElse("TBD");
                    
                    tableModel.addRow(new Object[]{
                            courseName,
                            sectionCode,
                            "Final Grade",
                            "",
                            finalGradeStr
                    });
                }
            } else {
                // MONSOON: Show only course details with TBD
                tableModel.addRow(new Object[]{
                        courseName,
                        sectionCode,
                        "N/A",
                        "TBD",
                        "TBD"
                });
            }
        }
    }

    private void downloadTranscript() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript");
        fileChooser.setSelectedFile(new java.io.File("transcript.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File fileToSave = fileChooser.getSelectedFile();
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileToSave))) {
            // Write header
            writer.writeNext(new String[]{"Term", "Year", "Course Code", "Course Title", "Final Score", "Letter Grade"});

            // Write data
            Student student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Student not found"));
            List<Enrollment> allEnrollments = enrollmentRepository.findByStudent(student.id());

            for (Enrollment en : allEnrollments) {
                if (en.status() != EnrollmentStatus.COMPLETED) {
                    continue; // Skip non-completed courses for transcript
                }
                
                Section sec = sectionRepository.findById(en.sectionId()).orElse(null);
                Course crs = (sec != null) ? courseRepository.findById(sec.courseId()).orElse(null) : null;
                Optional<FinalGrade> fg = finalGradeRepository.findByEnrollment(en.id());

                if (sec != null && crs != null && fg.isPresent()) {
                    writer.writeNext(new String[]{
                            sec.term().name(),
                            String.valueOf(sec.year()),
                            crs.code(),
                            crs.title(),
                            String.format("%.2f", fg.get().finalScore()),
                            fg.get().letterGrade()
                    });
                }
            }
            JOptionPane.showMessageDialog(this, "Transcript downloaded successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing file: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void downloadTranscriptPdf() {
        JOptionPane.showMessageDialog(this, "Transcript export (PDF) - Coming soon!",
                "Feature Not Available", JOptionPane.INFORMATION_MESSAGE);
    }
}
