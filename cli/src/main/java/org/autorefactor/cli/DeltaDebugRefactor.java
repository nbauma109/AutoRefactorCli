package org.autorefactor.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.autorefactor.cli.dd.DDMin;
import org.autorefactor.cli.dd.DDMin.Result;
import org.autorefactor.refactoring.RefactoringRule;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Refactor with delta debugging enabled that
 * reduces test input on failed refactorings.
 * 
 * @author cal
 */
@SuppressWarnings("restriction")
public class DeltaDebugRefactor {

    public interface TargetTest {
		Result apply(String code);
	}

    void deltaDebugRefactorFile(final Refactor.RefactorTarget target, final List<RefactoringRule> originalRules, final EffApplyArgs args) throws CoreException, Exception {
    	final IResource resource = target.getResource();
        final String originalCode = FileUtil.read(resource);
        DDMin.Result testResult = testRefactorFile(originalCode, target, originalRules, args);
        if (testResult != DDMin.Result.Reproduced) {
            throw new IllegalStateException("cannot start: test case not reproducable");
        }
        final File file = resource.getLocation().toFile();
		try {
            // 1. reduce rules
			final List<RefactoringRule> rules = reduceRules(target, originalCode, originalRules, args);
			if (originalRules.size() > 1) {
				if (rules.size() < originalRules.size()) {
					System.out.println("reduced rule size from " + originalRules.size() + " to " + rules.size());
					System.out.println("applying rules: " + rules);
				} else {
					System.out.println("could not reduce reduced rule size " + originalRules.size());
				}
			}
	        final TargetTest test = new TargetTest() {
				@Override
				public Result apply(String code) {
					return testRefactorFile(code, target, rules, args);
	            }
	        };

            String code = originalCode;
			code = reduceWhitespace(code, test);
			code = splitLines(code, test);

			String previousCode = "";
            while(!code.equals(previousCode)) {
            	previousCode = code;
            	boolean loop;
            	// 2. lines + expressions
            	do {
            		// 2.1. reduce lines
            		code = reduceLines(code, target, test);
            		// 2.2. misc
            		String s = code;
            		code = tryReplacements(code, target, test);
            		loop = !s.equals(code);
            	} while (loop);
                // 3. reduce characters
                code = reduceCharacters(code, target, test);
                if (code.equals(previousCode)) {
        			code = reduceWhitespace(code, test);
        			code = splitLines(code, test);
                    code = tryReplacements(code, target, test);
                }
            }
            FileUtil.writeFile(new File(file.getAbsolutePath() + "-ddmin"), code);
            // TODO: hcak: this currently leaves refactored code in fs where we catch it
            test.apply(code);
            FileUtil.writeFile(new File(file.getAbsolutePath() + "-ddmin-result"), FileUtil.readFileToString(file.getAbsolutePath()));
        } finally {
            FileUtil.writeFile(file, originalCode);
        }
    }

	private List<RefactoringRule> reduceRules(final Refactor.RefactorTarget target, final String originalCode,
			final List<RefactoringRule> originalRules, final EffApplyArgs args) {
		final List<RefactoringRule> rules;
		if (originalRules.size() <= 1) {
		    rules = originalRules;
		} else {
		    rules = DDMin.ddMin(originalRules, 2, new DDMin.Predicate<RefactoringRule>() {
		        @Override
		        public Result apply(List<RefactoringRule> newRules) {
		            return testRefactorFile(originalCode, target, newRules, args);
		        }
		    });
		}
		return rules;
	}

    // TODO: apply ddmax when applying "all" fails.
	private String reduceWhitespace(String code, TargetTest test) {
		{
			final String newCode = removeLeadingWhitespace(code);
			if (!code.equals(newCode) && test.apply(newCode) == Result.Reproduced) {
				code = newCode;
			}
		}
		{
			final String newCode = removeTrailingWhitespace(code);
			if (!code.equals(newCode) && test.apply(newCode) == Result.Reproduced) {
				code = newCode;
			}
		}
		{
			final String newCode = collapseWhitespace(code);
			if (!code.equals(newCode) && test.apply(newCode) == Result.Reproduced) {
				code = newCode;
			}
		}
		return code;
	}

