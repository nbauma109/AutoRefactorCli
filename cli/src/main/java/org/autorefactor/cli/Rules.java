package org.autorefactor.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.autorefactor.refactoring.RefactoringRule;
import org.autorefactor.refactoring.rules.AddBracketsToControlStatementRefactoring;
import org.autorefactor.refactoring.rules.AllRefactoringRules;
import org.autorefactor.refactoring.rules.AnnotationRefactoring;
import org.autorefactor.refactoring.rules.CollapseIfStatementRefactoring;
import org.autorefactor.refactoring.rules.CollectionContainsRefactoring;
import org.autorefactor.refactoring.rules.CommentsRefactoring;
import org.autorefactor.refactoring.rules.CommonCodeInIfElseStatementRefactoring;
import org.autorefactor.refactoring.rules.CommonIfInIfElseRefactoring;
import org.autorefactor.refactoring.rules.HotSpotIntrinsicedAPIsRefactoring;
import org.autorefactor.refactoring.rules.IfElseIfRefactoring;
import org.autorefactor.refactoring.rules.InvertEqualsRefactoring;
import org.autorefactor.refactoring.rules.MapRefactoring;
import org.autorefactor.refactoring.rules.NoAssignmentInIfConditionRefactoring;
import org.autorefactor.refactoring.rules.PrimitiveWrapperCreationRefactoring;
import org.autorefactor.refactoring.rules.PushNegationDownRefactoring;
import org.autorefactor.refactoring.rules.RemoveEmptyLinesRefactoring;
import org.autorefactor.refactoring.rules.RemoveFieldsDefaultValuesRefactoring;
import org.autorefactor.refactoring.rules.RemoveSemiColonRefactoring;
import org.autorefactor.refactoring.rules.RemoveUnnecessaryCastRefactoring;
import org.autorefactor.refactoring.rules.RemoveUnneededThisExpressionRefactoring;
import org.autorefactor.refactoring.rules.RemoveUselessNullCheckRefactoring;
import org.autorefactor.refactoring.rules.SetRatherThanMapRefactoring;
import org.autorefactor.refactoring.rules.StringBuilderRefactoring;
import org.autorefactor.refactoring.rules.TestNGAssertRefactoring;
import org.autorefactor.refactoring.rules.TryWithResourceRefactoring;
import org.autorefactor.refactoring.rules.UseDiamondOperatorRefactoring;
import org.autorefactor.refactoring.rules.UseMultiCatchRefactoring;
import org.autorefactor.refactoring.rules.UseStringContainsRefactoring;
import org.autorefactor.refactoring.rules.VectorOldToNewAPIRefactoring;
import org.autorefactor.refactoring.rules.WorkWithNullCheckedExpressionFirstRefactoring;

@SuppressWarnings("restriction")
public class Rules {

	final static Map<String, SourceLevel> sourceLevelPerRuleClass;
    static {
        Map<String, SourceLevel> m = new HashMap<String, SourceLevel>();
        m.put(UseMultiCatchRefactoring.class.getName(), SourceLevel.Java7);
        m.put(TryWithResourceRefactoring.class.getName(), SourceLevel.Java7);
        m.put(UseDiamondOperatorRefactoring.class.getName(), SourceLevel.Java7);
        sourceLevelPerRuleClass = Collections.unmodifiableMap(m);
    }

	/**
	 * Open bugs:
	 *
	 * No not move end() before if() begin() if (needsBegin()) { end(); } else {
	 * end(); }
	 *
	 * @return rules
	 */
	public static List<RefactoringRule> filteredAllRules() {
	    List<RefactoringRule> rules = new ArrayList<RefactoringRule>(AllRefactoringRules.getAllRefactoringRules());
	    // leave that for formatter
	    Rules.removeByClass(rules, RemoveEmptyLinesRefactoring.class);
	    // leave that for formatter
	    Rules.removeByClass(rules, CommentsRefactoring.class);
	    // wrong semantic change
	    Rules.removeByClass(rules, CommonCodeInIfElseStatementRefactoring.class);
	    // wrong changes (https://github.com/JnRouvignac/AutoRefactor/issues/300)
	    Rules.removeByClass(rules, SetRatherThanMapRefactoring.class);
	    return rules;
	}

	static RefactoringRule findBySimpleClassName(List<RefactoringRule> rules, String name) {
	    for (RefactoringRule rule : rules) {
	        if (name.equals(rule.getClass().getSimpleName())) {
	            return rule;
	        }
	    }
	    return null;
	}

	static void removeByClass(List<RefactoringRule> rules, Class<? extends RefactoringRule> clazz) {
	    Iterator<RefactoringRule> it = rules.iterator();
	    while (it.hasNext()) {
	        if (clazz.isInstance(it.next())) {
	            it.remove();
	            break;
	        }
	    }
	}

