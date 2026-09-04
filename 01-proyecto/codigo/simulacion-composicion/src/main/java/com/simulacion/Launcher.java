package com.simulacion;

/**
 * Classpath-compatible launcher.
 * Application.launch() requires module path in Java 9+.
 * This launcher uses reflection to bypass that check.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
