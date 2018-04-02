/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2015-2018 Jean-Noël Rouvignac - initial API and implementation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program under LICENSE-GNUGPL.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution under LICENSE-ECLIPSE, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.autorefactor.refactoring.rules;

import static org.autorefactor.matcher.AstMatcher.allOf;
import static org.autorefactor.matcher.AstMatcher.anyOf;
import static org.autorefactor.matcher.AstMatcher.asStmtList;
import static org.autorefactor.matcher.AstMatcher.assignment;
import static org.autorefactor.matcher.AstMatcher.booleanLiteral;
import static org.autorefactor.matcher.AstMatcher.breakStatement;
import static org.autorefactor.matcher.AstMatcher.descendant;
import static org.autorefactor.matcher.AstMatcher.enhancedForStatement;
import static org.autorefactor.matcher.AstMatcher.expressionStatement;
import static org.autorefactor.matcher.AstMatcher.forStatement;
import static org.autorefactor.matcher.AstMatcher.hasBound;
import static org.autorefactor.matcher.AstMatcher.hasBoundPM;
import static org.autorefactor.matcher.AstMatcher.ifStatement;
import static org.autorefactor.matcher.AstMatcher.ignoreParentheses;
import static org.autorefactor.matcher.AstMatcher.isEqualToBoundNode;
import static org.autorefactor.matcher.AstMatcher.isSameLocalVariableAsBoundNode;
import static org.autorefactor.matcher.AstMatcher.isSameVariableAsBoundNode;
import static org.autorefactor.matcher.AstMatcher.matchesAstOfBoundNode;
import static org.autorefactor.matcher.AstMatcher.matchesBoundNode;
import static org.autorefactor.matcher.AstMatcher.methodInvocation;
import static org.autorefactor.matcher.AstMatcher.name;
import static org.autorefactor.matcher.AstMatcher.node;
import static org.autorefactor.matcher.AstMatcher.parameterizedMatcher;
import static org.autorefactor.matcher.AstMatcher.returnStatement;
import static org.autorefactor.matcher.AstMatcher.simpleName;
import static org.autorefactor.matcher.AstMatcher.stmt;
import static org.autorefactor.matcher.AstMatcher.uniqueStatement;
import static org.autorefactor.matcher.AstMatcher.variableDeclarationFragment;
import static org.autorefactor.matcher.AstMatcher.variableDeclarationStatement;
import static org.autorefactor.refactoring.ASTHelper.DO_NOT_VISIT_SUBTREE;
import static org.autorefactor.refactoring.ASTHelper.VISIT_SUBTREE;
import static org.autorefactor.refactoring.ASTHelper.getNextSibling;
import static org.autorefactor.refactoring.ForLoopHelper.ContainerType.COLLECTION;
import static org.autorefactor.refactoring.ForLoopHelper.IterationType.INDEX;
import static org.autorefactor.refactoring.ForLoopHelper.IterationType.ITERATOR;
import static org.eclipse.jdt.core.dom.Assignment.Operator.ASSIGN;

import java.util.List;

import org.autorefactor.matcher.AstMatcher;
import org.autorefactor.matcher.AstMatcher.Matcher;
import org.autorefactor.matcher.ImmutableMap;
import org.autorefactor.matcher.MatchFinder;
import org.autorefactor.matcher.Matchers.EnhancedForStatementMatcher;
import org.autorefactor.matcher.Matchers.ExpressionStatementMatcher;
import org.autorefactor.matcher.Matchers.ForStatementMatcher;
import org.autorefactor.matcher.Matchers.StatementMatcher;
import org.autorefactor.matcher.ParameterizedMatcher;
import org.autorefactor.refactoring.ASTBuilder;
import org.autorefactor.refactoring.ASTHelper;
import org.autorefactor.refactoring.BlockSubVisitor;
import org.autorefactor.refactoring.ForLoopHelper;
import org.autorefactor.refactoring.ForLoopHelper.ForLoopContent;
import org.autorefactor.util.NotImplementedException;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/** See {@link #getDescription()} method. */
public class CollectionContainsRefactoring extends AbstractRefactoringRule {
    /**
     * Get the name.
     *
     * @return the name.
     */
    public String getName() {
        return "Collection.contains() rather than loop";
    }

