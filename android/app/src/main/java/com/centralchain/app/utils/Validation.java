package com.centralchain.app.utils;

// checks for form input
public class Validation {

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    // at least 6 characters, same as the backend
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // whole number bigger than 0
    public static boolean isPositiveNumber(String text) {
        try {
            return Integer.parseInt(text.trim()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // whole number 0 or more
    public static boolean isZeroOrMore(String text) {
        try {
            return Integer.parseInt(text.trim()) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidPrice(String text) {
        try {
            return Double.parseDouble(text.trim()) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // only jpg, png or webp
    public static boolean isValidPhoto(String fileName) {
        if (fileName == null) return false;
        String name = fileName.toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp");
    }
}
