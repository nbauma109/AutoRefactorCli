package org.autorefactor.cli.dd;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class DDMinTest {

    @Test
    public void testDDMin() {
        String src = "{\na\nb\nc\nd\nx\ne\nf\n}\n";

        List<String> code = new ArrayList(Arrays.asList(src.split("\n")));
        List<String> result = DDMin.ddMin(code, 2, new DDMin.Predicate<String>() {
            public DDMin.Result apply(List<String> code) {
                return code.contains("x") ? DDMin.Result.Reproduced : DDMin.Result.NotReproduced;
            }
        });
        assertEquals("[x]", String.valueOf(result));

        code = new ArrayList(Arrays.asList(src.split("\n")));
        result = DDMin.ddMin(code, 2, new DDMin.Predicate<String>() {
            public DDMin.Result apply(List<String> code) {
                return code.contains("x") && code.contains("{") && code.contains("}") ? DDMin.Result.Reproduced : DDMin.Result.NotReproduced;
            }
        });
        assertEquals("[{, x, }]", String.valueOf(result));

        code = new ArrayList(Arrays.asList(src.split("\n")));
        result = DDMin.ddMin(code, 2, new DDMin.Predicate<String>() {
            public DDMin.Result apply(List<String> code) {
                return code.contains("{") && code.contains("}") ? DDMin.Result.Reproduced : DDMin.Result.NotReproduced;
            }
        });
        assertEquals("[{, }]", String.valueOf(result));
    }
}