    /**
     * Get the description.
     *
     * @return the description.
     */
    public String getDescription() {
        return "Replace loop with Collection.contains(Object obj).";
    }

    /**
     * Get the reason.
     *
     * @return the reason.
     */
    public String getReason() {
        return "It reduces code to focus attention on code that matters.";
    }

    @Override
    public boolean visit(Block node) {
        final AssignmentForAndReturnVisitor assignmentForAndReturnVisitor =
                new AssignmentForAndReturnVisitor(ctx, node);
        node.accept(assignmentForAndReturnVisitor);
        return assignmentForAndReturnVisitor.getResult();
    }

    private static final class AssignmentForAndReturnVisitor extends BlockSubVisitor {
        //private ASTVisitor matchVisitor;

        private static final EnhancedForStatementMatcher enhancedForWithUniqueIf =
                enhancedForStatement()
                .hasBody(uniqueStatement(ifStatement().bind("if")));

        public AssignmentForAndReturnVisitor(final RefactoringContext ctx, final Block startNode) {
            super(ctx, startNode);
            /*
            DirectMatchFinder finder = new DirectMatchFinder();

            // TODO: better to use visit methods until matcher/visitor combination is optimized

            finder.addMatcher(forStatement(), new DirectMatchCallback() {
                @Override
                public Visit onMatch(BoundNodes bounds) {
                    ForStatement node = bounds.castAs("root", ForStatement.class);
                    return Visit.fromVisitorReturn(
                            ForLoopHelper.FOR_LOOP_CONTENT_MATCHER.matchAndTransform(node)
                            .filter(loopContent -> COLLECTION.equals(loopContent.getContainerType()))
                            .map(loopContent -> {
                                if (INDEX.equals(loopContent.getIterationType())) {
                                    return maybeRefactorIndexedCollection(node, loopContent);
                                } else if (ITERATOR.equals(loopContent.getIterationType())) {
                                    return maybeRefactorIteratedCollection(node, loopContent);
                                } else {
                                    return VISIT_SUBTREE;
                                }
                            })
                            .orElse(VISIT_SUBTREE));
                }
            });
            finder.addMatcher(enhancedForWithUniqueIf, new DirectMatchCallback() {
                @Override
                public Visit onMatch(BoundNodes bounds) {
                    EnhancedForStatement node = bounds.castAs("root", EnhancedForStatement.class);
                    return Visit.fromVisitorReturn(
                            maybeReplaceWithCollectionContains(node,
                                    node.getExpression(),
                                    node.getParameter().getName(),
                                    bounds.castAs("if", IfStatement.class)));
                }
            });

            this.matchVisitor = finder.createVisitor();
            */
        }

        /*
        // TODO: better to use visit methods until matcher/visitor combination is optimized

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
        */

        /*
        @Override
        public boolean visit(EnhancedForStatement node) {
            final SingleVariableDeclaration loopVariable = node.getParameter();
            final IfStatement is = uniqueStmtAs(node.getBody(), IfStatement.class);
            return maybeReplaceWithCollectionContains(node, node.getExpression(), loopVariable.getName(), is);
        }
        */

        public boolean visit(EnhancedForStatement node) {
        	return MatchFinder
        			.match(node, enhancedForWithUniqueIf)
        			.map(bounds ->
        			maybeReplaceWithCollectionContains(node,
        					node.getExpression(),
        					node.getParameter().getName(),
        					bounds.castAs("if", IfStatement.class)))
        			.orElse(VISIT_SUBTREE);
        }

        private static Matcher<? extends Expression> isInstanceOfCollection() {
            return AstMatcher.isInstanceOf("java.util.Collection");
        }

        private static Matcher<BooleanLiteral> isNegatedLiteralOfBound(String id) {
            return matchesBoundNode(id, (n, bound) ->
                n.booleanValue() != ((BooleanLiteral) bound).booleanValue());
        }