	public static String removeLeadingWhitespace(String code) {
		return code.replaceAll("(?m)^[ \t]+", "");
	}

	public static String removeTrailingWhitespace(String code) {
		return code.replaceAll("(?m)[ \t]+$", "");
	}

	public static String collapseWhitespace(String code) {
		return code.replaceAll("[ \t]+", " ");
	}

    // TODO: apply ddmax when applying "all" fails.
	public static String splitLines(String code, TargetTest test) {
		{
			// split before "{" and after "}" and after ")" if that is followed by a character (e.g. @asdf()hello)
			final String newCode = code.replaceAll("(?m)\\{$", "\n{")
					.replaceAll("\\}", "}\n")
					.replaceAll("\\)(\\s*[A-Za-z_])", ")\n$1")
					.replaceAll("\\n\\n+", "\n");
			if (!code.equals(newCode) && test.apply(newCode) == Result.Reproduced) {
				code = newCode;
			}
		}
		return code;
	}

	private String reduceLines(String code, final Refactor.RefactorTarget target, final TargetTest test) {
		final List<String> reducedLines = DDMin.ddMin(split(code), 2, new DDMin.Predicate<String>() {
		    @Override
		    public Result apply(List<String> a) {
		        final String newCode = join(a);
		        /*
		         * writeFile(new
		         * File(resource.getLocation().toFile().getAbsolutePath(
		         * )), newCode); refreshResource(resource);
		         */
		        // setContent((IFile)resource, newCode);
		        Result res = test.apply(newCode);
		        if (res == Result.Reproduced) {
		            FileUtil.writeFile(new File(target.getResource().getLocation().toFile().getAbsolutePath() + "-ddmin-latest"),
		                    newCode);
		        }
		        return res;
		    }
		});
		return join(reducedLines);
	}

	private String reduceCharacters(final String code, final Refactor.RefactorTarget target, final TargetTest test) {
		final List<Character> reducedChars = DDMin.ddMin(splitChars(code), 2,
		        new DDMin.Predicate<Character>() {
		            @Override
		            public Result apply(List<Character> a) {
		                final String newCode = joinChars(a);
		                Result res = test.apply(newCode);
		                if (res == Result.Reproduced) {
		                    FileUtil.writeFile(new File(
		                            target.getResource().getLocation().toFile().getAbsolutePath() + "-ddmin-latest"),
		                            newCode);
		                }
		                return res;
		            }
		        });
		return joinChars(reducedChars);
	}

	private static class Replace {
		final int start;
		final int end;
		public Replace(int start, int end) {
			this.start = start;
			this.end = end;
		}
		@Override
		public String toString() {
			return "Replace [start=" + start + ", end=" + end + "]";
		}
	}
	// can be optimized to not match "return ;"
	private static Pattern returns = Pattern.compile("(?!<[a-z\"])return[^a-z;][^;]+;");

	// or comparison
	private static Pattern assignment = Pattern.compile("=[^;]+;");

	private static Pattern mayBeGenericParam = Pattern.compile("<[a-zA-Z_0-9?]+>");

	// ascii only ...
	private static Pattern typeName = Pattern.compile("(?<![a-z\"])[A-Z][a-zA-Z_0-9]*");

	// var names longer than 2 characters
	// Note: matches more .... does not match function names (followed by "(")
	private static Pattern name = Pattern.compile("(?<![A-Za-z0-9_\"])(?!\\s*\\()[a-z_][a-zA-Z_0-9]{2,}");

	// null, true, false, ...
	private static Pattern ignore = Pattern.compile("(if|return|while|do|true|false|null)");

    // TODO: needs ddmax!
	// TODO: may introduce loops!
	public static String tryReplacements(final String code, final Refactor.RefactorTarget target, TargetTest test) {
		String replaced = code;
		replaced = tryReplace(replaced, returns, "return null;", target, test);
		replaced = tryReplace(replaced, returns, "return true;", target, test);
		replaced = tryReplace(replaced, returns, "return 1;", target, test);
		replaced = tryReplace(replaced, assignment, "=null;", target, test);
		replaced = tryReplace(replaced, assignment, "=true;", target, test);
		replaced = tryReplace(replaced, assignment, "=1;", target, test);
		replaced = tryReplace(replaced, mayBeGenericParam, " ", target, test);
		replaced = tryReplace(replaced, typeName, "Object", target, test);
		replaced = tryReplace(replaced, name, "q", target, test);
		return replaced;
	}

