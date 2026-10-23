package com.centralchain.app.utils;

import com.centralchain.app.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// colours and text for statuses and dates
public class StatusStyle {

    // colour for each order or delivery status
    public static int getColor(String status) {
        if (status == null) return R.color.muted;
        switch (status) {
            case "PENDING":
            case "ASSIGNED":
                return R.color.warning;
            case "APPROVED":
            case "PROCESSING":
                return R.color.primary;
            case "DISPATCHED":
            case "IN_TRANSIT":
                return R.color.info;
            case "DELIVERED":
                return R.color.success;
            case "CANCELLED":
            case "FAILED":
                return R.color.danger;
            default:
                return R.color.muted;
        }
    }

    // makes the status easy to read
    public static String getLabel(String status) {
        if (status == null || status.isEmpty()) return "";
        String text = status.replace("_", " ").toLowerCase();
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    // makes the date easy to read
    public static String formatDate(String date) {
        if (date == null) return "-";
        try {
            return LocalDateTime.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm"));
        } catch (Exception e) {
            return date;
        }
    }
}