        private static final Matcher<?> ifCondEqualsThenReturnOrBreak() {
            return ifStatement()
                    .hasCondition(
                            methodInvocation()
                            .isMethod("java.lang.Object", "equals", "java.lang.Object")
                            .bind("mi"))
                    .hasThen(asStmtList()
                            .isNotEmpty()
                            .anyOf(
                                    asStmtList()
                                    .hasSize(1)
                                    .hasIndex(0, returnStatement()
                                            .hasExpression(booleanLiteral().bind("innerBl"))
                                            .bind("thenStmt")),
                                    asStmtList()
                                    .hasSize(1)
                                    .hasIndex(0, stmt()
                                    		.bind("thenStmt")),
                                    asStmtList()
                                    .lastIndex(breakStatement().unlessHasLabel().bind("break"))
                                    // "not loopElementIsUsed":
                                    // do not match if
                                    //  1. forVar is simple name and
                                    //  2. any descendant of statements contains SimpleName with isSameLocalVariable(forVar)
                                    .unless(
                                    		allOf(
                                    				hasBound("forVar", simpleName()),
                                    				descendant(simpleName().that(isSameLocalVariableAsBoundNode("forVar")))))
                            		))
                    .unlessHasElseStatement();
        }

        private static final StatementMatcher nextStmtReturnsNegatedLiteral() {
            return stmt()
                    .nextStatement(returnStatement()
                            .bind("forNextStmt")
                            .hasExpression(
                                    booleanLiteral()
                                    .that(isNegatedLiteralOfBound("innerBl"))));
        }

        private static final String findTargetExpression_Result = "result";
        private static Matcher<?> findTargetExpression() {
            return anyOf(
                    methodInvocation()
                    .hasExpression(ignoreParentheses(isSameVariableAsBoundNode("forVar")))
                    .hasArgumentAt(0, ignoreParentheses().bind(findTargetExpression_Result)),
                    methodInvocation()
                    .hasArgumentAt(0, ignoreParentheses(isSameVariableAsBoundNode("forVar")))
                    .hasExpression(ignoreParentheses().bind(findTargetExpression_Result)),
                    methodInvocation()
                    .hasExpression(ignoreParentheses(matchesAstOfBoundNode("forVar")))
                    .hasArgumentAt(0, ignoreParentheses().bind(findTargetExpression_Result)),
                    methodInvocation()
                    .hasArgumentAt(0, ignoreParentheses(matchesAstOfBoundNode("forVar")))
                    .hasExpression(ignoreParentheses().bind(findTargetExpression_Result))
                    );
        }

        // TODO: make non overriding of bindings the default
        // main output is "result" of expressionToFind
        private static final ParameterizedMatcher maybeReplaceWithCollectionContainsMatcher =
                parameterizedMatcher("if", "forNode", "forVar", "iterable")
                // make argument a bound variable for reference below
                .hasParam("forNode", node().bind("forNode"))
                // make argument a bound variable for reference by expressionToFind matcher
                .hasParam("forVar", node().bind("forVar"))
                .hasParam("iterable", isInstanceOfCollection())
                // provides mi, thenStmt/break, innerBl,
                .hasParam("if", ifCondEqualsThenReturnOrBreak())
                // provides target expression as "result"
                .hasBound("mi", findTargetExpression())
                .anyOf(
                        // a single "then" statement with extra restriction
                        hasBoundPM("thenStmt",
                                // requires innerBl
                                hasBound("forNode",
                                        nextStmtReturnsNegatedLiteral(),
                                        node().bind("replaceLoopAndReturn"))),
                        // or a single then statement
                        hasBoundPM("thenStmt"),
                        // or a break as last statement
                        hasBoundPM("break"));

