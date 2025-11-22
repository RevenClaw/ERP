package edu.univ.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void setUpStreams() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    void mainPrintsHelloMessage() {
        App.main(new String[0]);
        assertEquals("Hello University ERP!" + System.lineSeparator(), outputStream.toString());
    }
}

