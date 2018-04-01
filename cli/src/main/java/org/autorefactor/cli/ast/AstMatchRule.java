package org.autorefactor.cli.ast;

import static org.autorefactor.refactoring.ASTHelper.DO_NOT_VISIT_SUBTREE;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import org.autorefactor.cli.FileUtil;
import org.autorefactor.matcher.AstMatcher.BoundNodes;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.autorefactor.matcher.DirectMatchCallback;
import org.autorefactor.matcher.DirectMatchFinder;
import org.autorefactor.refactoring.rules.AbstractRefactoringRule;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

@SuppressWarnings("restriction")
public class AstMatchRule extends AbstractRefactoringRule {

    @Override
    public String getDescription() {
        return "Apply groovy match expression.";
    }

    @Override
    public String getName() {
        return "Apply groovy match expression.";
    }

    @Override
    public String getReason() {
        return "Print ast nodes matching given matcher.";
    }

    private ASTVisitor matchVisitor;

    public AstMatchRule(Matcher<?> matcher) {
        // Attention: currently first match wins?
        DirectMatchFinder finder = new DirectMatchFinder();

        finder.addMatcher(matcher, new DirectMatchCallback() {
            @Override
            public Visit onMatch(BoundNodes bounds) {
                ASTNode node = bounds.castAs("root", ASTNode.class);
                Map<String,Object> map = new TreeMap<>(bounds.getSingleMap());
                
                File file = filePath(node);
                String source = ""; 
                try {
                	source = FileUtil.readFile(file.getAbsolutePath());
                } catch (Exception e) {
                	e.printStackTrace();
                }
                String[] lines = source.split("(\\r)?\\n", -1);
                
                System.out.println();
                printNode(node, "root", lines);
                map.remove("root");
                map.entrySet().stream().forEach(e -> printNode((ASTNode)e.getValue(), e.getKey(), lines));
                return Visit.fromVisitorReturn(true);
            }

			public void printNode(ASTNode node, String id, String[] lines) {
                CompilationUnit cu = compilationUnit(node);
				System.out.println(location(node) + ": note: \"" + id + "\" binds here");
				final int lineNumber = cu.getLineNumber(node.getStartPosition());
				System.out.println(lineNumber > lines.length ? "<no source> (have: " + lines.length + " lines)" : lines[lineNumber - 1]);
				// TODO: let ~ end at end position?
				// 1 based
				int startCol = cu.getColumnNumber(node.getStartPosition());
				// optimizable ... (TODO)
                System.out.printf("%" + startCol + "s%s%n", " ", "^~~~~~");
			}
        });

        this.matchVisitor = finder.createVisitor();
    }

    // location is printed half open
    private String location(ASTNode node) {
        try {
            CompilationUnit cu = (CompilationUnit) node.getRoot();
            // starts with 1
            int startLine = cu.getLineNumber(node.getStartPosition());
            int startCol = cu.getColumnNumber(node.getStartPosition()) + 1;
            int endLine = cu.getLineNumber(node.getStartPosition() + node.getLength());
            int endCol = cu.getColumnNumber(node.getStartPosition() + node.getLength());
            return filePath(node)
            		//cu.getTypeRoot().getPath().toFile()//cu.getTypeRoot().getPath().makeRelative()
                    + ((startLine != endLine || startCol != endCol)
                            ?  ":(" + startLine + "." + startCol + "-" + endLine + "." + endCol + ")"
                            : ":(" + startLine + "." + startCol + ")");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private File filePath(ASTNode node) {
        CompilationUnit cu = compilationUnit(node);
        return cu.getTypeRoot().getResource().getLocation().toFile();
    }

	public CompilationUnit compilationUnit(ASTNode node) {
		return (CompilationUnit) node.getRoot();
	}

    @Override
    public boolean preVisit2(ASTNode node) {
        boolean visit = super.preVisit2(node);
        //System.out.println("previsit2: node=" + node.getClass().getName());
        //System.out.println("    visit=" + visit);
        if (!visit) {
            return DO_NOT_VISIT_SUBTREE;
        }
        return matchVisitor.preVisit2(node);
    }
}