        private boolean maybeReplaceWithCollectionContains(
                Statement forNode, Expression iterable, Expression loopElement, IfStatement is) {
            return maybeReplaceWithCollectionContainsMatcher
            		// apply parameterized matcher with parameters
                    .match(
                            ImmutableMap.of(
                                    "if", is,
                                    "forNode", forNode,
                                    "forVar", loopElement,
                                    "iterable", iterable))
                    // act on matched
                    .map(bounds -> {
                        if (bounds.containsBound("replaceLoopAndReturn")) {
                            replaceLoopAndReturn(forNode, iterable,
                                    bounds.castAs(findTargetExpression_Result, Expression.class),
                                    bounds.castAs("forNextStmt", Statement.class),
                                    bounds.castAs("innerBl", BooleanLiteral.class).booleanValue());
                            setResult(DO_NOT_VISIT_SUBTREE);
                            return DO_NOT_VISIT_SUBTREE;
                        }
                        final Statement thenStmt = bounds.getAs("thenStmt", Statement.class);
                        if (thenStmt != null) {
                            return maybeReplaceLoopAndVariable(forNode, iterable,
                                    thenStmt,
                                    bounds.castAs(findTargetExpression_Result, Expression.class));
                        } else if (bounds.containsBound("break")) {
                            // last statement of then is simple break
                            List<Statement> thenStmts = ASTHelper.asList(is.getThenStatement());
                            final Expression toFind = bounds.castAs(findTargetExpression_Result, Expression.class);
                            if (thenStmts.size() == 2
                                && maybeReplaceLoopAndVariable(forNode, iterable, thenStmts.get(0), toFind)
                                    == DO_NOT_VISIT_SUBTREE) {
                                return DO_NOT_VISIT_SUBTREE;
                            }

                            replaceLoopByIf(forNode, iterable, thenStmts, toFind,
                                    bounds.castAs("break", BreakStatement.class));
                            setResult(DO_NOT_VISIT_SUBTREE);
                            return DO_NOT_VISIT_SUBTREE;
                        }
                        return VISIT_SUBTREE;
                    })
                    .orElse(VISIT_SUBTREE);
        }

                /*
        private boolean maybeReplaceWithCollectionContains(
                Statement forNode, Expression iterable, Expression loopElement, IfStatement is) {
            if (is == null) {
                return VISIT_SUBTREE;
            }
            if (is != null
                    && is.getElseStatement() == null
                    && instanceOf(iterable, "java.util.Collection")) {
                MethodInvocation cond = as(is.getExpression(), MethodInvocation.class);
                List<Statement> thenStmts = asList(is.getThenStatement());
                if (!thenStmts.isEmpty()
                        && isMethod(cond, "java.lang.Object", "equals", "java.lang.Object")) {
                    Expression toFind = getExpressionToFind(cond, loopElement);
                    if (toFind != null) {
                        if (thenStmts.size() == 1) {
                            Statement thenStmt = thenStmts.get(0);
                            BooleanLiteral innerBl = getReturnedBooleanLiteral(thenStmt);

                            Statement forNextStmt = getNextStatement(forNode);
                            BooleanLiteral outerBl = getReturnedBooleanLiteral(forNextStmt);

                            Boolean isPositive = signCollectionContains(innerBl, outerBl);
                            if (isPositive != null) {
                                replaceLoopAndReturn(forNode, iterable, toFind, forNextStmt, isPositive);
                                setResult(DO_NOT_VISIT_SUBTREE);
                                return DO_NOT_VISIT_SUBTREE;
                            }
                            return maybeReplaceLoopAndVariable(forNode, iterable, thenStmt, toFind);
                        } else {
                            BreakStatement bs = as(thenStmts.get(thenStmts.size() - 1), BreakStatement.class);
                            if (bs != null && bs.getLabel() == null) {
                                if (thenStmts.size() == 2
                                        && maybeReplaceLoopAndVariable(forNode, iterable, thenStmts.get(0), toFind)
                                            == DO_NOT_VISIT_SUBTREE) {
                                    return DO_NOT_VISIT_SUBTREE;
                                }

                                if (loopElementIsUsed(loopElement, thenStmts)) {
                                    // Cannot remove the loop and its loop element
                                    return VISIT_SUBTREE;
                                }

                                replaceLoopByIf(forNode, iterable, thenStmts, toFind, bs);
                                setResult(DO_NOT_VISIT_SUBTREE);
                                return DO_NOT_VISIT_SUBTREE;
                            }
                        }
                    }
                }
            }
            return VISIT_SUBTREE;
        }

        private boolean loopElementIsUsed(Expression loopElement, List<Statement> thenStmts) {
            if (loopElement instanceof SimpleName) {
                VarUseFinderVisitor visitor = new VarUseFinderVisitor((SimpleName) loopElement);
                for (Statement aThenStmt : thenStmts) {
                    if (visitor.findOrDefault(aThenStmt, false)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static class VarUseFinderVisitor extends FinderVisitor<Boolean> {
            private final SimpleName varName;

            public VarUseFinderVisitor(SimpleName varName) {
                this.varName = varName;
            }

            @Override
            public boolean visit(SimpleName variable) {
                if (isSameLocalVariable(varName, variable)) {
                    setResult(true);
                    return DO_NOT_VISIT_SUBTREE;
                }
                return VISIT_SUBTREE;
            }
        }
*/

