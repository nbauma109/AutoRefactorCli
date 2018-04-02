package org.autorefactor.matcher;

import static org.autorefactor.matcher.InternalMatcherUtil.tryCast;

import org.autorefactor.matcher.AstMatcher.Matcher;
import org.autorefactor.refactoring.ASTHelper;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotatableType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * Eclipse JDT dom ast node specific matcher classes.
 *
 * This is file is generated by AstMatcherGenerator.
 */
public final class Matchers {

    private Matchers() {}

    /**
     * Matcher builder for AST node BreakStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class BreakStatementMatcher
        extends StatementMatcherSupport<BreakStatement, BreakStatementMatcher> {

        BreakStatementMatcher() {
            super(BreakStatement.class);
        }

        public final BreakStatementMatcher hasLabel(String name) {
            addPredicate("label", n -> { SimpleName sn = tryCast(n.getLabel(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final BreakStatementMatcher hasLabel(Matcher<? extends SimpleName>... matchers) {
            addProperty("label", SimpleName.class, BreakStatement::getLabel, matchers);
            return this;
        }

        public final BreakStatementMatcher unlessHasLabel() {
            addPredicate("label", n -> n.getLabel() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ArrayCreation.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ArrayCreationMatcher
        extends ExpressionMatcherSupport<ArrayCreation, ArrayCreationMatcher> {

        ArrayCreationMatcher() {
            super(ArrayCreation.class);
        }

        @SafeVarargs
        public final ArrayCreationMatcher hasType(Matcher<? extends ArrayType>... matchers) {
            addProperty("type", ArrayType.class, ArrayCreation::getType, matchers);
            return this;
        }

        @SafeVarargs
        public final ArrayCreationMatcher hasDimension(Matcher<? extends Expression>... matchers) {
            addPropertyList("dimensions", Expression.class, ArrayCreation::dimensions, matchers);
            return this;
        }

        public final ArrayCreationMatcher dimensionCountIs(int count) {
            addCountIs(ArrayCreation::dimensions, count);
            return this;
        }

        @SafeVarargs
        public final ArrayCreationMatcher hasDimensionAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("dimensions", Expression.class, ArrayCreation::dimensions, index, matchers);
            return this;
        }

        @SafeVarargs
        public final ArrayCreationMatcher hasInitializer(Matcher<? extends ArrayInitializer>... matchers) {
            addProperty("initializer", ArrayInitializer.class, ArrayCreation::getInitializer, matchers);
            return this;
        }

        public final ArrayCreationMatcher unlessHasInitializer() {
            addPredicate("initializer", n -> n.getInitializer() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node VariableDeclarationExpression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class VariableDeclarationExpressionMatcher
        extends ExpressionMatcherSupport<VariableDeclarationExpression, VariableDeclarationExpressionMatcher> {

        VariableDeclarationExpressionMatcher() {
            super(VariableDeclarationExpression.class);
        }

        @SafeVarargs
        public final VariableDeclarationExpressionMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, VariableDeclarationExpression::getType, matchers);
            return this;
        }

        @SafeVarargs
        public final VariableDeclarationExpressionMatcher hasFragment(Matcher<? extends VariableDeclarationFragment>... matchers) {
            addPropertyList("fragments", VariableDeclarationFragment.class, VariableDeclarationExpression::fragments, matchers);
            return this;
        }

        public final VariableDeclarationExpressionMatcher fragmentCountIs(int count) {
            addCountIs(VariableDeclarationExpression::fragments, count);
            return this;
        }

        @SafeVarargs
        public final VariableDeclarationExpressionMatcher hasFragmentAt(int index, Matcher<? extends VariableDeclarationFragment>... matchers) {
            addPropertyListElement("fragments", VariableDeclarationFragment.class, VariableDeclarationExpression::fragments, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node CastExpression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class CastExpressionMatcher
        extends ExpressionMatcherSupport<CastExpression, CastExpressionMatcher> {

        CastExpressionMatcher() {
            super(CastExpression.class);
        }

        @SafeVarargs
        public final CastExpressionMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, CastExpression::getType, matchers);
            return this;
        }

        @SafeVarargs
        public final CastExpressionMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, CastExpression::getExpression, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node Annotation.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class AnnotationMatcher
        extends ExpressionMatcherSupport<Annotation, AnnotationMatcher> {

        AnnotationMatcher() {
            super(Annotation.class);
        }
    }

    /**
     * Matcher builder for AST node InstanceofExpression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class InstanceofExpressionMatcher
        extends ExpressionMatcherSupport<InstanceofExpression, InstanceofExpressionMatcher> {

        InstanceofExpressionMatcher() {
            super(InstanceofExpression.class);
        }

        @SafeVarargs
        public final InstanceofExpressionMatcher hasLeftOperand(Matcher<? extends Expression>... matchers) {
            addProperty("leftOperand", Expression.class, InstanceofExpression::getLeftOperand, matchers);
            return this;
        }

        @SafeVarargs
        public final InstanceofExpressionMatcher hasRightOperand(Matcher<? extends Type>... matchers) {
            addProperty("rightOperand", Type.class, InstanceofExpression::getRightOperand, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ContinueStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ContinueStatementMatcher
        extends StatementMatcherSupport<ContinueStatement, ContinueStatementMatcher> {

        ContinueStatementMatcher() {
            super(ContinueStatement.class);
        }

        public final ContinueStatementMatcher hasLabel(String name) {
            addPredicate("label", n -> { SimpleName sn = tryCast(n.getLabel(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final ContinueStatementMatcher hasLabel(Matcher<? extends SimpleName>... matchers) {
            addProperty("label", SimpleName.class, ContinueStatement::getLabel, matchers);
            return this;
        }

        public final ContinueStatementMatcher unlessHasLabel() {
            addPredicate("label", n -> n.getLabel() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node FieldDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class FieldDeclarationMatcher
        extends CommonMatcher<FieldDeclaration, FieldDeclarationMatcher> {

        FieldDeclarationMatcher() {
            super(FieldDeclaration.class);
        }

        @SafeVarargs
        public final FieldDeclarationMatcher hasJavadoc(Matcher<? extends Javadoc>... matchers) {
            addProperty("javadoc", Javadoc.class, FieldDeclaration::getJavadoc, matchers);
            return this;
        }

        public final FieldDeclarationMatcher unlessHasJavadoc() {
            addPredicate("javadoc", n -> n.getJavadoc() == null);
            return this;
        }

        @SafeVarargs
        public final FieldDeclarationMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, FieldDeclaration::getType, matchers);
            return this;
        }

        @SafeVarargs
        public final FieldDeclarationMatcher hasFragment(Matcher<? extends VariableDeclarationFragment>... matchers) {
            addPropertyList("fragments", VariableDeclarationFragment.class, FieldDeclaration::fragments, matchers);
            return this;
        }

        public final FieldDeclarationMatcher fragmentCountIs(int count) {
            addCountIs(FieldDeclaration::fragments, count);
            return this;
        }

        @SafeVarargs
        public final FieldDeclarationMatcher hasFragmentAt(int index, Matcher<? extends VariableDeclarationFragment>... matchers) {
            addPropertyListElement("fragments", VariableDeclarationFragment.class, FieldDeclaration::fragments, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node QualifiedType.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class QualifiedTypeMatcher
        extends CommonMatcher<QualifiedType, QualifiedTypeMatcher> {

        QualifiedTypeMatcher() {
            super(QualifiedType.class);
        }

        @SafeVarargs
        public final QualifiedTypeMatcher hasQualifier(Matcher<? extends Type>... matchers) {
            addProperty("qualifier", Type.class, QualifiedType::getQualifier, matchers);
            return this;
        }

        @SafeVarargs
        public final QualifiedTypeMatcher hasAnnotation(Matcher<? extends Annotation>... matchers) {
            addPropertyList("annotations", Annotation.class, QualifiedType::annotations, matchers);
            return this;
        }

        public final QualifiedTypeMatcher annotationCountIs(int count) {
            addCountIs(QualifiedType::annotations, count);
            return this;
        }

        @SafeVarargs
        public final QualifiedTypeMatcher hasAnnotationAt(int index, Matcher<? extends Annotation>... matchers) {
            addPropertyListElement("annotations", Annotation.class, QualifiedType::annotations, index, matchers);
            return this;
        }

        public final QualifiedTypeMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final QualifiedTypeMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, QualifiedType::getName, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node CatchClause.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class CatchClauseMatcher
        extends CommonMatcher<CatchClause, CatchClauseMatcher> {

        CatchClauseMatcher() {
            super(CatchClause.class);
        }

        @SafeVarargs
        public final CatchClauseMatcher hasException(Matcher<? extends SingleVariableDeclaration>... matchers) {
            addProperty("exception", SingleVariableDeclaration.class, CatchClause::getException, matchers);
            return this;
        }

        @SafeVarargs
        public final CatchClauseMatcher hasBody(Matcher<? extends Block>... matchers) {
            addProperty("body", Block.class, CatchClause::getBody, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node MemberValuePair.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class MemberValuePairMatcher
        extends CommonMatcher<MemberValuePair, MemberValuePairMatcher> {

        MemberValuePairMatcher() {
            super(MemberValuePair.class);
        }

        public final MemberValuePairMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final MemberValuePairMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, MemberValuePair::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final MemberValuePairMatcher hasValue(Matcher<? extends Expression>... matchers) {
            addProperty("value", Expression.class, MemberValuePair::getValue, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node VariableDeclarationFragment.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class VariableDeclarationFragmentMatcher
        extends CommonMatcher<VariableDeclarationFragment, VariableDeclarationFragmentMatcher> {

        VariableDeclarationFragmentMatcher() {
            super(VariableDeclarationFragment.class);
        }

        public final VariableDeclarationFragmentMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final VariableDeclarationFragmentMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, VariableDeclarationFragment::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final VariableDeclarationFragmentMatcher hasExtraDimension(Matcher<? extends Dimension>... matchers) {
            addPropertyList("extraDimensions", Dimension.class, VariableDeclarationFragment::extraDimensions, matchers);
            return this;
        }

        public final VariableDeclarationFragmentMatcher extraDimensionCountIs(int count) {
            addCountIs(VariableDeclarationFragment::extraDimensions, count);
            return this;
        }

        @SafeVarargs
        public final VariableDeclarationFragmentMatcher hasExtraDimensionAt(int index, Matcher<? extends Dimension>... matchers) {
            addPropertyListElement("extraDimensions", Dimension.class, VariableDeclarationFragment::extraDimensions, index, matchers);
            return this;
        }

        @SafeVarargs
        public final VariableDeclarationFragmentMatcher hasInitializer(Matcher<? extends Expression>... matchers) {
            addProperty("initializer", Expression.class, VariableDeclarationFragment::getInitializer, matchers);
            return this;
        }

        public final VariableDeclarationFragmentMatcher unlessHasInitializer() {
            addPredicate("initializer", n -> n.getInitializer() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node EnumDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class EnumDeclarationMatcher
        extends CommonMatcher<EnumDeclaration, EnumDeclarationMatcher> {

        EnumDeclarationMatcher() {
            super(EnumDeclaration.class);
        }

        @SafeVarargs
        public final EnumDeclarationMatcher hasJavadoc(Matcher<? extends Javadoc>... matchers) {
            addProperty("javadoc", Javadoc.class, EnumDeclaration::getJavadoc, matchers);
            return this;
        }

        public final EnumDeclarationMatcher unlessHasJavadoc() {
            addPredicate("javadoc", n -> n.getJavadoc() == null);
            return this;
        }

        public final EnumDeclarationMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final EnumDeclarationMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, EnumDeclaration::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final EnumDeclarationMatcher hasSuperInterfaceType(Matcher<? extends Type>... matchers) {
            addPropertyList("superInterfaceTypes", Type.class, EnumDeclaration::superInterfaceTypes, matchers);
            return this;
        }

        public final EnumDeclarationMatcher superInterfaceTypeCountIs(int count) {
            addCountIs(EnumDeclaration::superInterfaceTypes, count);
            return this;
        }

        @SafeVarargs
        public final EnumDeclarationMatcher hasSuperInterfaceTypeAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("superInterfaceTypes", Type.class, EnumDeclaration::superInterfaceTypes, index, matchers);
            return this;
        }

        @SafeVarargs
        public final EnumDeclarationMatcher hasEnumConstant(Matcher<? extends EnumConstantDeclaration>... matchers) {
            addPropertyList("enumConstants", EnumConstantDeclaration.class, EnumDeclaration::enumConstants, matchers);
            return this;
        }

        public final EnumDeclarationMatcher enumConstantCountIs(int count) {
            addCountIs(EnumDeclaration::enumConstants, count);
            return this;
        }

        @SafeVarargs
        public final EnumDeclarationMatcher hasEnumConstantAt(int index, Matcher<? extends EnumConstantDeclaration>... matchers) {
            addPropertyListElement("enumConstants", EnumConstantDeclaration.class, EnumDeclaration::enumConstants, index, matchers);
            return this;
        }

        @SafeVarargs
        public final EnumDeclarationMatcher hasBodyDeclaration(Matcher<? extends BodyDeclaration>... matchers) {
            addPropertyList("bodyDeclarations", BodyDeclaration.class, EnumDeclaration::bodyDeclarations, matchers);
            return this;
        }

        public final EnumDeclarationMatcher bodyDeclarationCountIs(int count) {
            addCountIs(EnumDeclaration::bodyDeclarations, count);
            return this;
        }

        @SafeVarargs
        public final EnumDeclarationMatcher hasBodyDeclarationAt(int index, Matcher<? extends BodyDeclaration>... matchers) {
            addPropertyListElement("bodyDeclarations", BodyDeclaration.class, EnumDeclaration::bodyDeclarations, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node BlockComment.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class BlockCommentMatcher
        extends CommonMatcher<BlockComment, BlockCommentMatcher> {

        BlockCommentMatcher() {
            super(BlockComment.class);
        }
    }

    /**
     * Matcher builder for AST node ConditionalExpression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ConditionalExpressionMatcher
        extends ExpressionMatcherSupport<ConditionalExpression, ConditionalExpressionMatcher> {

        ConditionalExpressionMatcher() {
            super(ConditionalExpression.class);
        }

        @SafeVarargs
        public final ConditionalExpressionMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, ConditionalExpression::getExpression, matchers);
            return this;
        }

        @SafeVarargs
        public final ConditionalExpressionMatcher hasThenExpression(Matcher<? extends Expression>... matchers) {
            addProperty("thenExpression", Expression.class, ConditionalExpression::getThenExpression, matchers);
            return this;
        }

        @SafeVarargs
        public final ConditionalExpressionMatcher hasElseExpression(Matcher<? extends Expression>... matchers) {
            addProperty("elseExpression", Expression.class, ConditionalExpression::getElseExpression, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ASTNode.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ASTNodeMatcher
        extends CommonMatcher<ASTNode, ASTNodeMatcher> {

        ASTNodeMatcher() {
            super(ASTNode.class);
        }
    }

    /**
     * Matcher builder for AST node BooleanLiteral.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class BooleanLiteralMatcher
        extends ExpressionMatcherSupport<BooleanLiteral, BooleanLiteralMatcher> {

        BooleanLiteralMatcher() {
            super(BooleanLiteral.class);
        }

        public final BooleanLiteralMatcher isEqualTo(boolean b) {
            addPredicate(n -> n.booleanValue() == b);
            return this;
        }

        public final BooleanLiteralMatcher isEqualTo(Boolean b) {
            addPredicate(n -> b != null && n.booleanValue() == b);
            return this;
        }
    }

    /**
     * Matcher builder for AST node Name.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class NameMatcher
        extends ExpressionMatcherSupport<Name, NameMatcher> {

        NameMatcher() {
            super(Name.class);
        }
    }

    /**
     * Matcher builder for AST node ArrayType.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ArrayTypeMatcher
        extends CommonMatcher<ArrayType, ArrayTypeMatcher> {

        ArrayTypeMatcher() {
            super(ArrayType.class);
        }

        @SafeVarargs
        public final ArrayTypeMatcher hasElementType(Matcher<? extends Type>... matchers) {
            addProperty("elementType", Type.class, ArrayType::getElementType, matchers);
            return this;
        }

        @SafeVarargs
        public final ArrayTypeMatcher hasDimension(Matcher<? extends Dimension>... matchers) {
            addPropertyList("dimensions", Dimension.class, ArrayType::dimensions, matchers);
            return this;
        }

        public final ArrayTypeMatcher dimensionCountIs(int count) {
            addCountIs(ArrayType::dimensions, count);
            return this;
        }

        @SafeVarargs
        public final ArrayTypeMatcher hasDimensionAt(int index, Matcher<? extends Dimension>... matchers) {
            addPropertyListElement("dimensions", Dimension.class, ArrayType::dimensions, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node Javadoc.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class JavadocMatcher
        extends CommonMatcher<Javadoc, JavadocMatcher> {

        JavadocMatcher() {
            super(Javadoc.class);
        }

        @SafeVarargs
        public final JavadocMatcher hasTag(Matcher<? extends TagElement>... matchers) {
            addPropertyList("tags", TagElement.class, Javadoc::tags, matchers);
            return this;
        }

        public final JavadocMatcher tagCountIs(int count) {
            addCountIs(Javadoc::tags, count);
            return this;
        }

        @SafeVarargs
        public final JavadocMatcher hasTagAt(int index, Matcher<? extends TagElement>... matchers) {
            addPropertyListElement("tags", TagElement.class, Javadoc::tags, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ClassInstanceCreation.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ClassInstanceCreationMatcher
        extends ExpressionMatcherSupport<ClassInstanceCreation, ClassInstanceCreationMatcher> {

        ClassInstanceCreationMatcher() {
            super(ClassInstanceCreation.class);
        }

        @SafeVarargs
        public final ClassInstanceCreationMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, ClassInstanceCreation::getExpression, matchers);
            return this;
        }

        public final ClassInstanceCreationMatcher unlessHasExpression() {
            addPredicate("expression", n -> n.getExpression() == null);
            return this;
        }

        @SafeVarargs
        public final ClassInstanceCreationMatcher hasTypeArgument(Matcher<? extends Type>... matchers) {
            addPropertyList("typeArguments", Type.class, ClassInstanceCreation::typeArguments, matchers);
            return this;
        }

        public final ClassInstanceCreationMatcher typeArgumentCountIs(int count) {
            addCountIs(ClassInstanceCreation::typeArguments, count);
            return this;
        }

        @SafeVarargs
        public final ClassInstanceCreationMatcher hasTypeArgumentAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeArguments", Type.class, ClassInstanceCreation::typeArguments, index, matchers);
            return this;
        }

        @SafeVarargs
        public final ClassInstanceCreationMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, ClassInstanceCreation::getType, matchers);
            return this;
        }

        @SafeVarargs
        public final ClassInstanceCreationMatcher hasArgument(Matcher<? extends Expression>... matchers) {
            addPropertyList("arguments", Expression.class, ClassInstanceCreation::arguments, matchers);
            return this;
        }

        public final ClassInstanceCreationMatcher argumentCountIs(int count) {
            addCountIs(ClassInstanceCreation::arguments, count);
            return this;
        }

        @SafeVarargs
        public final ClassInstanceCreationMatcher hasArgumentAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("arguments", Expression.class, ClassInstanceCreation::arguments, index, matchers);
            return this;
        }

        @SafeVarargs
        public final ClassInstanceCreationMatcher hasAnonymousClassDeclaration(Matcher<? extends AnonymousClassDeclaration>... matchers) {
            addProperty("anonymousClassDeclaration", AnonymousClassDeclaration.class, ClassInstanceCreation::getAnonymousClassDeclaration, matchers);
            return this;
        }

        public final ClassInstanceCreationMatcher unlessHasAnonymousClassDeclaration() {
            addPredicate("anonymousClassDeclaration", n -> n.getAnonymousClassDeclaration() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node EnumConstantDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class EnumConstantDeclarationMatcher
        extends CommonMatcher<EnumConstantDeclaration, EnumConstantDeclarationMatcher> {

        EnumConstantDeclarationMatcher() {
            super(EnumConstantDeclaration.class);
        }

        @SafeVarargs
        public final EnumConstantDeclarationMatcher hasJavadoc(Matcher<? extends Javadoc>... matchers) {
            addProperty("javadoc", Javadoc.class, EnumConstantDeclaration::getJavadoc, matchers);
            return this;
        }

        public final EnumConstantDeclarationMatcher unlessHasJavadoc() {
            addPredicate("javadoc", n -> n.getJavadoc() == null);
            return this;
        }

        public final EnumConstantDeclarationMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final EnumConstantDeclarationMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, EnumConstantDeclaration::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final EnumConstantDeclarationMatcher hasArgument(Matcher<? extends Expression>... matchers) {
            addPropertyList("arguments", Expression.class, EnumConstantDeclaration::arguments, matchers);
            return this;
        }

        public final EnumConstantDeclarationMatcher argumentCountIs(int count) {
            addCountIs(EnumConstantDeclaration::arguments, count);
            return this;
        }

        @SafeVarargs
        public final EnumConstantDeclarationMatcher hasArgumentAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("arguments", Expression.class, EnumConstantDeclaration::arguments, index, matchers);
            return this;
        }

        @SafeVarargs
        public final EnumConstantDeclarationMatcher hasAnonymousClassDeclaration(Matcher<? extends AnonymousClassDeclaration>... matchers) {
            addProperty("anonymousClassDeclaration", AnonymousClassDeclaration.class, EnumConstantDeclaration::getAnonymousClassDeclaration, matchers);
            return this;
        }

        public final EnumConstantDeclarationMatcher unlessHasAnonymousClassDeclaration() {
            addPredicate("anonymousClassDeclaration", n -> n.getAnonymousClassDeclaration() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node TryStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class TryStatementMatcher
        extends StatementMatcherSupport<TryStatement, TryStatementMatcher> {

        TryStatementMatcher() {
            super(TryStatement.class);
        }

        @SafeVarargs
        public final TryStatementMatcher hasResource(Matcher<? extends VariableDeclarationExpression>... matchers) {
            addPropertyList("resources", VariableDeclarationExpression.class, TryStatement::resources, matchers);
            return this;
        }

        public final TryStatementMatcher resourceCountIs(int count) {
            addCountIs(TryStatement::resources, count);
            return this;
        }

        @SafeVarargs
        public final TryStatementMatcher hasResourceAt(int index, Matcher<? extends VariableDeclarationExpression>... matchers) {
            addPropertyListElement("resources", VariableDeclarationExpression.class, TryStatement::resources, index, matchers);
            return this;
        }

        @SafeVarargs
        public final TryStatementMatcher hasBody(Matcher<? extends Block>... matchers) {
            addProperty("body", Block.class, TryStatement::getBody, matchers);
            return this;
        }

        @SafeVarargs
        public final TryStatementMatcher hasCatchClause(Matcher<? extends CatchClause>... matchers) {
            addPropertyList("catchClauses", CatchClause.class, TryStatement::catchClauses, matchers);
            return this;
        }

        public final TryStatementMatcher catchClauseCountIs(int count) {
            addCountIs(TryStatement::catchClauses, count);
            return this;
        }

        @SafeVarargs
        public final TryStatementMatcher hasCatchClauseAt(int index, Matcher<? extends CatchClause>... matchers) {
            addPropertyListElement("catchClauses", CatchClause.class, TryStatement::catchClauses, index, matchers);
            return this;
        }

        @SafeVarargs
        public final TryStatementMatcher hasFinally(Matcher<? extends Block>... matchers) {
            addProperty("finally", Block.class, TryStatement::getFinally, matchers);
            return this;
        }

        public final TryStatementMatcher unlessHasFinally() {
            addPredicate("finally", n -> n.getFinally() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node AnnotatableType.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class AnnotatableTypeMatcher
        extends CommonMatcher<AnnotatableType, AnnotatableTypeMatcher> {

        AnnotatableTypeMatcher() {
            super(AnnotatableType.class);
        }
    }

    /**
     * Matcher builder for AST node ReturnStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ReturnStatementMatcher
        extends StatementMatcherSupport<ReturnStatement, ReturnStatementMatcher> {

        ReturnStatementMatcher() {
            super(ReturnStatement.class);
        }

        @SafeVarargs
        public final ReturnStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, ReturnStatement::getExpression, matchers);
            return this;
        }

        public final ReturnStatementMatcher unlessHasExpression() {
            addPredicate("expression", n -> n.getExpression() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node BodyDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class BodyDeclarationMatcher
        extends CommonMatcher<BodyDeclaration, BodyDeclarationMatcher> {

        BodyDeclarationMatcher() {
            super(BodyDeclaration.class);
        }
    }

    /**
     * Matcher builder for AST node SingleMemberAnnotation.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SingleMemberAnnotationMatcher
        extends ExpressionMatcherSupport<SingleMemberAnnotation, SingleMemberAnnotationMatcher> {

        SingleMemberAnnotationMatcher() {
            super(SingleMemberAnnotation.class);
        }

        @SafeVarargs
        public final SingleMemberAnnotationMatcher hasTypeName(Matcher<? extends Name>... matchers) {
            addProperty("typeName", Name.class, SingleMemberAnnotation::getTypeName, matchers);
            return this;
        }

        @SafeVarargs
        public final SingleMemberAnnotationMatcher hasValue(Matcher<? extends Expression>... matchers) {
            addProperty("value", Expression.class, SingleMemberAnnotation::getValue, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node TypeLiteral.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class TypeLiteralMatcher
        extends ExpressionMatcherSupport<TypeLiteral, TypeLiteralMatcher> {

        TypeLiteralMatcher() {
            super(TypeLiteral.class);
        }

        @SafeVarargs
        public final TypeLiteralMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, TypeLiteral::getType, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node VariableDeclarationStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class VariableDeclarationStatementMatcher
        extends StatementMatcherSupport<VariableDeclarationStatement, VariableDeclarationStatementMatcher> {

        VariableDeclarationStatementMatcher() {
            super(VariableDeclarationStatement.class);
        }

        @SafeVarargs
        public final VariableDeclarationStatementMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, VariableDeclarationStatement::getType, matchers);
            return this;
        }

        @SafeVarargs
        public final VariableDeclarationStatementMatcher hasFragment(Matcher<? extends VariableDeclarationFragment>... matchers) {
            addPropertyList("fragments", VariableDeclarationFragment.class, VariableDeclarationStatement::fragments, matchers);
            return this;
        }

        public final VariableDeclarationStatementMatcher fragmentCountIs(int count) {
            addCountIs(VariableDeclarationStatement::fragments, count);
            return this;
        }

        @SafeVarargs
        public final VariableDeclarationStatementMatcher hasFragmentAt(int index, Matcher<? extends VariableDeclarationFragment>... matchers) {
            addPropertyListElement("fragments", VariableDeclarationFragment.class, VariableDeclarationStatement::fragments, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node AnonymousClassDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class AnonymousClassDeclarationMatcher
        extends CommonMatcher<AnonymousClassDeclaration, AnonymousClassDeclarationMatcher> {

        AnonymousClassDeclarationMatcher() {
            super(AnonymousClassDeclaration.class);
        }

        @SafeVarargs
        public final AnonymousClassDeclarationMatcher hasBodyDeclaration(Matcher<? extends BodyDeclaration>... matchers) {
            addPropertyList("bodyDeclarations", BodyDeclaration.class, AnonymousClassDeclaration::bodyDeclarations, matchers);
            return this;
        }

        public final AnonymousClassDeclarationMatcher bodyDeclarationCountIs(int count) {
            addCountIs(AnonymousClassDeclaration::bodyDeclarations, count);
            return this;
        }

        @SafeVarargs
        public final AnonymousClassDeclarationMatcher hasBodyDeclarationAt(int index, Matcher<? extends BodyDeclaration>... matchers) {
            addPropertyListElement("bodyDeclarations", BodyDeclaration.class, AnonymousClassDeclaration::bodyDeclarations, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node VariableDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class VariableDeclarationMatcher
        extends CommonMatcher<VariableDeclaration, VariableDeclarationMatcher> {

        VariableDeclarationMatcher() {
            super(VariableDeclaration.class);
        }
    }

    /**
     * Matcher builder for AST node EmptyStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class EmptyStatementMatcher
        extends StatementMatcherSupport<EmptyStatement, EmptyStatementMatcher> {

        EmptyStatementMatcher() {
            super(EmptyStatement.class);
        }
    }

    /**
     * Matcher builder for AST node Initializer.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class InitializerMatcher
        extends CommonMatcher<Initializer, InitializerMatcher> {

        InitializerMatcher() {
            super(Initializer.class);
        }

        @SafeVarargs
        public final InitializerMatcher hasJavadoc(Matcher<? extends Javadoc>... matchers) {
            addProperty("javadoc", Javadoc.class, Initializer::getJavadoc, matchers);
            return this;
        }

        public final InitializerMatcher unlessHasJavadoc() {
            addPredicate("javadoc", n -> n.getJavadoc() == null);
            return this;
        }

        @SafeVarargs
        public final InitializerMatcher hasBody(Matcher<? extends Block>... matchers) {
            addProperty("body", Block.class, Initializer::getBody, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node TypeDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class TypeDeclarationMatcher
        extends CommonMatcher<TypeDeclaration, TypeDeclarationMatcher> {

        TypeDeclarationMatcher() {
            super(TypeDeclaration.class);
        }

        @SafeVarargs
        public final TypeDeclarationMatcher hasJavadoc(Matcher<? extends Javadoc>... matchers) {
            addProperty("javadoc", Javadoc.class, TypeDeclaration::getJavadoc, matchers);
            return this;
        }

        public final TypeDeclarationMatcher unlessHasJavadoc() {
            addPredicate("javadoc", n -> n.getJavadoc() == null);
            return this;
        }

        public final TypeDeclarationMatcher isInterface() {
            addPredicate("interface", n -> n.isInterface());
            return this;
        }

        public final TypeDeclarationMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final TypeDeclarationMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, TypeDeclaration::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final TypeDeclarationMatcher hasTypeParameter(Matcher<? extends TypeParameter>... matchers) {
            addPropertyList("typeParameters", TypeParameter.class, TypeDeclaration::typeParameters, matchers);
            return this;
        }

        public final TypeDeclarationMatcher typeParameterCountIs(int count) {
            addCountIs(TypeDeclaration::typeParameters, count);
            return this;
        }

        @SafeVarargs
        public final TypeDeclarationMatcher hasTypeParameterAt(int index, Matcher<? extends TypeParameter>... matchers) {
            addPropertyListElement("typeParameters", TypeParameter.class, TypeDeclaration::typeParameters, index, matchers);
            return this;
        }

        @SafeVarargs
        public final TypeDeclarationMatcher hasSuperclassType(Matcher<? extends Type>... matchers) {
            addProperty("superclassType", Type.class, TypeDeclaration::getSuperclassType, matchers);
            return this;
        }

        public final TypeDeclarationMatcher unlessHasSuperclassType() {
            addPredicate("superclassType", n -> n.getSuperclassType() == null);
            return this;
        }

        @SafeVarargs
        public final TypeDeclarationMatcher hasSuperInterfaceType(Matcher<? extends Type>... matchers) {
            addPropertyList("superInterfaceTypes", Type.class, TypeDeclaration::superInterfaceTypes, matchers);
            return this;
        }

        public final TypeDeclarationMatcher superInterfaceTypeCountIs(int count) {
            addCountIs(TypeDeclaration::superInterfaceTypes, count);
            return this;
        }

        @SafeVarargs
        public final TypeDeclarationMatcher hasSuperInterfaceTypeAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("superInterfaceTypes", Type.class, TypeDeclaration::superInterfaceTypes, index, matchers);
            return this;
        }

        @SafeVarargs
        public final TypeDeclarationMatcher hasBodyDeclaration(Matcher<? extends BodyDeclaration>... matchers) {
            addPropertyList("bodyDeclarations", BodyDeclaration.class, TypeDeclaration::bodyDeclarations, matchers);
            return this;
        }

        public final TypeDeclarationMatcher bodyDeclarationCountIs(int count) {
            addCountIs(TypeDeclaration::bodyDeclarations, count);
            return this;
        }

        @SafeVarargs
        public final TypeDeclarationMatcher hasBodyDeclarationAt(int index, Matcher<? extends BodyDeclaration>... matchers) {
            addPropertyListElement("bodyDeclarations", BodyDeclaration.class, TypeDeclaration::bodyDeclarations, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ConstructorInvocation.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ConstructorInvocationMatcher
        extends StatementMatcherSupport<ConstructorInvocation, ConstructorInvocationMatcher> {

        ConstructorInvocationMatcher() {
            super(ConstructorInvocation.class);
        }

        @SafeVarargs
        public final ConstructorInvocationMatcher hasTypeArgument(Matcher<? extends Type>... matchers) {
            addPropertyList("typeArguments", Type.class, ConstructorInvocation::typeArguments, matchers);
            return this;
        }

        public final ConstructorInvocationMatcher typeArgumentCountIs(int count) {
            addCountIs(ConstructorInvocation::typeArguments, count);
            return this;
        }

        @SafeVarargs
        public final ConstructorInvocationMatcher hasTypeArgumentAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeArguments", Type.class, ConstructorInvocation::typeArguments, index, matchers);
            return this;
        }

        @SafeVarargs
        public final ConstructorInvocationMatcher hasArgument(Matcher<? extends Expression>... matchers) {
            addPropertyList("arguments", Expression.class, ConstructorInvocation::arguments, matchers);
            return this;
        }

        public final ConstructorInvocationMatcher argumentCountIs(int count) {
            addCountIs(ConstructorInvocation::arguments, count);
            return this;
        }

        @SafeVarargs
        public final ConstructorInvocationMatcher hasArgumentAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("arguments", Expression.class, ConstructorInvocation::arguments, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node QualifiedName.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class QualifiedNameMatcher
        extends ExpressionMatcherSupport<QualifiedName, QualifiedNameMatcher> {

        QualifiedNameMatcher() {
            super(QualifiedName.class);
        }

        @SafeVarargs
        public final QualifiedNameMatcher hasQualifier(Matcher<? extends Name>... matchers) {
            addProperty("qualifier", Name.class, QualifiedName::getQualifier, matchers);
            return this;
        }

        public final QualifiedNameMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final QualifiedNameMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, QualifiedName::getName, matchers);
            return this;
        }

        public final QualifiedNameMatcher isArrayLength() {
            addPredicate(n ->
                ASTHelper.isArray(n.getQualifier())
                    && n.getName().getIdentifier().equals("length"));
            return this;
        }
    }

    /**
     * Matcher builder for AST node UnionType.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class UnionTypeMatcher
        extends CommonMatcher<UnionType, UnionTypeMatcher> {

        UnionTypeMatcher() {
            super(UnionType.class);
        }

        @SafeVarargs
        public final UnionTypeMatcher hasType(Matcher<? extends Type>... matchers) {
            addPropertyList("types", Type.class, UnionType::types, matchers);
            return this;
        }

        public final UnionTypeMatcher typeCountIs(int count) {
            addCountIs(UnionType::types, count);
            return this;
        }

        @SafeVarargs
        public final UnionTypeMatcher hasTypeAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("types", Type.class, UnionType::types, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ThisExpression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ThisExpressionMatcher
        extends ExpressionMatcherSupport<ThisExpression, ThisExpressionMatcher> {

        ThisExpressionMatcher() {
            super(ThisExpression.class);
        }

        @SafeVarargs
        public final ThisExpressionMatcher hasQualifier(Matcher<? extends Name>... matchers) {
            addProperty("qualifier", Name.class, ThisExpression::getQualifier, matchers);
            return this;
        }

        public final ThisExpressionMatcher unlessHasQualifier() {
            addPredicate("qualifier", n -> n.getQualifier() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node EnhancedForStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class EnhancedForStatementMatcher
        extends StatementMatcherSupport<EnhancedForStatement, EnhancedForStatementMatcher> {

        EnhancedForStatementMatcher() {
            super(EnhancedForStatement.class);
        }

        @SafeVarargs
        public final EnhancedForStatementMatcher hasParameter(Matcher<? extends SingleVariableDeclaration>... matchers) {
            addProperty("parameter", SingleVariableDeclaration.class, EnhancedForStatement::getParameter, matchers);
            return this;
        }

        @SafeVarargs
        public final EnhancedForStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, EnhancedForStatement::getExpression, matchers);
            return this;
        }

        @SafeVarargs
        public final EnhancedForStatementMatcher hasBody(Matcher<? extends Statement>... matchers) {
            addProperty("body", Statement.class, EnhancedForStatement::getBody, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node NormalAnnotation.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class NormalAnnotationMatcher
        extends ExpressionMatcherSupport<NormalAnnotation, NormalAnnotationMatcher> {

        NormalAnnotationMatcher() {
            super(NormalAnnotation.class);
        }

        @SafeVarargs
        public final NormalAnnotationMatcher hasTypeName(Matcher<? extends Name>... matchers) {
            addProperty("typeName", Name.class, NormalAnnotation::getTypeName, matchers);
            return this;
        }

        @SafeVarargs
        public final NormalAnnotationMatcher hasValue(Matcher<? extends MemberValuePair>... matchers) {
            addPropertyList("values", MemberValuePair.class, NormalAnnotation::values, matchers);
            return this;
        }

        public final NormalAnnotationMatcher valueCountIs(int count) {
            addCountIs(NormalAnnotation::values, count);
            return this;
        }

        @SafeVarargs
        public final NormalAnnotationMatcher hasValueAt(int index, Matcher<? extends MemberValuePair>... matchers) {
            addPropertyListElement("values", MemberValuePair.class, NormalAnnotation::values, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node IntersectionType.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class IntersectionTypeMatcher
        extends CommonMatcher<IntersectionType, IntersectionTypeMatcher> {

        IntersectionTypeMatcher() {
            super(IntersectionType.class);
        }

        @SafeVarargs
        public final IntersectionTypeMatcher hasType(Matcher<? extends Type>... matchers) {
            addPropertyList("types", Type.class, IntersectionType::types, matchers);
            return this;
        }

        public final IntersectionTypeMatcher typeCountIs(int count) {
            addCountIs(IntersectionType::types, count);
            return this;
        }

        @SafeVarargs
        public final IntersectionTypeMatcher hasTypeAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("types", Type.class, IntersectionType::types, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node SimpleType.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SimpleTypeMatcher
        extends CommonMatcher<SimpleType, SimpleTypeMatcher> {

        SimpleTypeMatcher() {
            super(SimpleType.class);
        }

        @SafeVarargs
        public final SimpleTypeMatcher hasAnnotation(Matcher<? extends Annotation>... matchers) {
            addPropertyList("annotations", Annotation.class, SimpleType::annotations, matchers);
            return this;
        }

        public final SimpleTypeMatcher annotationCountIs(int count) {
            addCountIs(SimpleType::annotations, count);
            return this;
        }

        @SafeVarargs
        public final SimpleTypeMatcher hasAnnotationAt(int index, Matcher<? extends Annotation>... matchers) {
            addPropertyListElement("annotations", Annotation.class, SimpleType::annotations, index, matchers);
            return this;
        }

        public final SimpleTypeMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final SimpleTypeMatcher hasName(Matcher<? extends Name>... matchers) {
            addProperty("name", Name.class, SimpleType::getName, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node AssertStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class AssertStatementMatcher
        extends StatementMatcherSupport<AssertStatement, AssertStatementMatcher> {

        AssertStatementMatcher() {
            super(AssertStatement.class);
        }

        @SafeVarargs
        public final AssertStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, AssertStatement::getExpression, matchers);
            return this;
        }

        @SafeVarargs
        public final AssertStatementMatcher hasMessage(Matcher<? extends Expression>... matchers) {
            addProperty("message", Expression.class, AssertStatement::getMessage, matchers);
            return this;
        }

        public final AssertStatementMatcher unlessHasMessage() {
            addPredicate("message", n -> n.getMessage() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node SynchronizedStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SynchronizedStatementMatcher
        extends StatementMatcherSupport<SynchronizedStatement, SynchronizedStatementMatcher> {

        SynchronizedStatementMatcher() {
            super(SynchronizedStatement.class);
        }

        @SafeVarargs
        public final SynchronizedStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, SynchronizedStatement::getExpression, matchers);
            return this;
        }

        @SafeVarargs
        public final SynchronizedStatementMatcher hasBody(Matcher<? extends Block>... matchers) {
            addProperty("body", Block.class, SynchronizedStatement::getBody, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node DoStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class DoStatementMatcher
        extends StatementMatcherSupport<DoStatement, DoStatementMatcher> {

        DoStatementMatcher() {
            super(DoStatement.class);
        }

        @SafeVarargs
        public final DoStatementMatcher hasBody(Matcher<? extends Statement>... matchers) {
            addProperty("body", Statement.class, DoStatement::getBody, matchers);
            return this;
        }

        @SafeVarargs
        public final DoStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, DoStatement::getExpression, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node NumberLiteral.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class NumberLiteralMatcher
        extends ExpressionMatcherSupport<NumberLiteral, NumberLiteralMatcher> {

        NumberLiteralMatcher() {
            super(NumberLiteral.class);
        }

        public final NumberLiteralMatcher hasToken(String v) {
            addPredicate("token", n -> v != null && v.equals(n.getToken()));
            return this;
        }
    }

    /**
     * Matcher builder for AST node Expression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ExpressionMatcher
        extends ExpressionMatcherSupport<Expression, ExpressionMatcher> {

        ExpressionMatcher() {
            super(Expression.class);
        }
    }

    /**
     * Matcher builder for AST node Dimension.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class DimensionMatcher
        extends CommonMatcher<Dimension, DimensionMatcher> {

        DimensionMatcher() {
            super(Dimension.class);
        }

        @SafeVarargs
        public final DimensionMatcher hasAnnotation(Matcher<? extends Annotation>... matchers) {
            addPropertyList("annotations", Annotation.class, Dimension::annotations, matchers);
            return this;
        }

        public final DimensionMatcher annotationCountIs(int count) {
            addCountIs(Dimension::annotations, count);
            return this;
        }

        @SafeVarargs
        public final DimensionMatcher hasAnnotationAt(int index, Matcher<? extends Annotation>... matchers) {
            addPropertyListElement("annotations", Annotation.class, Dimension::annotations, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node NullLiteral.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class NullLiteralMatcher
        extends ExpressionMatcherSupport<NullLiteral, NullLiteralMatcher> {

        NullLiteralMatcher() {
            super(NullLiteral.class);
        }
    }

    /**
     * Matcher builder for AST node MemberRef.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class MemberRefMatcher
        extends CommonMatcher<MemberRef, MemberRefMatcher> {

        MemberRefMatcher() {
            super(MemberRef.class);
        }

        @SafeVarargs
        public final MemberRefMatcher hasQualifier(Matcher<? extends Name>... matchers) {
            addProperty("qualifier", Name.class, MemberRef::getQualifier, matchers);
            return this;
        }

        public final MemberRefMatcher unlessHasQualifier() {
            addPredicate("qualifier", n -> n.getQualifier() == null);
            return this;
        }

        public final MemberRefMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final MemberRefMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, MemberRef::getName, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ExpressionMethodReference.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ExpressionMethodReferenceMatcher
        extends ExpressionMatcherSupport<ExpressionMethodReference, ExpressionMethodReferenceMatcher> {

        ExpressionMethodReferenceMatcher() {
            super(ExpressionMethodReference.class);
        }

        @SafeVarargs
        public final ExpressionMethodReferenceMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, ExpressionMethodReference::getExpression, matchers);
            return this;
        }

        @SafeVarargs
        public final ExpressionMethodReferenceMatcher hasTypeArgument(Matcher<? extends Type>... matchers) {
            addPropertyList("typeArguments", Type.class, ExpressionMethodReference::typeArguments, matchers);
            return this;
        }

        public final ExpressionMethodReferenceMatcher typeArgumentCountIs(int count) {
            addCountIs(ExpressionMethodReference::typeArguments, count);
            return this;
        }

        @SafeVarargs
        public final ExpressionMethodReferenceMatcher hasTypeArgumentAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeArguments", Type.class, ExpressionMethodReference::typeArguments, index, matchers);
            return this;
        }

        public final ExpressionMethodReferenceMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final ExpressionMethodReferenceMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, ExpressionMethodReference::getName, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node TypeParameter.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class TypeParameterMatcher
        extends CommonMatcher<TypeParameter, TypeParameterMatcher> {

        TypeParameterMatcher() {
            super(TypeParameter.class);
        }

        public final TypeParameterMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final TypeParameterMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, TypeParameter::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final TypeParameterMatcher hasTypeBound(Matcher<? extends Type>... matchers) {
            addPropertyList("typeBounds", Type.class, TypeParameter::typeBounds, matchers);
            return this;
        }

        public final TypeParameterMatcher typeBoundCountIs(int count) {
            addCountIs(TypeParameter::typeBounds, count);
            return this;
        }

        @SafeVarargs
        public final TypeParameterMatcher hasTypeBoundAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeBounds", Type.class, TypeParameter::typeBounds, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node MethodRef.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class MethodRefMatcher
        extends CommonMatcher<MethodRef, MethodRefMatcher> {

        MethodRefMatcher() {
            super(MethodRef.class);
        }

        @SafeVarargs
        public final MethodRefMatcher hasQualifier(Matcher<? extends Name>... matchers) {
            addProperty("qualifier", Name.class, MethodRef::getQualifier, matchers);
            return this;
        }

        public final MethodRefMatcher unlessHasQualifier() {
            addPredicate("qualifier", n -> n.getQualifier() == null);
            return this;
        }

        public final MethodRefMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final MethodRefMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, MethodRef::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final MethodRefMatcher hasParameter(Matcher<? extends MethodRefParameter>... matchers) {
            addPropertyList("parameters", MethodRefParameter.class, MethodRef::parameters, matchers);
            return this;
        }

        public final MethodRefMatcher parameterCountIs(int count) {
            addCountIs(MethodRef::parameters, count);
            return this;
        }

        @SafeVarargs
        public final MethodRefMatcher hasParameterAt(int index, Matcher<? extends MethodRefParameter>... matchers) {
            addPropertyListElement("parameters", MethodRefParameter.class, MethodRef::parameters, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node SuperConstructorInvocation.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SuperConstructorInvocationMatcher
        extends StatementMatcherSupport<SuperConstructorInvocation, SuperConstructorInvocationMatcher> {

        SuperConstructorInvocationMatcher() {
            super(SuperConstructorInvocation.class);
        }

        @SafeVarargs
        public final SuperConstructorInvocationMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, SuperConstructorInvocation::getExpression, matchers);
            return this;
        }

        public final SuperConstructorInvocationMatcher unlessHasExpression() {
            addPredicate("expression", n -> n.getExpression() == null);
            return this;
        }

        @SafeVarargs
        public final SuperConstructorInvocationMatcher hasTypeArgument(Matcher<? extends Type>... matchers) {
            addPropertyList("typeArguments", Type.class, SuperConstructorInvocation::typeArguments, matchers);
            return this;
        }

        public final SuperConstructorInvocationMatcher typeArgumentCountIs(int count) {
            addCountIs(SuperConstructorInvocation::typeArguments, count);
            return this;
        }

        @SafeVarargs
        public final SuperConstructorInvocationMatcher hasTypeArgumentAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeArguments", Type.class, SuperConstructorInvocation::typeArguments, index, matchers);
            return this;
        }

        @SafeVarargs
        public final SuperConstructorInvocationMatcher hasArgument(Matcher<? extends Expression>... matchers) {
            addPropertyList("arguments", Expression.class, SuperConstructorInvocation::arguments, matchers);
            return this;
        }

        public final SuperConstructorInvocationMatcher argumentCountIs(int count) {
            addCountIs(SuperConstructorInvocation::arguments, count);
            return this;
        }

        @SafeVarargs
        public final SuperConstructorInvocationMatcher hasArgumentAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("arguments", Expression.class, SuperConstructorInvocation::arguments, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node WildcardType.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class WildcardTypeMatcher
        extends CommonMatcher<WildcardType, WildcardTypeMatcher> {

        WildcardTypeMatcher() {
            super(WildcardType.class);
        }

        @SafeVarargs
        public final WildcardTypeMatcher hasAnnotation(Matcher<? extends Annotation>... matchers) {
            addPropertyList("annotations", Annotation.class, WildcardType::annotations, matchers);
            return this;
        }

        public final WildcardTypeMatcher annotationCountIs(int count) {
            addCountIs(WildcardType::annotations, count);
            return this;
        }

        @SafeVarargs
        public final WildcardTypeMatcher hasAnnotationAt(int index, Matcher<? extends Annotation>... matchers) {
            addPropertyListElement("annotations", Annotation.class, WildcardType::annotations, index, matchers);
            return this;
        }

        @SafeVarargs
        public final WildcardTypeMatcher hasBound(Matcher<? extends Type>... matchers) {
            addProperty("bound", Type.class, WildcardType::getBound, matchers);
            return this;
        }

        public final WildcardTypeMatcher unlessHasBound() {
            addPredicate("bound", n -> n.getBound() == null);
            return this;
        }

        public final WildcardTypeMatcher isUpperBound() {
            addPredicate("upperBound", n -> n.isUpperBound());
            return this;
        }
    }

    /**
     * Matcher builder for AST node ExpressionStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ExpressionStatementMatcher
        extends StatementMatcherSupport<ExpressionStatement, ExpressionStatementMatcher> {

        ExpressionStatementMatcher() {
            super(ExpressionStatement.class);
        }

        @SafeVarargs
        public final ExpressionStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, ExpressionStatement::getExpression, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node CharacterLiteral.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class CharacterLiteralMatcher
        extends ExpressionMatcherSupport<CharacterLiteral, CharacterLiteralMatcher> {

        CharacterLiteralMatcher() {
            super(CharacterLiteral.class);
        }

        public final CharacterLiteralMatcher hasEscapedValue(String v) {
            addPredicate("escapedValue", n -> v != null && v.equals(n.getEscapedValue()));
            return this;
        }
    }

    /**
     * Matcher builder for AST node LambdaExpression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class LambdaExpressionMatcher
        extends ExpressionMatcherSupport<LambdaExpression, LambdaExpressionMatcher> {

        LambdaExpressionMatcher() {
            super(LambdaExpression.class);
        }

        @SafeVarargs
        public final LambdaExpressionMatcher hasParameter(Matcher<? extends VariableDeclaration>... matchers) {
            addPropertyList("parameters", VariableDeclaration.class, LambdaExpression::parameters, matchers);
            return this;
        }

        public final LambdaExpressionMatcher parameterCountIs(int count) {
            addCountIs(LambdaExpression::parameters, count);
            return this;
        }

        @SafeVarargs
        public final LambdaExpressionMatcher hasParameterAt(int index, Matcher<? extends VariableDeclaration>... matchers) {
            addPropertyListElement("parameters", VariableDeclaration.class, LambdaExpression::parameters, index, matchers);
            return this;
        }

        @SafeVarargs
        public final LambdaExpressionMatcher hasBody(Matcher<? extends ASTNode>... matchers) {
            addProperty("body", ASTNode.class, LambdaExpression::getBody, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node WhileStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class WhileStatementMatcher
        extends StatementMatcherSupport<WhileStatement, WhileStatementMatcher> {

        WhileStatementMatcher() {
            super(WhileStatement.class);
        }

        @SafeVarargs
        public final WhileStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, WhileStatement::getExpression, matchers);
            return this;
        }

        @SafeVarargs
        public final WhileStatementMatcher hasBody(Matcher<? extends Statement>... matchers) {
            addProperty("body", Statement.class, WhileStatement::getBody, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node SingleVariableDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SingleVariableDeclarationMatcher
        extends CommonMatcher<SingleVariableDeclaration, SingleVariableDeclarationMatcher> {

        SingleVariableDeclarationMatcher() {
            super(SingleVariableDeclaration.class);
        }

        @SafeVarargs
        public final SingleVariableDeclarationMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, SingleVariableDeclaration::getType, matchers);
            return this;
        }

        @SafeVarargs
        public final SingleVariableDeclarationMatcher hasVarargsAnnotation(Matcher<? extends Annotation>... matchers) {
            addPropertyList("varargsAnnotations", Annotation.class, SingleVariableDeclaration::varargsAnnotations, matchers);
            return this;
        }

        public final SingleVariableDeclarationMatcher varargsAnnotationCountIs(int count) {
            addCountIs(SingleVariableDeclaration::varargsAnnotations, count);
            return this;
        }

        @SafeVarargs
        public final SingleVariableDeclarationMatcher hasVarargsAnnotationAt(int index, Matcher<? extends Annotation>... matchers) {
            addPropertyListElement("varargsAnnotations", Annotation.class, SingleVariableDeclaration::varargsAnnotations, index, matchers);
            return this;
        }

        public final SingleVariableDeclarationMatcher isVarargs() {
            addPredicate("varargs", n -> n.isVarargs());
            return this;
        }

        public final SingleVariableDeclarationMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final SingleVariableDeclarationMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, SingleVariableDeclaration::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final SingleVariableDeclarationMatcher hasExtraDimension(Matcher<? extends Dimension>... matchers) {
            addPropertyList("extraDimensions", Dimension.class, SingleVariableDeclaration::extraDimensions, matchers);
            return this;
        }

        public final SingleVariableDeclarationMatcher extraDimensionCountIs(int count) {
            addCountIs(SingleVariableDeclaration::extraDimensions, count);
            return this;
        }

        @SafeVarargs
        public final SingleVariableDeclarationMatcher hasExtraDimensionAt(int index, Matcher<? extends Dimension>... matchers) {
            addPropertyListElement("extraDimensions", Dimension.class, SingleVariableDeclaration::extraDimensions, index, matchers);
            return this;
        }

        @SafeVarargs
        public final SingleVariableDeclarationMatcher hasInitializer(Matcher<? extends Expression>... matchers) {
            addProperty("initializer", Expression.class, SingleVariableDeclaration::getInitializer, matchers);
            return this;
        }

        public final SingleVariableDeclarationMatcher unlessHasInitializer() {
            addPredicate("initializer", n -> n.getInitializer() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ThrowStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ThrowStatementMatcher
        extends StatementMatcherSupport<ThrowStatement, ThrowStatementMatcher> {

        ThrowStatementMatcher() {
            super(ThrowStatement.class);
        }

        @SafeVarargs
        public final ThrowStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, ThrowStatement::getExpression, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node SuperFieldAccess.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SuperFieldAccessMatcher
        extends ExpressionMatcherSupport<SuperFieldAccess, SuperFieldAccessMatcher> {

        SuperFieldAccessMatcher() {
            super(SuperFieldAccess.class);
        }

        @SafeVarargs
        public final SuperFieldAccessMatcher hasQualifier(Matcher<? extends Name>... matchers) {
            addProperty("qualifier", Name.class, SuperFieldAccess::getQualifier, matchers);
            return this;
        }

        public final SuperFieldAccessMatcher unlessHasQualifier() {
            addPredicate("qualifier", n -> n.getQualifier() == null);
            return this;
        }

        public final SuperFieldAccessMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final SuperFieldAccessMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, SuperFieldAccess::getName, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node MarkerAnnotation.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class MarkerAnnotationMatcher
        extends ExpressionMatcherSupport<MarkerAnnotation, MarkerAnnotationMatcher> {

        MarkerAnnotationMatcher() {
            super(MarkerAnnotation.class);
        }

        @SafeVarargs
        public final MarkerAnnotationMatcher hasTypeName(Matcher<? extends Name>... matchers) {
            addProperty("typeName", Name.class, MarkerAnnotation::getTypeName, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node AnnotationTypeDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class AnnotationTypeDeclarationMatcher
        extends CommonMatcher<AnnotationTypeDeclaration, AnnotationTypeDeclarationMatcher> {

        AnnotationTypeDeclarationMatcher() {
            super(AnnotationTypeDeclaration.class);
        }

        @SafeVarargs
        public final AnnotationTypeDeclarationMatcher hasJavadoc(Matcher<? extends Javadoc>... matchers) {
            addProperty("javadoc", Javadoc.class, AnnotationTypeDeclaration::getJavadoc, matchers);
            return this;
        }

        public final AnnotationTypeDeclarationMatcher unlessHasJavadoc() {
            addPredicate("javadoc", n -> n.getJavadoc() == null);
            return this;
        }

        public final AnnotationTypeDeclarationMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final AnnotationTypeDeclarationMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, AnnotationTypeDeclaration::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final AnnotationTypeDeclarationMatcher hasBodyDeclaration(Matcher<? extends BodyDeclaration>... matchers) {
            addPropertyList("bodyDeclarations", BodyDeclaration.class, AnnotationTypeDeclaration::bodyDeclarations, matchers);
            return this;
        }

        public final AnnotationTypeDeclarationMatcher bodyDeclarationCountIs(int count) {
            addCountIs(AnnotationTypeDeclaration::bodyDeclarations, count);
            return this;
        }

        @SafeVarargs
        public final AnnotationTypeDeclarationMatcher hasBodyDeclarationAt(int index, Matcher<? extends BodyDeclaration>... matchers) {
            addPropertyListElement("bodyDeclarations", BodyDeclaration.class, AnnotationTypeDeclaration::bodyDeclarations, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node Type.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class TypeMatcher
        extends CommonMatcher<Type, TypeMatcher> {

        TypeMatcher() {
            super(Type.class);
        }
    }

    /**
     * Matcher builder for AST node MethodInvocation.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class MethodInvocationMatcher
        extends ExpressionMatcherSupport<MethodInvocation, MethodInvocationMatcher> {

        MethodInvocationMatcher() {
            super(MethodInvocation.class);
        }

        @SafeVarargs
        public final MethodInvocationMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, MethodInvocation::getExpression, matchers);
            return this;
        }

        public final MethodInvocationMatcher unlessHasExpression() {
            addPredicate("expression", n -> n.getExpression() == null);
            return this;
        }

        @SafeVarargs
        public final MethodInvocationMatcher hasTypeArgument(Matcher<? extends Type>... matchers) {
            addPropertyList("typeArguments", Type.class, MethodInvocation::typeArguments, matchers);
            return this;
        }

        public final MethodInvocationMatcher typeArgumentCountIs(int count) {
            addCountIs(MethodInvocation::typeArguments, count);
            return this;
        }

        @SafeVarargs
        public final MethodInvocationMatcher hasTypeArgumentAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeArguments", Type.class, MethodInvocation::typeArguments, index, matchers);
            return this;
        }

        public final MethodInvocationMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final MethodInvocationMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, MethodInvocation::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final MethodInvocationMatcher hasArgument(Matcher<? extends Expression>... matchers) {
            addPropertyList("arguments", Expression.class, MethodInvocation::arguments, matchers);
            return this;
        }

        public final MethodInvocationMatcher argumentCountIs(int count) {
            addCountIs(MethodInvocation::arguments, count);
            return this;
        }

        @SafeVarargs
        public final MethodInvocationMatcher hasArgumentAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("arguments", Expression.class, MethodInvocation::arguments, index, matchers);
            return this;
        }

        public final MethodInvocationMatcher isMethod(String typeQualifiedName,
                String methodName, String... parameterTypesQualifiedNames) {
            addPredicate(n -> ASTHelper.isMethod(n, typeQualifiedName, methodName, parameterTypesQualifiedNames));
            return this;
        }
    }

    /**
     * Matcher builder for AST node MethodDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class MethodDeclarationMatcher
        extends CommonMatcher<MethodDeclaration, MethodDeclarationMatcher> {

        MethodDeclarationMatcher() {
            super(MethodDeclaration.class);
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasJavadoc(Matcher<? extends Javadoc>... matchers) {
            addProperty("javadoc", Javadoc.class, MethodDeclaration::getJavadoc, matchers);
            return this;
        }

        public final MethodDeclarationMatcher unlessHasJavadoc() {
            addPredicate("javadoc", n -> n.getJavadoc() == null);
            return this;
        }

        public final MethodDeclarationMatcher isConstructor() {
            addPredicate("constructor", n -> n.isConstructor());
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasTypeParameter(Matcher<? extends TypeParameter>... matchers) {
            addPropertyList("typeParameters", TypeParameter.class, MethodDeclaration::typeParameters, matchers);
            return this;
        }

        public final MethodDeclarationMatcher typeParameterCountIs(int count) {
            addCountIs(MethodDeclaration::typeParameters, count);
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasTypeParameterAt(int index, Matcher<? extends TypeParameter>... matchers) {
            addPropertyListElement("typeParameters", TypeParameter.class, MethodDeclaration::typeParameters, index, matchers);
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasReturnType(Matcher<? extends Type>... matchers) {
            addProperty("returnType2", Type.class, MethodDeclaration::getReturnType2, matchers);
            return this;
        }

        public final MethodDeclarationMatcher unlessHasReturnType() {
            addPredicate("returnType2", n -> n.getReturnType2() == null);
            return this;
        }

        public final MethodDeclarationMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, MethodDeclaration::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasReceiverType(Matcher<? extends Type>... matchers) {
            addProperty("receiverType", Type.class, MethodDeclaration::getReceiverType, matchers);
            return this;
        }

        public final MethodDeclarationMatcher unlessHasReceiverType() {
            addPredicate("receiverType", n -> n.getReceiverType() == null);
            return this;
        }

        public final MethodDeclarationMatcher hasReceiverQualifier(String name) {
            addPredicate("receiverQualifier", n -> { SimpleName sn = tryCast(n.getReceiverQualifier(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasReceiverQualifier(Matcher<? extends SimpleName>... matchers) {
            addProperty("receiverQualifier", SimpleName.class, MethodDeclaration::getReceiverQualifier, matchers);
            return this;
        }

        public final MethodDeclarationMatcher unlessHasReceiverQualifier() {
            addPredicate("receiverQualifier", n -> n.getReceiverQualifier() == null);
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasParameter(Matcher<? extends SingleVariableDeclaration>... matchers) {
            addPropertyList("parameters", SingleVariableDeclaration.class, MethodDeclaration::parameters, matchers);
            return this;
        }

        public final MethodDeclarationMatcher parameterCountIs(int count) {
            addCountIs(MethodDeclaration::parameters, count);
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasParameterAt(int index, Matcher<? extends SingleVariableDeclaration>... matchers) {
            addPropertyListElement("parameters", SingleVariableDeclaration.class, MethodDeclaration::parameters, index, matchers);
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasExtraDimension(Matcher<? extends Dimension>... matchers) {
            addPropertyList("extraDimensions", Dimension.class, MethodDeclaration::extraDimensions, matchers);
            return this;
        }

        public final MethodDeclarationMatcher extraDimensionCountIs(int count) {
            addCountIs(MethodDeclaration::extraDimensions, count);
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasExtraDimensionAt(int index, Matcher<? extends Dimension>... matchers) {
            addPropertyListElement("extraDimensions", Dimension.class, MethodDeclaration::extraDimensions, index, matchers);
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasThrownExceptionType(Matcher<? extends Type>... matchers) {
            addPropertyList("thrownExceptionTypes", Type.class, MethodDeclaration::thrownExceptionTypes, matchers);
            return this;
        }

        public final MethodDeclarationMatcher thrownExceptionTypeCountIs(int count) {
            addCountIs(MethodDeclaration::thrownExceptionTypes, count);
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasThrownExceptionTypeAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("thrownExceptionTypes", Type.class, MethodDeclaration::thrownExceptionTypes, index, matchers);
            return this;
        }

        @SafeVarargs
        public final MethodDeclarationMatcher hasBody(Matcher<? extends Block>... matchers) {
            addProperty("body", Block.class, MethodDeclaration::getBody, matchers);
            return this;
        }

        public final MethodDeclarationMatcher unlessHasBody() {
            addPredicate("body", n -> n.getBody() == null);
            return this;
        }

		public final MethodDeclarationMatcher hasDeclaringClass(String qualifiedName) {
        	return hasName(AstMatcher.simpleName().hasDeclaringClass(qualifiedName));
		}
    }

    /**
     * Matcher builder for AST node AbstractTypeDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class AbstractTypeDeclarationMatcher
        extends CommonMatcher<AbstractTypeDeclaration, AbstractTypeDeclarationMatcher> {

        AbstractTypeDeclarationMatcher() {
            super(AbstractTypeDeclaration.class);
        }
    }

    /**
     * Matcher builder for AST node TextElement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class TextElementMatcher
        extends CommonMatcher<TextElement, TextElementMatcher> {

        TextElementMatcher() {
            super(TextElement.class);
        }

        public final TextElementMatcher hasText(String v) {
            addPredicate("text", n -> v != null && v.equals(n.getText()));
            return this;
        }
    }

    /**
     * Matcher builder for AST node Modifier.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ModifierMatcher
        extends CommonMatcher<Modifier, ModifierMatcher> {

        ModifierMatcher() {
            super(Modifier.class);
        }
    }

    /**
     * Matcher builder for AST node SwitchCase.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SwitchCaseMatcher
        extends StatementMatcherSupport<SwitchCase, SwitchCaseMatcher> {

        SwitchCaseMatcher() {
            super(SwitchCase.class);
        }

        @SafeVarargs
        public final SwitchCaseMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, SwitchCase::getExpression, matchers);
            return this;
        }

        public final SwitchCaseMatcher unlessHasExpression() {
            addPredicate("expression", n -> n.getExpression() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ArrayAccess.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ArrayAccessMatcher
        extends ExpressionMatcherSupport<ArrayAccess, ArrayAccessMatcher> {

        ArrayAccessMatcher() {
            super(ArrayAccess.class);
        }

        @SafeVarargs
        public final ArrayAccessMatcher hasArray(Matcher<? extends Expression>... matchers) {
            addProperty("array", Expression.class, ArrayAccess::getArray, matchers);
            return this;
        }

        @SafeVarargs
        public final ArrayAccessMatcher hasIndex(Matcher<? extends Expression>... matchers) {
            addProperty("index", Expression.class, ArrayAccess::getIndex, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ParenthesizedExpression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ParenthesizedExpressionMatcher
        extends ExpressionMatcherSupport<ParenthesizedExpression, ParenthesizedExpressionMatcher> {

        ParenthesizedExpressionMatcher() {
            super(ParenthesizedExpression.class);
        }

        @SafeVarargs
        public final ParenthesizedExpressionMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, ParenthesizedExpression::getExpression, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node LabeledStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class LabeledStatementMatcher
        extends StatementMatcherSupport<LabeledStatement, LabeledStatementMatcher> {

        LabeledStatementMatcher() {
            super(LabeledStatement.class);
        }

        public final LabeledStatementMatcher hasLabel(String name) {
            addPredicate("label", n -> { SimpleName sn = tryCast(n.getLabel(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final LabeledStatementMatcher hasLabel(Matcher<? extends SimpleName>... matchers) {
            addProperty("label", SimpleName.class, LabeledStatement::getLabel, matchers);
            return this;
        }

        @SafeVarargs
        public final LabeledStatementMatcher hasBody(Matcher<? extends Statement>... matchers) {
            addProperty("body", Statement.class, LabeledStatement::getBody, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node SwitchStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SwitchStatementMatcher
        extends StatementMatcherSupport<SwitchStatement, SwitchStatementMatcher> {

        SwitchStatementMatcher() {
            super(SwitchStatement.class);
        }

        @SafeVarargs
        public final SwitchStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, SwitchStatement::getExpression, matchers);
            return this;
        }

        @SafeVarargs
        public final SwitchStatementMatcher hasStatement(Matcher<? extends Statement>... matchers) {
            addPropertyList("statements", Statement.class, SwitchStatement::statements, matchers);
            return this;
        }

        public final SwitchStatementMatcher statementCountIs(int count) {
            addCountIs(SwitchStatement::statements, count);
            return this;
        }

        @SafeVarargs
        public final SwitchStatementMatcher hasStatementAt(int index, Matcher<? extends Statement>... matchers) {
            addPropertyListElement("statements", Statement.class, SwitchStatement::statements, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node MethodRefParameter.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class MethodRefParameterMatcher
        extends CommonMatcher<MethodRefParameter, MethodRefParameterMatcher> {

        MethodRefParameterMatcher() {
            super(MethodRefParameter.class);
        }

        @SafeVarargs
        public final MethodRefParameterMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, MethodRefParameter::getType, matchers);
            return this;
        }

        public final MethodRefParameterMatcher isVarargs() {
            addPredicate("varargs", n -> n.isVarargs());
            return this;
        }

        public final MethodRefParameterMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final MethodRefParameterMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, MethodRefParameter::getName, matchers);
            return this;
        }

        public final MethodRefParameterMatcher unlessHasName() {
            addPredicate("name", n -> n.getName() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node FieldAccess.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class FieldAccessMatcher
        extends ExpressionMatcherSupport<FieldAccess, FieldAccessMatcher> {

        FieldAccessMatcher() {
            super(FieldAccess.class);
        }

        @SafeVarargs
        public final FieldAccessMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, FieldAccess::getExpression, matchers);
            return this;
        }

        public final FieldAccessMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final FieldAccessMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, FieldAccess::getName, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ParameterizedType.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ParameterizedTypeMatcher
        extends CommonMatcher<ParameterizedType, ParameterizedTypeMatcher> {

        ParameterizedTypeMatcher() {
            super(ParameterizedType.class);
        }

        @SafeVarargs
        public final ParameterizedTypeMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, ParameterizedType::getType, matchers);
            return this;
        }

        @SafeVarargs
        public final ParameterizedTypeMatcher hasTypeArgument(Matcher<? extends Type>... matchers) {
            addPropertyList("typeArguments", Type.class, ParameterizedType::typeArguments, matchers);
            return this;
        }

        public final ParameterizedTypeMatcher typeArgumentCountIs(int count) {
            addCountIs(ParameterizedType::typeArguments, count);
            return this;
        }

        @SafeVarargs
        public final ParameterizedTypeMatcher hasTypeArgumentAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeArguments", Type.class, ParameterizedType::typeArguments, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node TypeDeclarationStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class TypeDeclarationStatementMatcher
        extends StatementMatcherSupport<TypeDeclarationStatement, TypeDeclarationStatementMatcher> {

        TypeDeclarationStatementMatcher() {
            super(TypeDeclarationStatement.class);
        }

        @SafeVarargs
        public final TypeDeclarationStatementMatcher hasDeclaration(Matcher<? extends AbstractTypeDeclaration>... matchers) {
            addProperty("declaration", AbstractTypeDeclaration.class, TypeDeclarationStatement::getDeclaration, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node PrimitiveType.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class PrimitiveTypeMatcher
        extends CommonMatcher<PrimitiveType, PrimitiveTypeMatcher> {

        PrimitiveTypeMatcher() {
            super(PrimitiveType.class);
        }

        @SafeVarargs
        public final PrimitiveTypeMatcher hasAnnotation(Matcher<? extends Annotation>... matchers) {
            addPropertyList("annotations", Annotation.class, PrimitiveType::annotations, matchers);
            return this;
        }

        public final PrimitiveTypeMatcher annotationCountIs(int count) {
            addCountIs(PrimitiveType::annotations, count);
            return this;
        }

        @SafeVarargs
        public final PrimitiveTypeMatcher hasAnnotationAt(int index, Matcher<? extends Annotation>... matchers) {
            addPropertyListElement("annotations", Annotation.class, PrimitiveType::annotations, index, matchers);
            return this;
        }

		public final PrimitiveTypeMatcher isInt() {
        	return hasCode(PrimitiveType.INT);
		}

		public final PrimitiveTypeMatcher isChar() {
        	return hasCode(PrimitiveType.CHAR);
		}

		public final PrimitiveTypeMatcher isBoolean() {
        	return hasCode(PrimitiveType.BOOLEAN);
		}

		public final PrimitiveTypeMatcher isShort() {
        	return hasCode(PrimitiveType.SHORT);
		}

		public final PrimitiveTypeMatcher isLong() {
        	return hasCode(PrimitiveType.LONG);
		}

		public final PrimitiveTypeMatcher isFloat() {
        	return hasCode(PrimitiveType.FLOAT);
		}

		public final PrimitiveTypeMatcher isDouble() {
        	return hasCode(PrimitiveType.DOUBLE);
		}

		public final PrimitiveTypeMatcher isByte() {
        	return hasCode(PrimitiveType.BYTE);
		}

		public final PrimitiveTypeMatcher isVoid() {
        	return hasCode(PrimitiveType.VOID);
		}

        public final PrimitiveTypeMatcher hasCode(PrimitiveType.Code code) {
            addPredicate(n -> code != null && code.equals(n.getPrimitiveTypeCode()));
            return this;
        }
    }

    /**
     * Matcher builder for AST node LineComment.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class LineCommentMatcher
        extends CommonMatcher<LineComment, LineCommentMatcher> {

        LineCommentMatcher() {
            super(LineComment.class);
        }
    }

    /**
     * Matcher builder for AST node CreationReference.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class CreationReferenceMatcher
        extends ExpressionMatcherSupport<CreationReference, CreationReferenceMatcher> {

        CreationReferenceMatcher() {
            super(CreationReference.class);
        }

        @SafeVarargs
        public final CreationReferenceMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, CreationReference::getType, matchers);
            return this;
        }

        @SafeVarargs
        public final CreationReferenceMatcher hasTypeArgument(Matcher<? extends Type>... matchers) {
            addPropertyList("typeArguments", Type.class, CreationReference::typeArguments, matchers);
            return this;
        }

        public final CreationReferenceMatcher typeArgumentCountIs(int count) {
            addCountIs(CreationReference::typeArguments, count);
            return this;
        }

        @SafeVarargs
        public final CreationReferenceMatcher hasTypeArgumentAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeArguments", Type.class, CreationReference::typeArguments, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node ImportDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ImportDeclarationMatcher
        extends CommonMatcher<ImportDeclaration, ImportDeclarationMatcher> {

        ImportDeclarationMatcher() {
            super(ImportDeclaration.class);
        }

        public final ImportDeclarationMatcher isStatic() {
            addPredicate("static", n -> n.isStatic());
            return this;
        }

        @SafeVarargs
        public final ImportDeclarationMatcher hasName(Matcher<? extends Name>... matchers) {
            addProperty("name", Name.class, ImportDeclaration::getName, matchers);
            return this;
        }

        public final ImportDeclarationMatcher isOnDemand() {
            addPredicate("onDemand", n -> n.isOnDemand());
            return this;
        }
    }

    /**
     * Matcher builder for AST node IfStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class IfStatementMatcher
        extends StatementMatcherSupport<IfStatement, IfStatementMatcher> {

        IfStatementMatcher() {
            super(IfStatement.class);
        }

        @SafeVarargs
        public final IfStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, IfStatement::getExpression, matchers);
            return this;
        }

        @SafeVarargs
        public final IfStatementMatcher hasThenStatement(Matcher<? extends Statement>... matchers) {
            addProperty("thenStatement", Statement.class, IfStatement::getThenStatement, matchers);
            return this;
        }

        @SafeVarargs
        public final IfStatementMatcher hasElseStatement(Matcher<? extends Statement>... matchers) {
            addProperty("elseStatement", Statement.class, IfStatement::getElseStatement, matchers);
            return this;
        }

        public final IfStatementMatcher unlessHasElseStatement() {
            addPredicate("elseStatement", n -> n.getElseStatement() == null);
            return this;
        }

        @SafeVarargs
		 public final IfStatementMatcher hasCondition(Matcher<? extends Expression>... matchers) {
            return hasExpression(matchers);
		 }

        @SafeVarargs
		 public final IfStatementMatcher hasThen(Matcher<? extends Statement>... matchers) {
            return hasThenStatement(matchers);
		 }

        @SafeVarargs
		 public final IfStatementMatcher hasElse(Matcher<? extends Statement>... matchers) {
            return hasElseStatement(matchers);
		 }
    }

    /**
     * Matcher builder for AST node SimpleName.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SimpleNameMatcher
        extends ExpressionMatcherSupport<SimpleName, SimpleNameMatcher> {

        SimpleNameMatcher() {
            super(SimpleName.class);
        }

        public final SimpleNameMatcher hasIdentifier(String v) {
            addPredicate("identifier", n -> v != null && v.equals(n.getIdentifier()));
            return this;
        }

        public final SimpleNameMatcher hasDeclaringClass(String name) {
            addPredicate("declaringClass", n -> {
                IVariableBinding b = tryCast(n.resolveBinding(), IVariableBinding.class);
                ITypeBinding declaringClass = b != null ? b.getDeclaringClass() : null;
                if (declaringClass == null) {
                	IMethodBinding mb = tryCast(n.resolveBinding(), IMethodBinding.class);
                    declaringClass = mb != null ? mb.getDeclaringClass() : null;
                }
                return name != null && declaringClass != null && name.equals(declaringClass.getQualifiedName());
            });
            return this;
        }
    }

    /**
     * Matcher builder for AST node CompilationUnit.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class CompilationUnitMatcher
        extends CommonMatcher<CompilationUnit, CompilationUnitMatcher> {

        CompilationUnitMatcher() {
            super(CompilationUnit.class);
        }

        @SafeVarargs
        public final CompilationUnitMatcher hasPackage(Matcher<? extends PackageDeclaration>... matchers) {
            addProperty("package", PackageDeclaration.class, CompilationUnit::getPackage, matchers);
            return this;
        }

        public final CompilationUnitMatcher unlessHasPackage() {
            addPredicate("package", n -> n.getPackage() == null);
            return this;
        }

        @SafeVarargs
        public final CompilationUnitMatcher hasImport(Matcher<? extends ImportDeclaration>... matchers) {
            addPropertyList("imports", ImportDeclaration.class, CompilationUnit::imports, matchers);
            return this;
        }

        public final CompilationUnitMatcher importCountIs(int count) {
            addCountIs(CompilationUnit::imports, count);
            return this;
        }

        @SafeVarargs
        public final CompilationUnitMatcher hasImportAt(int index, Matcher<? extends ImportDeclaration>... matchers) {
            addPropertyListElement("imports", ImportDeclaration.class, CompilationUnit::imports, index, matchers);
            return this;
        }

        @SafeVarargs
        public final CompilationUnitMatcher hasType(Matcher<? extends AbstractTypeDeclaration>... matchers) {
            addPropertyList("types", AbstractTypeDeclaration.class, CompilationUnit::types, matchers);
            return this;
        }

        public final CompilationUnitMatcher typeCountIs(int count) {
            addCountIs(CompilationUnit::types, count);
            return this;
        }

        @SafeVarargs
        public final CompilationUnitMatcher hasTypeAt(int index, Matcher<? extends AbstractTypeDeclaration>... matchers) {
            addPropertyListElement("types", AbstractTypeDeclaration.class, CompilationUnit::types, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node PostfixExpression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class PostfixExpressionMatcher
        extends ExpressionMatcherSupport<PostfixExpression, PostfixExpressionMatcher> {

        PostfixExpressionMatcher() {
            super(PostfixExpression.class);
        }

        @SafeVarargs
        public final PostfixExpressionMatcher hasOperand(Matcher<? extends Expression>... matchers) {
            addProperty("operand", Expression.class, PostfixExpression::getOperand, matchers);
            return this;
        }

        public final PostfixExpressionMatcher hasOperator(PostfixExpression.Operator v) {
            addPredicate("operator", n -> v != null && v.equals(n.getOperator()));
            return this;
        }

		public final PostfixExpressionMatcher hasIncrementOperator() {
        	return hasOperator(PostfixExpression.Operator.INCREMENT);
		}

		public final PostfixExpressionMatcher hasDecrementOperator() {
        	return hasOperator(PostfixExpression.Operator.DECREMENT);
		}
    }

    /**
     * Matcher builder for AST node TagElement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class TagElementMatcher
        extends CommonMatcher<TagElement, TagElementMatcher> {

        TagElementMatcher() {
            super(TagElement.class);
        }

        public final TagElementMatcher hasTagName(String v) {
            addPredicate("tagName", n -> v != null && v.equals(n.getTagName()));
            return this;
        }
    }

    /**
     * Matcher builder for AST node Block.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class BlockMatcher
        extends StatementMatcherSupport<Block, BlockMatcher> {

        BlockMatcher() {
            super(Block.class);
        }

        @SafeVarargs
        public final BlockMatcher hasStatement(Matcher<? extends Statement>... matchers) {
            addPropertyList("statements", Statement.class, Block::statements, matchers);
            return this;
        }

        public final BlockMatcher statementCountIs(int count) {
            addCountIs(Block::statements, count);
            return this;
        }

        @SafeVarargs
        public final BlockMatcher hasStatementAt(int index, Matcher<? extends Statement>... matchers) {
            addPropertyListElement("statements", Statement.class, Block::statements, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node NameQualifiedType.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class NameQualifiedTypeMatcher
        extends CommonMatcher<NameQualifiedType, NameQualifiedTypeMatcher> {

        NameQualifiedTypeMatcher() {
            super(NameQualifiedType.class);
        }

        @SafeVarargs
        public final NameQualifiedTypeMatcher hasQualifier(Matcher<? extends Name>... matchers) {
            addProperty("qualifier", Name.class, NameQualifiedType::getQualifier, matchers);
            return this;
        }

        @SafeVarargs
        public final NameQualifiedTypeMatcher hasAnnotation(Matcher<? extends Annotation>... matchers) {
            addPropertyList("annotations", Annotation.class, NameQualifiedType::annotations, matchers);
            return this;
        }

        public final NameQualifiedTypeMatcher annotationCountIs(int count) {
            addCountIs(NameQualifiedType::annotations, count);
            return this;
        }

        @SafeVarargs
        public final NameQualifiedTypeMatcher hasAnnotationAt(int index, Matcher<? extends Annotation>... matchers) {
            addPropertyListElement("annotations", Annotation.class, NameQualifiedType::annotations, index, matchers);
            return this;
        }

        public final NameQualifiedTypeMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final NameQualifiedTypeMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, NameQualifiedType::getName, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node AnnotationTypeMemberDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class AnnotationTypeMemberDeclarationMatcher
        extends CommonMatcher<AnnotationTypeMemberDeclaration, AnnotationTypeMemberDeclarationMatcher> {

        AnnotationTypeMemberDeclarationMatcher() {
            super(AnnotationTypeMemberDeclaration.class);
        }

        @SafeVarargs
        public final AnnotationTypeMemberDeclarationMatcher hasJavadoc(Matcher<? extends Javadoc>... matchers) {
            addProperty("javadoc", Javadoc.class, AnnotationTypeMemberDeclaration::getJavadoc, matchers);
            return this;
        }

        public final AnnotationTypeMemberDeclarationMatcher unlessHasJavadoc() {
            addPredicate("javadoc", n -> n.getJavadoc() == null);
            return this;
        }

        @SafeVarargs
        public final AnnotationTypeMemberDeclarationMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, AnnotationTypeMemberDeclaration::getType, matchers);
            return this;
        }

        public final AnnotationTypeMemberDeclarationMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final AnnotationTypeMemberDeclarationMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, AnnotationTypeMemberDeclaration::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final AnnotationTypeMemberDeclarationMatcher hasDefault(Matcher<? extends Expression>... matchers) {
            addProperty("default", Expression.class, AnnotationTypeMemberDeclaration::getDefault, matchers);
            return this;
        }

        public final AnnotationTypeMemberDeclarationMatcher unlessHasDefault() {
            addPredicate("default", n -> n.getDefault() == null);
            return this;
        }
    }

    /**
     * Matcher builder for AST node Statement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class StatementMatcher
        extends StatementMatcherSupport<Statement, StatementMatcher> {

        StatementMatcher() {
            super(Statement.class);
        }
    }

    /**
     * Matcher builder for AST node StringLiteral.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class StringLiteralMatcher
        extends ExpressionMatcherSupport<StringLiteral, StringLiteralMatcher> {

        StringLiteralMatcher() {
            super(StringLiteral.class);
        }

        public final StringLiteralMatcher hasEscapedValue(String v) {
            addPredicate("escapedValue", n -> v != null && v.equals(n.getEscapedValue()));
            return this;
        }

        public final StringLiteralMatcher isEqualTo(String v) {
            addPredicate(n -> v != null && v.equals(n.getLiteralValue()));
            return this;
        }
    }

    /**
     * Matcher builder for AST node ForStatement.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ForStatementMatcher
        extends StatementMatcherSupport<ForStatement, ForStatementMatcher> {

        ForStatementMatcher() {
            super(ForStatement.class);
        }

        @SafeVarargs
        public final ForStatementMatcher hasInitializer(Matcher<? extends Expression>... matchers) {
            addPropertyList("initializers", Expression.class, ForStatement::initializers, matchers);
            return this;
        }

        public final ForStatementMatcher initializerCountIs(int count) {
            addCountIs(ForStatement::initializers, count);
            return this;
        }

        @SafeVarargs
        public final ForStatementMatcher hasInitializerAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("initializers", Expression.class, ForStatement::initializers, index, matchers);
            return this;
        }

        @SafeVarargs
        public final ForStatementMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addProperty("expression", Expression.class, ForStatement::getExpression, matchers);
            return this;
        }

        public final ForStatementMatcher unlessHasExpression() {
            addPredicate("expression", n -> n.getExpression() == null);
            return this;
        }

        @SafeVarargs
        public final ForStatementMatcher hasUpdater(Matcher<? extends Expression>... matchers) {
            addPropertyList("updaters", Expression.class, ForStatement::updaters, matchers);
            return this;
        }

        public final ForStatementMatcher updaterCountIs(int count) {
            addCountIs(ForStatement::updaters, count);
            return this;
        }

        @SafeVarargs
        public final ForStatementMatcher hasUpdaterAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("updaters", Expression.class, ForStatement::updaters, index, matchers);
            return this;
        }

        @SafeVarargs
        public final ForStatementMatcher hasBody(Matcher<? extends Statement>... matchers) {
            addProperty("body", Statement.class, ForStatement::getBody, matchers);
            return this;
        }

       @SafeVarargs
		public final ForStatementMatcher hasCondition(Matcher<? extends Expression>... matchers) {
        	return hasExpression(matchers);
		}
    }

    /**
     * Matcher builder for AST node PrefixExpression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class PrefixExpressionMatcher
        extends ExpressionMatcherSupport<PrefixExpression, PrefixExpressionMatcher> {

        PrefixExpressionMatcher() {
            super(PrefixExpression.class);
        }

        public final PrefixExpressionMatcher hasOperator(PrefixExpression.Operator v) {
            addPredicate("operator", n -> v != null && v.equals(n.getOperator()));
            return this;
        }

        @SafeVarargs
        public final PrefixExpressionMatcher hasOperand(Matcher<? extends Expression>... matchers) {
            addProperty("operand", Expression.class, PrefixExpression::getOperand, matchers);
            return this;
        }

		public final PrefixExpressionMatcher hasIncrementOperator() {
        	return hasOperator(PrefixExpression.Operator.INCREMENT);
		}

		public final PrefixExpressionMatcher hasDecrementOperator() {
        	return hasOperator(PrefixExpression.Operator.DECREMENT);
		}

		public final PrefixExpressionMatcher hasPlusOperator() {
        	return hasOperator(PrefixExpression.Operator.PLUS);
		}

		public final PrefixExpressionMatcher hasMinusOperator() {
        	return hasOperator(PrefixExpression.Operator.MINUS);
		}

		public final PrefixExpressionMatcher hasComplementOperator() {
        	return hasOperator(PrefixExpression.Operator.COMPLEMENT);
		}

		public final PrefixExpressionMatcher hasNotOperator() {
        	return hasOperator(PrefixExpression.Operator.NOT);
		}
    }

    /**
     * Matcher builder for AST node ArrayInitializer.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class ArrayInitializerMatcher
        extends ExpressionMatcherSupport<ArrayInitializer, ArrayInitializerMatcher> {

        ArrayInitializerMatcher() {
            super(ArrayInitializer.class);
        }

        @SafeVarargs
        public final ArrayInitializerMatcher hasExpression(Matcher<? extends Expression>... matchers) {
            addPropertyList("expressions", Expression.class, ArrayInitializer::expressions, matchers);
            return this;
        }

        public final ArrayInitializerMatcher expressionCountIs(int count) {
            addCountIs(ArrayInitializer::expressions, count);
            return this;
        }

        @SafeVarargs
        public final ArrayInitializerMatcher hasExpressionAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("expressions", Expression.class, ArrayInitializer::expressions, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node SuperMethodInvocation.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SuperMethodInvocationMatcher
        extends ExpressionMatcherSupport<SuperMethodInvocation, SuperMethodInvocationMatcher> {

        SuperMethodInvocationMatcher() {
            super(SuperMethodInvocation.class);
        }

        @SafeVarargs
        public final SuperMethodInvocationMatcher hasQualifier(Matcher<? extends Name>... matchers) {
            addProperty("qualifier", Name.class, SuperMethodInvocation::getQualifier, matchers);
            return this;
        }

        public final SuperMethodInvocationMatcher unlessHasQualifier() {
            addPredicate("qualifier", n -> n.getQualifier() == null);
            return this;
        }

        @SafeVarargs
        public final SuperMethodInvocationMatcher hasTypeArgument(Matcher<? extends Type>... matchers) {
            addPropertyList("typeArguments", Type.class, SuperMethodInvocation::typeArguments, matchers);
            return this;
        }

        public final SuperMethodInvocationMatcher typeArgumentCountIs(int count) {
            addCountIs(SuperMethodInvocation::typeArguments, count);
            return this;
        }

        @SafeVarargs
        public final SuperMethodInvocationMatcher hasTypeArgumentAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeArguments", Type.class, SuperMethodInvocation::typeArguments, index, matchers);
            return this;
        }

        public final SuperMethodInvocationMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final SuperMethodInvocationMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, SuperMethodInvocation::getName, matchers);
            return this;
        }

        @SafeVarargs
        public final SuperMethodInvocationMatcher hasArgument(Matcher<? extends Expression>... matchers) {
            addPropertyList("arguments", Expression.class, SuperMethodInvocation::arguments, matchers);
            return this;
        }

        public final SuperMethodInvocationMatcher argumentCountIs(int count) {
            addCountIs(SuperMethodInvocation::arguments, count);
            return this;
        }

        @SafeVarargs
        public final SuperMethodInvocationMatcher hasArgumentAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("arguments", Expression.class, SuperMethodInvocation::arguments, index, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node SuperMethodReference.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class SuperMethodReferenceMatcher
        extends ExpressionMatcherSupport<SuperMethodReference, SuperMethodReferenceMatcher> {

        SuperMethodReferenceMatcher() {
            super(SuperMethodReference.class);
        }

        @SafeVarargs
        public final SuperMethodReferenceMatcher hasQualifier(Matcher<? extends Name>... matchers) {
            addProperty("qualifier", Name.class, SuperMethodReference::getQualifier, matchers);
            return this;
        }

        public final SuperMethodReferenceMatcher unlessHasQualifier() {
            addPredicate("qualifier", n -> n.getQualifier() == null);
            return this;
        }

        @SafeVarargs
        public final SuperMethodReferenceMatcher hasTypeArgument(Matcher<? extends Type>... matchers) {
            addPropertyList("typeArguments", Type.class, SuperMethodReference::typeArguments, matchers);
            return this;
        }

        public final SuperMethodReferenceMatcher typeArgumentCountIs(int count) {
            addCountIs(SuperMethodReference::typeArguments, count);
            return this;
        }

        @SafeVarargs
        public final SuperMethodReferenceMatcher hasTypeArgumentAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeArguments", Type.class, SuperMethodReference::typeArguments, index, matchers);
            return this;
        }

        public final SuperMethodReferenceMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final SuperMethodReferenceMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, SuperMethodReference::getName, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node TypeMethodReference.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class TypeMethodReferenceMatcher
        extends ExpressionMatcherSupport<TypeMethodReference, TypeMethodReferenceMatcher> {

        TypeMethodReferenceMatcher() {
            super(TypeMethodReference.class);
        }

        @SafeVarargs
        public final TypeMethodReferenceMatcher hasType(Matcher<? extends Type>... matchers) {
            addProperty("type", Type.class, TypeMethodReference::getType, matchers);
            return this;
        }

        @SafeVarargs
        public final TypeMethodReferenceMatcher hasTypeArgument(Matcher<? extends Type>... matchers) {
            addPropertyList("typeArguments", Type.class, TypeMethodReference::typeArguments, matchers);
            return this;
        }

        public final TypeMethodReferenceMatcher typeArgumentCountIs(int count) {
            addCountIs(TypeMethodReference::typeArguments, count);
            return this;
        }

        @SafeVarargs
        public final TypeMethodReferenceMatcher hasTypeArgumentAt(int index, Matcher<? extends Type>... matchers) {
            addPropertyListElement("typeArguments", Type.class, TypeMethodReference::typeArguments, index, matchers);
            return this;
        }

        public final TypeMethodReferenceMatcher hasName(String name) {
            addPredicate("name", n -> { SimpleName sn = tryCast(n.getName(), SimpleName.class); return name != null && sn != null && name.equals(sn.getIdentifier()); });
            return this;
        }

        @SafeVarargs
        public final TypeMethodReferenceMatcher hasName(Matcher<? extends SimpleName>... matchers) {
            addProperty("name", SimpleName.class, TypeMethodReference::getName, matchers);
            return this;
        }
    }

    /**
     * Matcher builder for AST node InfixExpression.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class InfixExpressionMatcher
        extends ExpressionMatcherSupport<InfixExpression, InfixExpressionMatcher> {

        InfixExpressionMatcher() {
            super(InfixExpression.class);
        }

        @SafeVarargs
        public final InfixExpressionMatcher hasLeftOperand(Matcher<? extends Expression>... matchers) {
            addProperty("leftOperand", Expression.class, InfixExpression::getLeftOperand, matchers);
            return this;
        }

        public final InfixExpressionMatcher hasOperator(InfixExpression.Operator v) {
            addPredicate("operator", n -> v != null && v.equals(n.getOperator()));
            return this;
        }

        @SafeVarargs
        public final InfixExpressionMatcher hasRightOperand(Matcher<? extends Expression>... matchers) {
            addProperty("rightOperand", Expression.class, InfixExpression::getRightOperand, matchers);
            return this;
        }

        @SafeVarargs
        public final InfixExpressionMatcher hasExtendedOperand(Matcher<? extends Expression>... matchers) {
            addPropertyList("extendedOperands", Expression.class, InfixExpression::extendedOperands, matchers);
            return this;
        }

        public final InfixExpressionMatcher extendedOperandCountIs(int count) {
            addCountIs(InfixExpression::extendedOperands, count);
            return this;
        }

        @SafeVarargs
        public final InfixExpressionMatcher hasExtendedOperandAt(int index, Matcher<? extends Expression>... matchers) {
            addPropertyListElement("extendedOperands", Expression.class, InfixExpression::extendedOperands, index, matchers);
            return this;
        }

		public final InfixExpressionMatcher hasTimesOperator() {
        	return hasOperator(InfixExpression.Operator.TIMES);
		}

		public final InfixExpressionMatcher hasDivideOperator() {
        	return hasOperator(InfixExpression.Operator.DIVIDE);
		}

		public final InfixExpressionMatcher hasRemainderOperator() {
        	return hasOperator(InfixExpression.Operator.REMAINDER);
		}

		public final InfixExpressionMatcher hasPlusOperator() {
        	return hasOperator(InfixExpression.Operator.PLUS);
		}

		public final InfixExpressionMatcher hasMinusOperator() {
        	return hasOperator(InfixExpression.Operator.MINUS);
		}

		public final InfixExpressionMatcher hasLeftShiftOperator() {
        	return hasOperator(InfixExpression.Operator.LEFT_SHIFT);
		}

		public final InfixExpressionMatcher hasRightShiftSignedOperator() {
        	return hasOperator(InfixExpression.Operator.RIGHT_SHIFT_SIGNED);
		}

		public final InfixExpressionMatcher hasRightShiftUnsignedOperator() {
        	return hasOperator(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED);
		}

		public final InfixExpressionMatcher hasLessOperator() {
        	return hasOperator(InfixExpression.Operator.LESS);
		}

		public final InfixExpressionMatcher hasGreaterOperator() {
        	return hasOperator(InfixExpression.Operator.GREATER);
		}

		public final InfixExpressionMatcher hasLessEqualsOperator() {
        	return hasOperator(InfixExpression.Operator.LESS_EQUALS);
		}

		public final InfixExpressionMatcher hasGreaterEqualsOperator() {
        	return hasOperator(InfixExpression.Operator.GREATER_EQUALS);
		}

		public final InfixExpressionMatcher hasEqualsOperator() {
        	return hasOperator(InfixExpression.Operator.EQUALS);
		}

		public final InfixExpressionMatcher hasNotEqualsOperator() {
        	return hasOperator(InfixExpression.Operator.NOT_EQUALS);
		}

		public final InfixExpressionMatcher hasXorOperator() {
        	return hasOperator(InfixExpression.Operator.XOR);
		}

		public final InfixExpressionMatcher hasOrOperator() {
        	return hasOperator(InfixExpression.Operator.OR);
		}

		public final InfixExpressionMatcher hasAndOperator() {
        	return hasOperator(InfixExpression.Operator.AND);
		}

		public final InfixExpressionMatcher hasConditionalOrOperator() {
        	return hasOperator(InfixExpression.Operator.CONDITIONAL_OR);
		}

		public final InfixExpressionMatcher hasConditionalAndOperator() {
        	return hasOperator(InfixExpression.Operator.CONDITIONAL_AND);
		}
    }

    /**
     * Matcher builder for AST node Assignment.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class AssignmentMatcher
        extends ExpressionMatcherSupport<Assignment, AssignmentMatcher> {

        AssignmentMatcher() {
            super(Assignment.class);
        }

        @SafeVarargs
        public final AssignmentMatcher hasLeftHandSide(Matcher<? extends Expression>... matchers) {
            addProperty("leftHandSide", Expression.class, Assignment::getLeftHandSide, matchers);
            return this;
        }

        public final AssignmentMatcher hasOperator(Assignment.Operator v) {
            addPredicate("operator", n -> v != null && v.equals(n.getOperator()));
            return this;
        }

        @SafeVarargs
        public final AssignmentMatcher hasRightHandSide(Matcher<? extends Expression>... matchers) {
            addProperty("rightHandSide", Expression.class, Assignment::getRightHandSide, matchers);
            return this;
        }

		public final AssignmentMatcher hasAssignOperator() {
        	return hasOperator(Assignment.Operator.ASSIGN);
		}

		public final AssignmentMatcher hasPlusAssignOperator() {
        	return hasOperator(Assignment.Operator.PLUS_ASSIGN);
		}

		public final AssignmentMatcher hasMinusAssignOperator() {
        	return hasOperator(Assignment.Operator.MINUS_ASSIGN);
		}

		public final AssignmentMatcher hasTimesAssignOperator() {
        	return hasOperator(Assignment.Operator.TIMES_ASSIGN);
		}

		public final AssignmentMatcher hasDivideAssignOperator() {
        	return hasOperator(Assignment.Operator.DIVIDE_ASSIGN);
		}

		public final AssignmentMatcher hasBitAndAssignOperator() {
        	return hasOperator(Assignment.Operator.BIT_AND_ASSIGN);
		}

		public final AssignmentMatcher hasBitOrAssignOperator() {
        	return hasOperator(Assignment.Operator.BIT_OR_ASSIGN);
		}

		public final AssignmentMatcher hasBitXorAssignOperator() {
        	return hasOperator(Assignment.Operator.BIT_XOR_ASSIGN);
		}

		public final AssignmentMatcher hasRemainderAssignOperator() {
        	return hasOperator(Assignment.Operator.REMAINDER_ASSIGN);
		}

		public final AssignmentMatcher hasLeftShiftAssignOperator() {
        	return hasOperator(Assignment.Operator.LEFT_SHIFT_ASSIGN);
		}

		public final AssignmentMatcher hasRightShiftSignedAssignOperator() {
        	return hasOperator(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN);
		}

		public final AssignmentMatcher hasRightShiftUnsignedAssignOperator() {
        	return hasOperator(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN);
		}
    }

    /**
     * Matcher builder for AST node PackageDeclaration.
     *
     * This code is generated.
     * For details see {@link Matchers}.
     */
    public static final class PackageDeclarationMatcher
        extends CommonMatcher<PackageDeclaration, PackageDeclarationMatcher> {

        PackageDeclarationMatcher() {
            super(PackageDeclaration.class);
        }

        @SafeVarargs
        public final PackageDeclarationMatcher hasJavadoc(Matcher<? extends Javadoc>... matchers) {
            addProperty("javadoc", Javadoc.class, PackageDeclaration::getJavadoc, matchers);
            return this;
        }

        public final PackageDeclarationMatcher unlessHasJavadoc() {
            addPredicate("javadoc", n -> n.getJavadoc() == null);
            return this;
        }

        @SafeVarargs
        public final PackageDeclarationMatcher hasAnnotation(Matcher<? extends Annotation>... matchers) {
            addPropertyList("annotations", Annotation.class, PackageDeclaration::annotations, matchers);
            return this;
        }

        public final PackageDeclarationMatcher annotationCountIs(int count) {
            addCountIs(PackageDeclaration::annotations, count);
            return this;
        }

        @SafeVarargs
        public final PackageDeclarationMatcher hasAnnotationAt(int index, Matcher<? extends Annotation>... matchers) {
            addPropertyListElement("annotations", Annotation.class, PackageDeclaration::annotations, index, matchers);
            return this;
        }

        @SafeVarargs
        public final PackageDeclarationMatcher hasName(Matcher<? extends Name>... matchers) {
            addProperty("name", Name.class, PackageDeclaration::getName, matchers);
            return this;
        }
    }
}
