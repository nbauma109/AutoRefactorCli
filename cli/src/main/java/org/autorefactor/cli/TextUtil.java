package org.autorefactor.cli;

import java.text.BreakIterator;
import java.util.Locale;

/** Brute force formatting util. May fail on windows. */
public final class TextUtil {
    private TextUtil() { }

    static String formatLines(String target, int maxLength) {
        StringBuilder sb = new StringBuilder();
        for (String s : target.split("\n")) {
            iformatLines(sb, s, maxLength);
            sb.append("\n");
        }
        return sb.toString();
    }

    private static void iformatLines(StringBuilder sb, String target, int maxLength) {
        Locale currentLocale = Locale.ENGLISH;

        BreakIterator boundary = BreakIterator.getLineInstance(currentLocale);
        boundary.setText(target);
        int start = boundary.first();
        int end = boundary.next();
        int lineLength = 0;

        while (end != BreakIterator.DONE) {
            String word = target.substring(start, end);
            lineLength = lineLength + word.length();
            if (lineLength >= maxLength) {
                sb.append("\n");
                lineLength = word.length();
            }
            sb.append(word);
            start = end;
            end = boundary.next();
        }
    }
}
