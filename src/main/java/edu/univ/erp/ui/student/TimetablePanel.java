package edu.univ.erp.ui.student;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import edu.univ.erp.api.student.StudentApi;
import edu.univ.erp.config.ApplicationContext;
import edu.univ.erp.data.CourseRepository;
import edu.univ.erp.data.InstructorRepository;
import edu.univ.erp.data.StudentRepository;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Term;
import edu.univ.erp.domain.Weekday;
import edu.univ.erp.ui.common.ButtonStyler;

/**
 * Panel for displaying student timetable in a calendar-style layout.
 */
public class TimetablePanel extends JPanel {

    private static final Weekday[] DISPLAY_DAYS = {
            Weekday.MON, Weekday.TUE, Weekday.WED, Weekday.THU, Weekday.FRI, Weekday.SAT
    };
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm a");

    private final long userId;
    private final StudentApi studentApi;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final JPanel calendarPanel;
    private JComboBox<Term> termCombo;
    private JComboBox<Integer> yearCombo;

    public TimetablePanel(ApplicationContext context, long userId) {
        this.userId = userId;
        this.studentApi = context.studentApi();
        this.studentRepository = context.repositoryFactory().studentRepository();
        this.courseRepository = context.repositoryFactory().courseRepository();
        this.instructorRepository = context.repositoryFactory().instructorRepository();
        this.calendarPanel = new JPanel();
        this.calendarPanel.setOpaque(true);
        this.calendarPanel.setLayout(new java.awt.GridLayout(1, DISPLAY_DAYS.length, 16, 0));
        this.calendarPanel.setBackground(new Color(240, 244, 248));

        initializeUI();
        loadTimetable(Term.MONSOON, 2025);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        topPanel.add(new JLabel("Term:"));
        termCombo = new JComboBox<>(Term.values());
        termCombo.setSelectedItem(Term.MONSOON);
        termCombo.addActionListener(e -> loadTimetable());
        topPanel.add(termCombo);

        topPanel.add(new JLabel("Year:"));
        yearCombo = new JComboBox<>(new Integer[]{2024, 2025, 2026});
        yearCombo.setSelectedItem(2025);
        yearCombo.addActionListener(e -> loadTimetable());
        topPanel.add(yearCombo);

        JButton refreshButton = new JButton("Refresh");
        ButtonStyler.stylePrimary(refreshButton);
        refreshButton.addActionListener(e -> loadTimetable());
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(calendarPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadTimetable() {
        Term term = (Term) termCombo.getSelectedItem();
        Integer year = (Integer) yearCombo.getSelectedItem();
        if (term != null && year != null) {
            loadTimetable(term, year);
        }
    }

    private void loadTimetable(Term term, int year) {
        try {
            studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Student not found"));
            List<Section> sections = studentApi.getTimetable(userId, term, year);
            List<ScheduleItem> scheduleItems = sections.stream()
                    .map(this::toScheduleItem)
                    .collect(Collectors.toCollection(ArrayList::new));
            updateCalendar(scheduleItems);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading timetable: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private ScheduleItem toScheduleItem(Section section) {
        Course course = courseRepository.findById(section.courseId()).orElse(null);
        String courseName = course != null ? course.title() : "Unknown Course";

        Instructor instructor = instructorRepository.findById(section.instructorId()).orElse(null);
        String instructorName = instructor != null
                ? instructor.title() + " " + instructor.department()
                : "TBA";

        return new ScheduleItem(
                courseName,
                section.sectionCode(),
                section.room(),
                instructorName,
                section.dayOfWeek(),
                section.startTime(),
                section.endTime()
        );
    }

    private void updateCalendar(List<ScheduleItem> scheduleItems) {
        calendarPanel.removeAll();
        calendarPanel.setLayout(new java.awt.GridLayout(1, DISPLAY_DAYS.length, 16, 0));

        Map<Weekday, List<ScheduleItem>> grouped = new EnumMap<>(Weekday.class);
        for (Weekday day : DISPLAY_DAYS) {
            grouped.put(day, new ArrayList<>());
        }
        for (ScheduleItem item : scheduleItems) {
            grouped.computeIfAbsent(item.day(), d -> new ArrayList<>()).add(item);
        }

        for (Weekday day : DISPLAY_DAYS) {
            List<ScheduleItem> dayItems = grouped.getOrDefault(day, List.of()).stream()
                    .sorted(Comparator.comparing(ScheduleItem::startTime))
                    .collect(Collectors.toList());
            calendarPanel.add(createDayColumn(day, dayItems));
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private JPanel createDayColumn(Weekday day, List<ScheduleItem> items) {
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(33, 78, 165));
        header.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel headerLabel = new JLabel(dayLabel(day));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(headerLabel.getFont().deriveFont(java.awt.Font.BOLD, 16f));
        header.add(headerLabel, BorderLayout.WEST);
        column.add(header);
        column.add(Box.createVerticalStrut(8));

        if (items.isEmpty()) {
            column.add(createEmptyState());
            return column;
        }

        for (ScheduleItem item : items) {
            column.add(createEventCard(item));
            column.add(Box.createVerticalStrut(10));
        }
        column.add(Box.createVerticalGlue());
        return column;
    }

    private JPanel createEventCard(ScheduleItem item) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(255, 255, 255));
        card.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.LineBorder(new Color(220, 226, 240), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel(item.course());
        title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD, 14f));

        JLabel time = new JLabel(formatTime(item.startTime()) + " - " + formatTime(item.endTime()));
        time.setForeground(new Color(90, 104, 133));

        JLabel sectionInfo = new JLabel("Section " + item.sectionCode() + " • " + item.room());
        sectionInfo.setForeground(new Color(120, 132, 158));

        JLabel instructor = new JLabel(item.instructor());
        instructor.setForeground(new Color(120, 132, 158));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(title);
        content.add(Box.createVerticalStrut(4));
        content.add(time);
        content.add(sectionInfo);
        content.add(instructor);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createEmptyState() {
        JPanel empty = new JPanel();
        empty.setBorder(new EmptyBorder(20, 12, 20, 12));
        empty.setBackground(new Color(246, 248, 252));
        JLabel label = new JLabel("No classes scheduled");
        label.setForeground(new Color(120, 132, 158));
        empty.add(label);
        return empty;
    }

    private String dayLabel(Weekday day) {
        return switch (day) {
            case MON -> "Monday";
            case TUE -> "Tuesday";
            case WED -> "Wednesday";
            case THU -> "Thursday";
            case FRI -> "Friday";
            case SAT -> "Saturday";
        };
    }

    private record ScheduleItem(
            String course,
            String sectionCode,
            String room,
            String instructor,
            Weekday day,
            java.time.LocalTime startTime,
            java.time.LocalTime endTime
    ) {
    }

    private String formatTime(java.time.LocalTime time) {
        return time.format(TIME_FORMATTER);
    }
}

