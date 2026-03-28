package com.ee;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import com.ee.Client.Main;

public class MainTest {

    @Test
    public void mainContextLoadsAndUnloads() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream sink = new ByteArrayOutputStream();

        try (PrintStream mutedStream = new PrintStream(sink);
                Main main = new Main()) {
            System.setOut(mutedStream);
            System.setErr(mutedStream);

            assertDoesNotThrow(main::init);
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
}
