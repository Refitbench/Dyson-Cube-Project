package com.refitbench.dysoncubeproject.util;

import java.text.DecimalFormat;

public class NumberUtils {

    private static final String[] SUFFIXES = {"", "K", "M", "B", "T", "Q", "Qi", "Sx", "Sp", "O"};

    public static String getFormattedBigNumber(double value) {
        if (value < 1000) {
            return String.valueOf((int) Math.ceil(value));
        }

        int exp = (int) (Math.log(value) / Math.log(1000));
        if (exp >= SUFFIXES.length) {
            return "Err";
        }

        var decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(value / Math.pow(1000, exp)) + SUFFIXES[exp];
    }

    public static double customCeil(double value) {
        if (value == (long) value) {
            return value;
        }
        return (value > 0) ? (long) value + 1 : (long) value;
    }
}
