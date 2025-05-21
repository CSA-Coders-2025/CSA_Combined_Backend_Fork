package com.nighthawk.spring_portfolio.mvc.crypto;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");

    public static String now() {
        return "[" + formatter.format(new Date()) + "]";
    }
}