        private void replaceLoopByIf(Statement forNode, Expression iterable, List<Statement> thenStmts,
                Expression toFind, BreakStatement bs) {
            thenStmts.remove(thenStmts.size() - 1);

            ASTBuilder b = getCtx().getASTBuilder();
            Statement replacement = b.if0(collectionContains(iterable, toFind, true, b),
                    b.block(b.copyRange(thenStmts)));
            getCtx().getRefactorings().replace(forNode, replacement);

            thenStmts.add(bs);
        }

        private void replaceLoopAndReturn(Statement forNode, Expression iterable, Expression toFind,
                Statement forNextStmt, boolean negate) {
            ASTBuilder b = getCtx().getASTBuilder();
            getCtx().getRefactorings().replace(forNode,
                    b.return0(
                            collectionContains(iterable, toFind, negate, b)));
            if (forNextStmt.equals(getNextSibling(forNode))) {
                getCtx().getRefactorings().remove(forNextStmt);
            }
        }

        private static final StatementMatcher previousStmtIsbooleanLiteralVarOrAssignment =
                stmt()
                .previousStatement(
                        stmt()
                        .anyOf(variableDeclarationStatement()
                                .fragmentCountIs(1)
                                .hasFragment(variableDeclarationFragment()
                                        .hasName(simpleName().bind("name"))
                                        .hasInitializer(booleanLiteral().bind("init")))
                                ,
                                expressionStatement()
                                .hasExpression(assignment()
                                        .hasAssignOperator()
                                        .hasLeftHandSide(name().bind("name"))
                                        .hasRightHandSide(booleanLiteral().bind("init")))
                                )
                        .bind("previousStatement"))
                // bind the previous sibling if present without restricting the match ("optional bind")
                .maybe(
                        stmt()
                        .previousSibling(
                                stmt()
                                .that(isEqualToBoundNode("previousStatement"))
                                .bind("previousStmtIsPreviousSibling")));

        // Attention: names must not collide with names of matcher above
        private static final ExpressionStatementMatcher assignmentOfSameVariableAndNegatedBooleanLiteral =
                expressionStatement()
                .hasExpression(ignoreParentheses(
                        assignment()
                        .hasOperator(ASSIGN)
                        .hasLeftHandSide(ignoreParentheses(
                                name()
                                .that(isSameVariableAsBoundNode("name"))
                                .bind("assignmentName")))
                        .hasRightHandSide(ignoreParentheses(
                                booleanLiteral()
                                .that(isNegatedLiteralOfBound("init"))
                                .bind("assignmentInit")))));

        private static final ParameterizedMatcher maybeReplaceLoopAndVariableMatcher =
                parameterizedMatcher("forNode", "uniqueThenStmt")
                .hasParam("forNode", previousStmtIsbooleanLiteralVarOrAssignment)
                .hasParam("uniqueThenStmt", assignmentOfSameVariableAndNegatedBooleanLiteral);

