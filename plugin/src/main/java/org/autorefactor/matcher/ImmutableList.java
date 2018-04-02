package org.autorefactor.matcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple partial implementation of imuutable list.
 *
 * TODO: use guava?
 */
public class ImmutableList {

    public static <T> List<T> copyOf(List<T> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(list));
    }

}
