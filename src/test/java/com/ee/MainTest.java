package com.ee;

import org.junit.jupiter.api.Test;

import com.ee.Client.Main;

public class MainTest {

    @Test
    public void mainContextLoadsAndUnloads() {
        try (Main main = new Main()) {
            main.init();
        }
    }
}