	/**
	 * Selected rules.
	 *
	 * @param refactorings
	 *            the resolved refactorings argument
	 * @param excludedRefactorings
	 * @param sourceLevel source level
	 * @return List of selected rules
	 */
	static List<RefactoringRule> filterRules(List<RefactoringRule> refactorings, List<String> excludedRefactorings, SourceLevel sourceLevel) {
	    List<RefactoringRule> rules = new ArrayList<>(refactorings);
	    if (!excludedRefactorings.isEmpty()) {
	        System.out.println("exclude refactorings: " + excludedRefactorings);
	        for (String name: excludedRefactorings) {
		        RefactoringRule rule = findBySimpleClassName(rules, name);
		        if (rule == null) {
		            System.err.println("warning: could not find rule: " + name);
		        } else {
		        	rules.remove(rule);
		        }
	        }
	    }
	    Iterator<RefactoringRule> it = rules.iterator();
	    while (it.hasNext()) {
	        final RefactoringRule rule = it.next();
	        SourceLevel rsl = sourceLevelPerRuleClass.get(rule.getClass().getName());
	        if (rsl != null && rsl.compareTo(sourceLevel) > 0) {
	            System.out.println("disabled rule by source level '" + sourceLevel
	                    + "': " + rule.getClass().getSimpleName() + " (" + rsl + ")");
	            it.remove();
	        }
	    }
	    return rules;
	}

	public static List<RefactoringRule> resolveRules(List<String> refactorings) {
		final List<RefactoringRule> allRules = AllRefactoringRules.getAllRefactoringRules();
	    List<RefactoringRule> rules = new ArrayList<RefactoringRule>(allRules.size());
	    for (String name : refactorings) {
	        if ("all".equals(name)) {
	            rules.clear();
	            rules.addAll(allRules);
	            break;
	        }
	        if ("all-cal-filter".equals(name)) {
	            rules.clear();
	            rules.addAll(filteredAllRules());
	            break;
	        }
	        RefactoringRule rule = findBySimpleClassName(allRules, name);
	        if (rule == null) {
	            throw new IllegalArgumentException("could not find rule: " + name);
	        }
	        rules.add(rule);
	    }
		return rules;
	}

	/**
	 * Selected rules.
	 *
	 * @return List of selected rules
	 */
	public static List<RefactoringRule> selectedRules() {
	    return new ArrayList<RefactoringRule>(Arrays.asList(
	            // 170
	            // new RemoveUnnecessaryLocalBeforeReturnRefactoring()
	            // 160
	            // new RemoveUnneededThisExpressionRefactoring()
	            // 167,183,184
	            // new StringBuilderRefactoring()
	            new RemoveUnnecessaryCastRefactoring()));
	}

	/**
	 * The list of refactorings that don't create uncompilable code or don't
	 * crash.
	 * 
	 * TODO: check for AutoRefactor 2.0
	 *
	 * @return rules
	 */
	public static List<RefactoringRule> getAllUnbrokenRules() {
	    return new ArrayList<RefactoringRule>(Arrays.asList(new RemoveUselessNullCheckRefactoring(),
	            new WorkWithNullCheckedExpressionFirstRefactoring(), new VectorOldToNewAPIRefactoring(),
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/161 -
	            // closed
	            new PrimitiveWrapperCreationRefactoring(),
	            // cal: maybe
	            // https://github.com/JnRouvignac/AutoRefactor/issues/165 -
	            // closed
	            // cal: maybe Don't overoptimize the booleans #190
	            // new BooleanRefactoring(),
	            new AddBracketsToControlStatementRefactoring(), new InvertEqualsRefactoring(),
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/162 -
	            // closed
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/181 -
	            // jdt bug - compile error
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/182 -
	            // closed
	            // new SimplifyExpressionRefactoring(),
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/160 -
	            // closed - but buggy
	            new RemoveUnneededThisExpressionRefactoring(),
	            // TODO JNR implement
	            // new ForeachRefactoring(),
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/159
	            // new DeadCodeEliminationRefactoring(),
	            new CollapseIfStatementRefactoring(), new CommonCodeInIfElseStatementRefactoring(),
	            // TODO JNR complete it
	            // new GenerecizeRefactoring(),
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/163 -
	            // duplicate
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/121 -
	            // closed
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/189
	            // new UseDiamondOperatorRefactoring(),
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/166 -
	            // closed
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/188
	            new UseMultiCatchRefactoring(), new CollectionContainsRefactoring(),
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/169 -
	            // fixed
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/158
	            // (AIOOBE)
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/179
	            // (Illegal Identifier)
	            // new CollectionRefactoring(),
	            new MapRefactoring(), new NoAssignmentInIfConditionRefactoring(), new IfElseIfRefactoring(),
	            new CommonIfInIfElseRefactoring(),
	            // TODO JNR implement
	            // new RemoveStupidIdiomaticPatternRefactoring(),
	            // TODO JNR - to be completed
	            // new ReduceVariableScopeRefactoring(),
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/167 -
	            // closed
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/183 -
	            // closed
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/184 -
	            // closed
	            new StringBuilderRefactoring(), new UseStringContainsRefactoring(), new PushNegationDownRefactoring(),
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/164
	            // and it's slow
	            // new CommentsRefactoring(),
	            new RemoveFieldsDefaultValuesRefactoring(),
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/170 -
	            // closed
	            // cal: https://github.com/JnRouvignac/AutoRefactor/issues/191 -
	            // plugin crash
	            // new RemoveUnnecessaryLocalBeforeReturnRefactoring(),
	            // cal: TODO: maybe wrong in combination (compile error)
	            // new RemoveUnnecessaryCastRefactoring(),
	            // cal: may cause infinite looping, late disable for now
	            // new RemoveUselessModifiersRefactoring(),
	            // TODO: new name?
	            new HotSpotIntrinsicedAPIsRefactoring(), new AnnotationRefactoring(), new RemoveSemiColonRefactoring(),
	            // FIXME it would be nice if it was only enabled when testng jar
	            // is detected for the project
	            new TestNGAssertRefactoring(), new RemoveEmptyLinesRefactoring()));
	}

}
