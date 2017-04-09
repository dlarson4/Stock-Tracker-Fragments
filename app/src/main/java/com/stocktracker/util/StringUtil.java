package com.stocktracker.util;

/**
 * Created by dlarson on 4/8/17.
 */
public class StringUtil {
    private StringUtil() {
    }

    public static boolean isBlank(String input) {
        int strLen;
        if (input == null || (strLen = input.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(input.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean notBlank(String input) {
        return !isBlank(input);
    }
}