	private static String tryReplace(final String code, Pattern pattern, final String replacement, final Refactor.RefactorTarget target,
			final TargetTest test) {
		final List<Replace> edits = new ArrayList<Replace>();
		Matcher m = pattern.matcher(code);
		while (m.find()) {
			if (!ignore.matcher(m.group()).matches()) {
				edits.add(new Replace(m.start(), m.end()));
			}
		}
		//System.out.println("edits=" + edits);
		if (!edits.isEmpty()) {
			// TODO: we want max here!
			final List<Replace> min = DDMin.ddMin(edits, 1, new DDMin.Predicate<Replace>() {
				@Override
				public Result apply(List<Replace> a) {
					// TODO: direct max would be more efficient
					final String newCode = DeltaDebugRefactor.apply(code, minus(edits, a), replacement);
					Result res = test.apply(newCode);
					if (res == Result.Reproduced && target != null) {
						FileUtil.writeFile(new File(
								target.getResource().getLocation().toFile().getAbsolutePath() + "-ddmin-latest"),
								newCode);
					}
					return res;
				}
			});
			return apply(code, minus(edits, min), replacement);
		} else {
			return code;
		}
	}

	private static List<Replace> minus(final List<Replace> l, List<Replace> sub) {
		List<Replace>  complement = new ArrayList<Replace>(l);
		complement.removeAll(sub);
		return complement;
	}

	// edits are expected in increasing order, non overlapping
	private static String apply(String s, List<Replace> edits, String replacement) {
		int offset = 0;
		for (Replace edit: edits) {
			s = s.substring(0, edit.start + offset) + replacement + s.substring(edit.end + offset, s.length());
			offset += replacement.length() - (edit.end - edit.start);
		}
		return s;
	}

    private DDMin.Result testRefactorFile(String code, Refactor.RefactorTarget target, final List<RefactoringRule> rules, final EffApplyArgs args) {
    	final IResource resource = target.getResource();
        DDMin.Result testResult = DDMin.Result.Unknown;
        try {
            // pre condition failure
            if (!matchesPreCondition(args, code, resource)) {
                return DDMin.Result.NotReproduced;
            }
            final boolean didRefactor = refactorSourceCodeDeltaDebug(code, target, rules);
            if (didRefactor) {
            	if (args.deltaDebugTestExceptionPattern != null) {
            		testResult = DDMin.Result.NotReproduced;
            	} else {
                    // System.out.println(codePattern + " tested on " + outCode);
                    testResult = matchesPostCondition(args, resource)
                	    	? DDMin.Result.Reproduced
                            : DDMin.Result.NotReproduced;
            	}
            } else {
            	//System.out.println("warn: no refactoring done");
                testResult = DDMin.Result.NotReproduced;
            }
            /*
        } catch (org.autorefactor.util.IllegalStateException e) {
            // TODO: cleanup
            if (e.getCause() instanceof MalformedTreeException) {
                testResult = DDMin.Result.Reproduced;
            } else {
                e.printStackTrace(System.err);
            }
            */
        } catch (Exception e) {
            final Throwable cause = e.getCause();
            final String s = e.toString() + (cause != null ? ";" + cause: "");
            final Pattern p = args.deltaDebugTestExceptionPattern;
            if (p != null && p.matcher(s).matches()) {
                testResult = DDMin.Result.Reproduced;
            } else {
                e.printStackTrace(System.err);
                System.err.println("delta debugging, testable exception: '" + s + "'");
                testResult = DDMin.Result.NotReproduced;
            }
        }
        return testResult;
    }

