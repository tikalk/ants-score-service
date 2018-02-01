package com.tikal.fuze.antscosmashing.scoreservice.controller;

public class StringUtils {

    /**
     * Removes the double quote from the start and end of the supplied string if
     * it starts and ends with this character. This method does not create a new
     * string if <tt>text</tt> doesn't start and end with double quotes, the
     * <tt>text</tt> object itself is returned in that case.
     *
     * @param text
     *        The string to remove the double quotes from.
     * @return The trimmed string, or a reference to <tt>text</tt> if it did
     *         not start and end with double quotes.
     */
    public static String trimDoubleQuotes(String text) {
        int textLength = text.length();

        if (textLength >= 2 && text.charAt(0) == '"' && text.charAt(textLength - 1) == '"') {
            return text.substring(1, textLength - 1);
        }

        return text;
    }
}