        private boolean maybeReplaceLoopAndVariable(
                Statement forNode, Expression iterable, Statement uniqueThenStmt, Expression toFind) {
            return maybeReplaceLoopAndVariableMatcher
            		// match with parameters
                    .match(ImmutableMap.of("forNode", forNode, "uniqueThenStmt", uniqueThenStmt))
                    // act on matched
                    .map(bounds -> {
                        // TODO: check that not multiple matches
                        final Name innerInitName = bounds.castAs("assignmentName", Name.class);
                        final BooleanLiteral innerInitInit = bounds.castAs("assignmentInit", BooleanLiteral.class);
                        final boolean previousStmtIsPreviousSibling = bounds.containsBound("previousStmtIsPreviousSibling");
                        final Statement previousStmt = bounds.castAs("previousStatement", Statement.class);

                        replaceLoopAndVariable(forNode, iterable, toFind, previousStmt,
                                previousStmtIsPreviousSibling, innerInitName, /* is positive */ innerInitInit.booleanValue());
                        setResult(DO_NOT_VISIT_SUBTREE);
                        return DO_NOT_VISIT_SUBTREE;
                    })
                    .orElse(VISIT_SUBTREE);
        }
            /*
        private boolean maybeReplaceLoopAndVariable(
                Statement forNode, Expression iterable, Statement uniqueThenStmt, Expression toFind) {
            Statement previousStmt = getPreviousStatement(forNode);
            if (previousStmt != null) {
                boolean previousStmtIsPreviousSibling = previousStmt.equals(getPreviousSibling(forNode));
                Assignment as = asExpression(uniqueThenStmt, Assignment.class);
                Pair<Name, Expression> innerInit = decomposeInitializer(as);
                Name initName = innerInit.getFirst();
                Expression init2 = innerInit.getSecond();
                Pair<Name, Expression> outerInit = getInitializer(previousStmt);
                if (isSameVariable(outerInit.getFirst(), initName)) {
                    Boolean isPositive = signCollectionContains((BooleanLiteral) init2,
                            (BooleanLiteral) outerInit.getSecond());
                    if (isPositive != null) {
                        replaceLoopAndVariable(forNode, iterable, toFind, previousStmt,
                                previousStmtIsPreviousSibling, initName, isPositive);
                        setResult(DO_NOT_VISIT_SUBTREE);
                        return DO_NOT_VISIT_SUBTREE;
                    }
                }
            }
            return VISIT_SUBTREE;
        }
            */

        private void replaceLoopAndVariable(Statement forNode, Expression iterable, Expression toFind,
                Statement previousStmt, boolean previousStmtIsPreviousSibling, Name initName, boolean isPositive) {
            ASTBuilder b = getCtx().getASTBuilder();
            Statement replacement;
            if (previousStmtIsPreviousSibling
                    && previousStmt instanceof VariableDeclarationStatement) {
                replacement = b.declareStmt(b.type("boolean"), b.move((SimpleName) initName),
                        collectionContains(iterable, toFind, isPositive, b));
            } else if (!previousStmtIsPreviousSibling
                    || previousStmt instanceof ExpressionStatement) {
                replacement = b.toStmt(b.assign(b.copy(initName), ASSIGN,
                        collectionContains(iterable, toFind, isPositive, b)));
            } else {
                throw new NotImplementedException(forNode);
            }
            getCtx().getRefactorings().replace(forNode, replacement);
            if (previousStmtIsPreviousSibling) {
                getCtx().getRefactorings().remove(previousStmt);
            }
        }

