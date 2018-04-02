package org.autorefactor.cli.dd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Delta Debugging - reducing test cases with delta debugging.
 *
 * Practical Improvements to the Minimizing Delta Debugging Algorithm
 * http://www.inf.u-szeged.hu/~akiss/pub/pdf/hodovan2016pardd.pdf
 *
 * Additional Improvements:
 * - if any reduction was possible in a run (aka "non empty run") a complete run is done again
 *   1/3 reduction on java case
 * - the algorithm has a bias on binary sizes of deltas and java examples had multiple 3 line examples
 * <pre>
            public boolean visit(Dimension node) {
                return true;
        }
        </pre>
   So 3 extra rounds with fixed length 3 and offsets 0,1,2 are tried.

 *
 * @author cal
 */
public class DDMin {
    private static final Character NEWLINE = Character.valueOf('\n');
    // 8 means in character mode "protected" is included
    private static final int MAX_FIXED_PASS_SIZE = 8;

    /** test result */
    public enum Result {
        NotReproduced,
        Unknown,
        Reproduced
    }
    public interface Predicate<T> {
        Result apply(List<T> a);
    }

    public static <T> List<T> ddMin(List<T> originalTarget, int n, Predicate<T> test) {
        List<T> target = new LinkedList<T>(originalTarget);
        boolean emptyRun = true;
        boolean fixedPass = false;
        int fixedPassSize = 2;
        int fixedPassOffset = -1;
        dd: while(n <= target.size()) {
            {
                final int minDeltaSize = target.size() / n;
                final int maxDeltaSize = (target.size() + n - 1)/ n;
                System.out.printf(
                        "DDMin: target size: %4d, n: %4d, delta size: %4d%s"
                                + (fixedPass && fixedPassOffset >= 0 ? ", offset: " + fixedPassOffset : "") + "%n",
                                target.size(),
                                n,
                                minDeltaSize,
                                !fixedPass && minDeltaSize != maxDeltaSize ? "+" : " "
                                );
            }
            if (!fixedPass) {
                /*
                 * unlikely for java, faster without
                // reduce to subset
                for (int i = 0; i < n; i++) {
                    //System.out.println("code=" + code + ",n=" + n + ", i=" + i);
                    List<String> delta = delta(code, i, n);
                    //System.out.println("delta=" + delta);
                    if (test.apply(delta) == Result.Reproduced) {
                        code = delta;
                        n = 2;
                        continue dd;
                    }
                }
                */
                // reduce to complement
                for (int i = 0; i < n; i++) {
                    List<T> complement = complement(target, i, n);
                    // TODO: size check is hack because complement may have not removed single new-line
                    if (complement.size() != target.size() && test.apply(complement) == Result.Reproduced) {
                        target = complement;
                        emptyRun = false;
                        n = Math.max(n - 1, 2);
                        continue dd;
                    }
                }
                // increase granularity
                if (n < target.size()) {
                    n = Math.min(target.size(), 2 * n);
                    continue dd;
                }
            } else {
                // reduce to complement
                // try complement of delta with size fixedPassSize at different offsets
                for (int i = 0; i < n; i++) {
                    final int start = fixedPassOffset + i * fixedPassSize;
                    List<T> complement = complementAt(target, start, start + fixedPassSize);
                    // TODO: size check is hack because complement may have not removed single new-line
                    if (complement.size() != target.size() && test.apply(complement) == Result.Reproduced) {
                        target = complement;
                        emptyRun = false;
                        n = Math.max(target.size() / fixedPassSize, 1);
                        continue dd;
                    }
                }
            }
            // if at least one reduction was done try a complete new run
            if (!emptyRun) {
                n = 2;
                emptyRun = true;
                fixedPass = false;
                fixedPassSize = 2;
                fixedPassOffset = -1;
                continue dd;
            }
            // try to remove deltas of size 2..4 in extra run with offsets
            // 0,<size-1>
            if (target.size() > fixedPassSize) {
                fixedPass = true;
                // e.g. for size "3" try offsets 0,1,2, inital value is -1
                if (fixedPassOffset < fixedPassSize - 1) {
                    fixedPassOffset++;
                    n = Math.max(target.size() / fixedPassSize, 1);
                    continue dd;
                } else if (fixedPassSize < MAX_FIXED_PASS_SIZE) {
                    fixedPassSize++;
                    fixedPassOffset = 0;
                    n = Math.max(target.size() / fixedPassSize, 1);
                    continue dd;
                }
            }

            break dd;
        }

        return target;
    }

    // spreads remainder of list.size() % n over range
    private static <T> List<T> complement(List<T> list, int i, int n) {
        int start = list.size() * i / n ;
        int end = Math.min(list.size() * (i + 1) / n, list.size());
        return deleteRange(list, start, end);
    }

    // uses exact position at start
    private static <T> List<T> complementAt(List<T> list, int start, int rawEnd) {
        int end = Math.min(rawEnd, list.size());
        //System.out.println("trying: delta " + list.subList(start,  end));
        return deleteRange(list, start, end);
    }

    private static <T> List<T> deleteRange(List<T> list, int start, int end) {
        List<T> l = new LinkedList<T>(list);
        if (list.get(0) instanceof Character) {
            // hack: do not remove new lines in character mode. should be coordinated no higher level
            Iterator<T> it = l.subList(start, end).iterator();
            while (it.hasNext()) {
                if (!NEWLINE.equals(it.next())) {
                    it.remove();
                }
            }
        } else {
            int num = end - start;
            while (num-- > 0) {
                l.remove(start);
            }
        }
        return l;
    }

    private static <T> List<T> delta(List<T> list, int i, int n) {
        int start = list.size() * i / n ;
        int end = Math.min(list.size() * (i + 1) / n, list.size());
        return list.subList(start, end);
    }
}
