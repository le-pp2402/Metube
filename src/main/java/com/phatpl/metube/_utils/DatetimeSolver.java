package com.phatpl.metube._utils;

import java.util.Date;
import java.util.List;

public class DatetimeSolver {
    public static String findTimeAgo(Date datetimeStart, Date datetimeEnd) {
        long total = datetimeEnd.getTime() - datetimeStart.getTime();
        if (total < 60) {
            return "Just now";
        } else if (total < 3600) {
            return total / 60 + " minutes ago";
        } else if (total < 86400) {
            return total / 3600 + " hours ago";
        } else if (total < 604800) {
            return total / 86400 + " days ago";
        } else if (total < 2592000) {
            return total / 604800 + " weeks ago";
        } else if (total < 31536000) {
            return total / 2592000 + " months ago";
        } else {
            return total / 31536000 + " years ago";
        }
    }
}