        private Expression collectionContains(Expression iterable, Expression toFind, boolean isPositive,
                ASTBuilder b) {
            final MethodInvocation invoke = b.invoke(b.move(iterable), "contains", b.move(toFind));
            if (isPositive) {
                return invoke;
            } else {
                return b.not(invoke);
            }
        }
/*
        private Boolean signCollectionContains(BooleanLiteral innerBl, BooleanLiteral outerBl) {
            if (innerBl != null
                    && outerBl != null
                    && innerBl.booleanValue() != outerBl.booleanValue()) {
                return innerBl.booleanValue();
            }
            return null;
        }

        private Pair<Name, Expression> getInitializer(Statement stmt) {
            if (stmt instanceof VariableDeclarationStatement) {
                return uniqueVariableDeclarationFragmentName(stmt);
            } else if (stmt instanceof ExpressionStatement) {
                Assignment as = asExpression(stmt, Assignment.class);
                if (hasOperator(as, ASSIGN)
                        && as.getLeftHandSide() instanceof Name) {
                    return Pair.of((Name) as.getLeftHandSide(), as.getRightHandSide());
                }
            }
            return Pair.empty();
        }

        private IfStatement uniqueStmtAs(Statement stmt, Class<IfStatement> stmtClazz) {
            return as(uniqueStmt(asList(stmt)), stmtClazz);
        }

        private Statement uniqueStmt(List<Statement> stmts) {
            return stmts.size() == 1 ? stmts.get(0) : null;
        }

        private BooleanLiteral getReturnedBooleanLiteral(Statement stmt) {
            ReturnStatement rs = as(stmt, ReturnStatement.class);
            if (rs != null) {
                return as(rs.getExpression(), BooleanLiteral.class);
            }
            return null;
        }

        private Expression getExpressionToFind(MethodInvocation cond, Expression forVar) {
            Expression expr = removeParentheses(cond.getExpression());
            Expression arg0 = removeParentheses(arg0(cond));
            if (isSameVariable(forVar, expr)) {
                return arg0;
            } else if (isSameVariable(forVar, arg0)) {
                return expr;
            } else if (matches(forVar, expr)) {
                return arg0;
            } else if (matches(forVar, arg0)) {
                return expr;
            } else {
                return null;
            }
        }

        @Override
        public boolean visit(ForStatement node) {
            final ForLoopContent loopContent = iterateOverContainer(node);
            final List<Statement> stmts = asList(node.getBody());
            if (loopContent != null
                    && COLLECTION.equals(loopContent.getContainerType())) {
                if (INDEX.equals(loopContent.getIterationType())) {
                    Expression loopElement = null;
                    IfStatement is;
                    if (stmts.size() == 2) {
                        Pair<Name, Expression> loopVarPair = uniqueVariableDeclarationFragmentName(stmts.get(0));
                        loopElement = loopVarPair.getFirst();
                        MethodInvocation mi = as(loopVarPair.getSecond(), MethodInvocation.class);
                        if (!matches(mi, collectionGet(loopContent))
                                || !isSameVariable(mi.getExpression(), loopContent.getContainerVariable())) {
                            return VISIT_SUBTREE;
                        }

                        is = as(stmts.get(1), IfStatement.class);
                    } else if (stmts.size() == 1) {
                        is = as(stmts.get(0), IfStatement.class);
                        loopElement = collectionGet(loopContent);
                    } else {
                        return VISIT_SUBTREE;
                    }

                    return maybeReplaceWithCollectionContains(node, loopContent.getContainerVariable(), loopElement,
                            is);
                } else if (ITERATOR.equals(loopContent.getIterationType())) {
                    Expression loopElement = null;
                    IfStatement is;
                    if (stmts.size() == 2) {
                        Pair<Name, Expression> loopVarPair = uniqueVariableDeclarationFragmentName(stmts.get(0));
                        loopElement = loopVarPair.getFirst();
                        MethodInvocation mi = as(loopVarPair.getSecond(), MethodInvocation.class);
                        if (!matches(mi, iteratorNext(loopContent))
                                || !isSameVariable(mi.getExpression(), loopContent.getIteratorVariable())) {
                            return VISIT_SUBTREE;
                        }

                        is = as(stmts.get(1), IfStatement.class);
                    } else if (stmts.size() == 1) {
                        is = as(stmts.get(0), IfStatement.class);
                        loopElement = iteratorNext(loopContent);
                    } else {
                        return VISIT_SUBTREE;
                    }

                    return maybeReplaceWithCollectionContains(node, loopContent.getContainerVariable(), loopElement,
                            is);
                }
            }
            return VISIT_SUBTREE;
        }
*/
        private static final ForStatementMatcher FOR_INDEX =
                forStatement()
                .hasBody(
                        anyOf(
                                asStmtList()
                                .hasSize(2)
                                .hasIndex(0,
                                        uniqueStatement(
                                                variableDeclarationStatement()
                                                .fragmentCountIs(1)
                                                .hasFragment(variableDeclarationFragment()
                                                        .hasName(simpleName().bind("name"))
                                                        .hasInitializer(ignoreParentheses(
                                                                methodInvocation()
                                                                .hasExpression(ignoreParentheses(
                                                                        matchesAstOfBoundNode("lc.containerVariable"),
                                                                        isSameVariableAsBoundNode("lc.containerVariable")))
                                                                .hasName("get")
                                                                .hasArgumentAt(0, matchesAstOfBoundNode("lc.loopVariable"))
                                                                .bind("init"))))))
                                .hasIndex(1, ifStatement().bind("if")),
                                asStmtList()
                                .hasSize(1)
                                .hasIndex(0, ifStatement().bind("if"))));

