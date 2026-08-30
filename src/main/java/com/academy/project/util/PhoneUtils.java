package com.academy.project.util;

public final class PhoneUtils {

    private PhoneUtils() {
    }

    /** Strips formatting and removes leading India country code when present. */
    public static String normalize(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("91") && digits.length() == 12) {
            return digits.substring(2);
        }
        return digits;
    }

    /** Formats phone for SMS gateways (India default: 91 + 10 digits). */
    public static String toSmsNumber(String phone, String defaultCountryCode) {
        String normalized = normalize(phone);
        if (normalized.isBlank()) {
            return "";
        }
        if (normalized.length() == 10) {
            return defaultCountryCode + normalized;
        }
        return normalized;
    }
}