	private boolean matchesPreCondition(final EffApplyArgs args, String code, IResource resource) throws IOException {
		final Pattern p = args.deltaDebugBeforeTestCodePattern;
		if (p != null && !p.matcher(code).matches()) {
			return false;
		}
		final String cmd = args.deltaDebugBeforeTestCodeCommand;
		if (cmd != null) {
			File tmpFile = File.createTempFile("/tmp/autorefactor",  "");
			try {
				FileUtil.writeFile(tmpFile, code);
				if (executeCommand(cmd, tmpFile) != 0) {
					return false;
				}
			} finally {
				tmpFile.delete();
			}
		}
		return true;
	}

	private boolean matchesPostCondition(final EffApplyArgs args, IResource resource) throws Exception {
		final Pattern p = args.deltaDebugTestCodePattern;
		if (p != null) {
	        String code = FileUtil.read(resource);
			if (!p.matcher(code).matches()) {
				return false;
			}
		}
		final String cmd = args.deltaDebugTestCodeCommand;
		if (cmd != null && executeCommand(cmd, resource.getLocation().toFile()) != 0) {
			return false;
		}
		return true;
	}

    private int executeCommand(String beforeTestCodeCommand, File arg1) {
    	try {
    		// TODO: use proper exec to avoid stream stalls
    		String[] cmdArray = new String[] { beforeTestCodeCommand, arg1.getAbsolutePath() };
			Process p = Runtime.getRuntime().exec(cmdArray);
    		int exitCode = p.waitFor();
    		//System.out.println("exit code " + exitCode + " when executing " + Arrays.asList(cmdArray));
			return exitCode;
    	} catch (InterruptedException e) {
    		e.printStackTrace(System.err);
    		return 2;
    	} catch (IOException e) {
    		e.printStackTrace(System.err);
    		return 2;
    	}
	}

    /*
     * Some AST infos:
     *  http://www.programcreek.com/2014/01/how-to-resolve-bindings-when-using-eclipse-jdt-astparser/
     */
    /**
     * @return didRefactor
     */
    private boolean refactorSourceCodeDeltaDebug(String code, Refactor.RefactorTarget target, final List<RefactoringRule> rules) throws Exception {
        //final String name = target.getResource().getName();

        CompilationUnit dcu = AstTools.parseCompilationUnit(code, target);

        IProblem[] problems = dcu.getProblems();
        // TODO verbose
        //System.out.println("refactorSourceCode: " + name + " (" + target.relativePath + "), #problems: " + problems.length);
        int i = 0;
        boolean error = false;
        // TODO: make sure errors are printed, not only leading warnings!
        for(IProblem problem : problems) {
            error |= problem.isError();
            i++;
            if (i <= 1 || problem.isError()) {
                System.out.println("refactorSourceCode: problem: " + problem.getMessage() + ":" + problem.getSourceStart());
            }
            if (i == 1) {
                //System.out.println("    ...");
            } else if (error) {
                break;
            }
        }
        if (problems.length > 0 && error) {
            //System.out.println("refactorSourceCode: " + name + " (" + unitName + "), aborting on #problems: " + problems.length);
            //System.out.println("    refactorSourceCode: aborting on error");
            return false;
        }
        
        return Refactor.refactorSourceCode(code, target, rules);
    }

    private static boolean LINE_MODE = true;

    private List<String> split(final String s) {
        if (LINE_MODE) {
            return new LinkedList<String>(Arrays.asList(s.split("\n")));
        } else {
            // very inefficient ... TODO
            List<String> l = new LinkedList<String>();
            for (int i = 0; i < s.length(); i++) {
                l.add(s.substring(i, i + 1));
            }
            return l;
        }
    }

    private String join(List<String> a) {
        if (LINE_MODE) {
            return join(a, "\n") + "\n";
        } else {
            return join(a, "");
        }
    }

    private String join(List<String> l, String sep) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s: l) {
            if (first) {
                first = false;
            } else {
                sb.append(sep);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private List<Character> splitChars(final String s) {
        // very inefficient ... TODO
        List<Character> l = new LinkedList<Character>();
        for (int i = 0; i < s.length(); i++) {
            l.add(s.charAt(i));
        }
        return l;
    }
    private String joinChars(List<Character> l) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Character s: l) {
            sb.append(s);
        }
        return sb.toString();
    }
}