        private boolean maybeRefactorIndexedCollection(ForStatement node, ForLoopContent loopContent) {
            return MatchFinder
                    .match(node,
                            FOR_INDEX,
                            ImmutableMap.of(
                                    "lc.containerVariable", loopContent.getContainerVariable(),
                                    "lc.loopVariable", loopContent.getLoopVariable()))
                    .map(bounds -> {
                        Expression loopElement = bounds.getAs("name", Name.class);
                        if (loopElement == null) {
                            loopElement = collectionGet(loopContent);
                        }
                        IfStatement is = bounds.castAs("if", IfStatement.class);

                        return maybeReplaceWithCollectionContains(node, loopContent.getContainerVariable(), loopElement, is);
                    })
                    .orElse(VISIT_SUBTREE);
        }

        private static final ForStatementMatcher FOR_ITERATOR =
                forStatement()
                .hasBody(
                        anyOf(
                                asStmtList()
                                .hasSize(2)
                                .hasIndex(0,
                                        uniqueStatement(
                                                variableDeclarationStatement()
                                                .fragmentCountIs(1)
                                                .hasFragment(variableDeclarationFragment()
                                                        .hasName(simpleName().bind("name"))
                                                        .hasInitializer(ignoreParentheses(
                                                                methodInvocation()
                                                                .hasExpression(ignoreParentheses(
                                                                        matchesAstOfBoundNode("lc.iteratorVariable"),
                                                                        isSameVariableAsBoundNode("lc.iteratorVariable")))
                                                                .hasName("next")
                                                                .bind("init"))))))
                                .hasIndex(1, ifStatement().bind("if")),
                                asStmtList()
                                .hasSize(1)
                                .hasIndex(0, ifStatement().bind("if"))));

        private boolean maybeRefactorIteratedCollection(ForStatement node, ForLoopContent loopContent) {
            return MatchFinder
                    .match(node,
                            FOR_ITERATOR,
                            ImmutableMap.of("lc.iteratorVariable", loopContent.getIteratorVariable()))
                    .map(bounds -> {
                        Expression loopElement = bounds.getAs("name", Name.class);
                        if (loopElement == null) {
                            loopElement = iteratorNext(loopContent);
                        }
                        IfStatement is = bounds.castAs("if", IfStatement.class);

                        return maybeReplaceWithCollectionContains(node, loopContent.getContainerVariable(), loopElement, is);
                    })
                    .orElse(VISIT_SUBTREE);
        }

        @Override
        public boolean visit(ForStatement node) {
        	return 
        			ForLoopHelper.FOR_LOOP_CONTENT_MATCHER
        			.matchAndTransform(node)
        			.filter(loopContent -> COLLECTION.equals(loopContent.getContainerType()))
        			.map(loopContent -> {
        				if (INDEX.equals(loopContent.getIterationType())) {
        					return maybeRefactorIndexedCollection(node, loopContent);
        				} else if (ITERATOR.equals(loopContent.getIterationType())) {
        					return maybeRefactorIteratedCollection(node, loopContent);
        				} else {
        					return VISIT_SUBTREE;
        				}
        			})
        			.orElse(VISIT_SUBTREE);
        }
        private MethodInvocation iteratorNext(final ForLoopContent loopContent) {
            ASTBuilder b = getCtx().getASTBuilder();
            return b.invoke(
                    b.copySubtree(loopContent.getIteratorVariable()),
                    "next");
        }

        private MethodInvocation collectionGet(final ForLoopContent loopContent) {
            ASTBuilder b = getCtx().getASTBuilder();
            return b.invoke(
                    b.copySubtree(loopContent.getContainerVariable()),
                    "get",
                    b.copySubtree(loopContent.getLoopVariable()));
        }
/*
        private Pair<Name, Expression> uniqueVariableDeclarationFragmentName(Statement stmt) {
            VariableDeclarationFragment vdf = getUniqueFragment(as(stmt, VariableDeclarationStatement.class));
            if (vdf != null) {
                return Pair.of((Name) vdf.getName(), vdf.getInitializer());
            }
            return Pair.empty();
        }
    */
    }
}
