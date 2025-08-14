package com.scg.scgcalls.util;

public class General {

    public static String formatDuration(String secondsStr) {
        try {
            int totalSeconds = Integer.parseInt(secondsStr);
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;

            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } catch (NumberFormatException e) {
            return "00:00:00"; // fallback if input is invalid
        }
    }
}
