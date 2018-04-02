package org.autorefactor.cli;

import java.util.Arrays;

public enum SourceLevel {
    None(""), Java14("1.4"), Java5("1.5"), Java6("1.6"), Java7("1.7"), Java8("1.8"), Java9("1.9"), Max("1.100");

    private final String v;

    SourceLevel(String v) {
        this.v = v;
    }

    static SourceLevel fromValue(String v) {
        for (SourceLevel sl : values()) {
            if (v.equals(sl.v)) {
                return sl;
            }
        }
        throw new IllegalArgumentException("could not find '" + v + "' in " + Arrays.asList(values()));
    }
}
