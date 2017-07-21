[![Build Status](https://travis-ci.org/JnRouvignac/AutoRefactor.png)](https://travis-ci.org/JnRouvignac/AutoRefactor)
[![Join the chat at https://gitter.im/JnRouvignac/AutoRefactor](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/JnRouvignac/AutoRefactor?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# AutoRefactor - Command Line Interface - *Experimental*

## Build (Linux only)

```
./build-cli
```

## Usage

```
$ ./cli/target/autorefactor/bin/autorefactor --help
Usage: autorefactor [options] [command] [command options]
  Options:
    --consolelog
      Show eclipse console log. Must be first parameter.
      Default: false
    --debug
      Debug mode
      Default: false
    --help, help
      Display usage info.
    --verbose
      Verbose mode
      Default: false
  Commands:
    list      List available refactorings.
      Usage: list

    apply      Apply refactorings.
      Usage: apply [options]
        Options:
          --classpath-variable
            Provide classpath variable (e.g. LIBS_DIR=/some/dir).
            Default: []
        * --project
            Path to project file.
          --source
            Source directories to use. (e.g. src/main/java).
            Default: []
```

## Apply Refactorings

```
./cli/target/autorefactor/bin/autorefactor apply \ 
    --project /some/source/dir/.project \
    --source src/main/java --source src/test/java \
    --refactorings UseDiamondOperatorRefactoring
```

## List Available Refactorings

```
$ ./cli/target/autorefactor/bin/autorefactor list
Available refactorings:
    AddBracketsToControlStatementRefactoring - Add brackets to control statement (pre-configured)
        Adds brackets to:
        - if then/else clauses,
        - for loop body,
        - do ... while loop body,
        - while loop body.
    AllInOneMethodRatherThanLoopRefactoring - All in one method rather than loop (pre-configured)
        Collection related refactorings:
        - replaces for/foreach loops to use Collections.addAll() where 
        possible,
        - replaces for/foreach loops to use Collection.addAll() where 
        possible,
        - replaces for/foreach loops to use Collection.containsAll() where 
        possible,
        - replaces for/foreach loops to use Collection.removeAll() where 
        possible.
    AndroidViewHolderRefactoring - Android ViewHolder (pre-configured)
        Android - Optimize getView() routines for Android applications. It 
        reduces the number calls to the costly inflate()) and getViewById() 
        Android API methods.
    AndroidWakeLockRefactoring - Android WakeLock (pre-configured)
        Android - Failing to release a wakelock properly can keep the 
        Android device in a high power mode, which reduces battery life. 
        There are several causes for this, such as releasing the wake lock 
        in onDestroy() instead of in onPause(), failing to call release() in 
        all possible code paths after an acquire(), and so on.
    AnnotationRefactoring - Annotation (pre-configured)
        Simplifies annotation uses:
        - empty parentheses will be removed from annotations,
        - single members named "value" will be removed from annotations and 
        only the value will be left.
    ArrayDequeRatherThanStackRefactoring - ArrayDeque rather than Stack (pre-configured)
        Replace Stack by ArrayDeque when possible.
    ArrayListRatherThanLinkedListRefactoring - ArrayList rather than LinkedList (pre-configured)
        Replace LinkedList by ArrayList when no item is inserted or removed 
        in the middle of the list.
    ArrayListRatherThanVectorRefactoring - ArrayList rather than Vector (pre-configured)
        Replace Vector by ArrayList when possible.
    BigDecimalRefactoring - BigDecimal (pre-configured)
        Refactors to a proper use of BigDecimals:
        - create BigDecimals from Strings rather than floating point values,
        - create BigDecimals from integers rather than String representing 
        integers,
        - use BigDecimal constants,
        - replace calls to BigDecimal.equals(Object) with calls to 
        BigDecimal.compareTo(BigDecimal).
    BooleanConstantRatherThanValueOfRefactoring - Boolean constant rather than valueOf() (pre-configured)
        Replace Boolean.valueOf(true) and Boolean.valueOf(false) by Boolean.
        TRUE and Boolean.FALSE.
    BooleanEqualsRatherThanNullCheckRefactoring - Boolean equals() rather than null check (pre-configured)
        Replace a null check of a Boolean followed by its value by an 
        equality with a boolean constant.
    BooleanRefactoring - Boolean (pre-configured)
        Boolean related refactorings:
        - remove if statements when then and else clauses do similar things 
        with opposite boolean values,
        - remove ternary operators when then and else clauses do similar 
        things with opposite boolean values,
        - simplify boolean expressions.
    CapitalizeLongLiteralRefactoring - Capitalize lower case 'l' -> 'L' for long number literals (pre-configured)
        Capitalize lower case 'l' -> 'L' for long number literals
    CollapseIfStatementRefactoring - Collapse if statements (pre-configured)
        Collapses two consecutive if statements into just one.
    CollectionContainsRefactoring - Collection.contains() rather than loop (pre-configured)
        Replace loop with Collection.contains(Object obj).
    CollectionRefactoring - Inited collection rather than new collection and Collection.addAll() (pre-configured)
        Replaces creating a new Collection, then invoking Collection.addAll()
         on it, by creating the new Collection with the other Collection as 
        parameter.
    CommentsRefactoring - Comments (pre-configured)
        Refactors comments:
        - remove empty comments and javadocs,
        - transform comments applicable to java elements into javadocs,
        - transform javadocs that are not attached to any java elements into 
        block comments,
        - remove IDE generated TODOs,
        - remove empty lines at start and end of javadocs and block comments,
        - uppercase first letter of javadocs,
        - collapse javadocs on a single line when allowed by Eclipse 
        settings for formatting,
        - add final '.' to javadocs that do not have any,
        - remove Eclipse generated (non-Javadoc) block comments.
    CommonCodeInIfElseStatementRefactoring - Extract common code in if else statement (pre-configured)
        Factorizes common code in all if / else if / else statements either 
        at the start of each blocks or at the end.
        Ultimately it can completely remove the if statement condition.
    CommonIfInIfElseRefactoring - Move common inner if statement from then/else clauses around outer if statement (pre-configured)
        Moves an inner if statement around the outer if condition, when the 
        inner if condition is common to both if/else clauses of the outer if 
        statement.
    ComparisonRefactoring - Comparison to 0 rather than 1 or -1 (pre-configured)
        Fix Comparable.compareTo() usage.
    DeadCodeEliminationRefactoring - Dead code elimination (pre-configured)
        Removes dead code.
    DoWhileRatherThanWhileRefactoring - Do/while rather than while (pre-configured)
        Replace while by do/while when the first evaluation is always true.
    EnumMapRatherThanHashMapRefactoring - EnumMap rather than HashMap for enum keys (pre-configured)
        Refactor implementation class HashMap -> EnumMap when key is a enum 
        type
    EnumSetRatherThanHashSetRefactoring - EnumSet rather than HashSet for enum types (pre-configured)
        Converts creation of HashSet with enum as a type to invocation of 
        static methods of EnumSet where possible
    HashMapRatherThanHashtableRefactoring - HashMap rather than Hashtable (pre-configured)
        Replace Hashtable by HashMap when possible.
    HashMapRatherThanTreeMapRefactoring - HashMap rather than TreeMap (pre-configured)
        Replace TreeMap by HashMap when the entry order is not used.
    HashSetRatherThanTreeSetRefactoring - HashSet rather than TreeSet (pre-configured)
        Replace TreeSet by HashSet when the entry order is not used.
    HotSpotIntrinsicedAPIsRefactoring - HotSpot intrinsiced APIs (pre-configured)
        Refactors code patterns to use intrinsiced APIs in Hotspot JVM.
        Intrinsics are APIs that receive special treatment when JITed: they 
        can be compiled down to use very efficient CPU instructions.
    IfElseIfRefactoring - if-elseif (pre-configured)
        Refactors "else { if (...) {} }" to "else if (...) {}" to.
    InvertEqualsRefactoring - Equals on constant rather than on variable (pre-configured)
        Inverts calls to Object.equals(Object) and String.equalsIgnoreCase
        (String) when it is known that the second operand is not null and 
        the first can be null.
    IsEmptyRatherThanSizeRefactoring - Empty test rather than size (pre-configured)
        Replaces some checks on Collection.size() or Map.size() with checks 
        on isEmpty().
    JUnitAssertRefactoring - JUnit asserts (pre-configured)
        Refactors to a proper use of JUnit assertions.
    LazyLogicalRatherThanEagerRefactoring - Lazy logical rather than eager (pre-configured)
        Replace & by && and | by || when the right operand is passive.
    MapEliminateKeySetCallsRefactoring - Replace useless calls to Map.keySet() when direct calls to the Map are possible (pre-configured)
        Directly invoke methods on Map rather than on Map.keySet() where 
        possible,
        also convert for loops iterating on Map.keySet() to iterate on Map.
        entrySet() where possible.
    MapRefactoring - Inited map rather than new map and Map.putAll() (pre-configured)
        Replaces creating a new Map, then invoking Map.putAll() on it, by 
        creating the new Map with the other Map as parameter.
    MergeBlocksWithJumpRefactoring - One block with jump rather than duplicate blocks (pre-configured)
        Merge following if statements with same code block that end with a 
        jump statement.
    MergeConditionalBlocksRefactoring - Merge conditional statements (pre-configured)
        Merge adjacent if / else if / else statements with same code block.
    NoAssignmentInIfConditionRefactoring - No assignment in if condition (pre-configured)
        Moves assignments inside an if condition before the if node.
    NoSettingRatherThanUselessSettingRefactoring - No setting rather than useless setting (pre-configured)
        Remove passive assignment when the variable is reassigned before 
        being read.
    ORConditionRatherThanRedundantClausesRefactoring - OR condition rather than redundant clauses (pre-configured)
        Replace (X && Y) || !X by Y || !X.
    PrimitiveWrapperCreationRefactoring - Primitive wrapper creation (pre-configured)
        Replaces unnecessary primitive wrappers instance creations by using 
        static factory methods ("valueOf()") or existing constants.
    PushNegationDownRefactoring - Push negation down (pre-configured)
        Pushes negations down, inside the negated expressions.
    RemoveEmptyLinesRefactoring - Remove empty lines (pre-configured)
        Removes unnecessary empty lines from source code:
        - empty lines after opening braces,
        - empty lines before closing braces,
        - two consecutive empty lines are converted to a single empty line.
    RemoveEmptyStatementRefactoring - Removes empty statements (pre-configured)
        Removes lone semicolons.
    RemoveEmptySuperConstrInvocationRefactoring - Remove super() call in constructor (pre-configured)
        Remove call to super constructor with empty arguments since it is 
        redundant. See JLS section 12.5 for more info.
    RemoveFieldsDefaultValuesRefactoring - Remove fields default values (pre-configured)
        Removes field initializers when they are the default value of the 
        field's types.
        For example, the initializer will be removed for integer fields 
        initialized to "0".
        Likewise, the initializer will be removed for non primitive fields 
        initialized to "null".
    RemoveSemiColonRefactoring - Remove semi-colons (pre-configured)
        Removes superfluous semi-colon after body declarations in type 
        declarations.
    RemoveUncheckedThrowsClausesRefactoring - Remove unchecked exceptions from throws clause (pre-configured)
        Remove unchecked exceptions from throws clause
    RemoveUnnecessaryCastRefactoring - Remove unnecessary casts (pre-configured)
        Removes unnecessary widening casts from return statements, 
        assignments and infix expressions.
    RemoveUnnecessaryLocalBeforeReturnRefactoring - Remove unnecessary local before return (pre-configured)
        Removes unnecessary local variable declaration or unnecessary 
        variable assignment before a return statement.
    RemoveUnneededThisExpressionRefactoring - Remove unneeded this expressions (pre-configured)
        Remove useless use of "this" from method calls.
    RemoveUselessModifiersRefactoring - Remove useless modifiers (pre-configured)
        Fixes modifier order.
        Also removes modifiers implied by the context:
        - "public", "static" and "final" for interface fields,
        - "public" and "abstract" for interface methods,
        - "final" for parameters in interface method declarations.
    RemoveUselessNullCheckRefactoring - Remove useless null checks (pre-configured)
        Removes useless null checks before assignments or return statements.
        Such useless null checks are comparing an expression against null,
        then either assigning null or the expression depending on the result 
        of the null check.
        It is simpler to directly assign the expression.
    ReplaceQualifiedNamesBySimpleNamesRefactoring - Replace qualified names by simple names (pre-configured)
        Refactors types, method invocations and field accesses to replace 
        qualified names by simple names when appropriate. For example when 
        relevant imports exist.
    SetRatherThanListRefactoring - Set rather than List (pre-configured)
        Replace List by HashSet when only presence of items is used.
    SetRatherThanMapRefactoring - HashSet rather than HashMap (pre-configured)
        Replace HashMap by HashSet when values are not read.
    SimplifyExpressionRefactoring - Simplify expressions (pre-configured)
        Simplifies Java expressions:
        - remove redundant null checks or useless right-hand side or left-
        hand sie operands,
        - remove useless parentheses,
        - directly check boolean values instead of comparing them with 
        true/false,
        - reduce double negation in boolean expression.
    StringBuilderMethodRatherThanReassignationRefactoring - StringBuilder method call rather than reassignation (pre-configured)
        Removes the assignation to the same variable on a StringBuilder.
        append() call.
    StringBuilderRatherThanStringBufferRefactoring - StringBuilder rather than StringBuffer (pre-configured)
        Replace StringBuffer by StringBuilder when possible.
    StringBuilderRefactoring - StringBuilder (pre-configured)
        Refactors to a proper use of StringBuilders:
        - replace String concatenations using operator '+' as parameters of 
        StringBuffer/StringBuilder.append(),
        - replace chained call to StringBuffer/StringBuilder constructor 
        followed by calls to append() and call toString() with straight 
        String concatenation using operator '+'.
    StringRatherThanNewStringRefactoring - String rather than new string (pre-configured)
        Removes a String instance from a String constant or literal.
    StringRefactoring - String (pre-configured)
        Removes:
        - calling String.toString() on a String instance,
        - remove calls to String.toString() inside String concatenations,
        - replace useless case shifts for equality by equalsIgnoreCase().
    StringValueOfRatherThanConcatRefactoring - String.valueOf() rather than concatenation (pre-configured)
        Replace forced string tranformation by String.valueOf().
    SuperCallRatherThanUselessOverridingRefactoring - Super call rather than useless overriding (pre-configured)
        Removes overriding of method if the overriding only call the super 
        class.
    SwitchRefactoring - Switch (pre-configured)
        Switch related refactorings:
        - replaces if/else if/else blocks to use switch where possible.
    TernaryOperatorRatherThanDuplicateConditionsRefactoring - Ternary operator rather than duplicate conditions (pre-configured)
        Replace (X && Y) || (!X && Z) by X ? Y : Z.
    TestNGAssertRefactoring - TestNG asserts (pre-configured)
        Refactors to a proper use of TestNG assertions.
    TryWithResourceRefactoring - Use try-with-resource (pre-configured)
        Changes code to make use of Java 7 try-with-resources feature. In 
        particular, it removes now useless finally clauses.
    UpdateSetRatherThanTestingFirstRefactoring - Update set rather than testing first (pre-configured)
        Set related refactorings:
        - replaces calls to Set.contains() immediately followed by Set.add() 
        with straight calls to Set.add(),
        - replaces calls to Set.contains() immediately followed by Set.
        remove() with straight calls to Set.remove().
    UseDiamondOperatorRefactoring - Diamond operator (pre-configured)
        Refactors class instance creations to use the diamond operator 
        wherever possible.
    UseMultiCatchRefactoring - Multi-catch (pre-configured)
        Refactors catch clauses with the same body to use Java 7's multi-
        catch.
    UseStringContainsRefactoring - Use String.contains() (pre-configured)
        Replaces uses of String.indexOf(String) String.lastIndexOf(String) 
        with String.contains(CharSequence) where appropriate.
    VectorOldToNewAPIRefactoring - Collections APIs rather than Vector pre-Collections APIs (pre-configured)
        Replaces Vector pre-Collections APIs with equivalent Collections 
        APIs.
    WorkWithNullCheckedExpressionFirstRefactoring - Work with null checked expressions first (pre-configured)
        Refactors if statements with a null checked expression to work with 
        the not null case in the then clause and then work with the null 
        case in the else clause.
    XORRatherThanDuplicateConditionsRefactoring - XOR rather than duplicate conditions (pre-configured)
        Replace (X && !Y) || (!X && Y) by X ^ Y.
```

# AutoRefactor

The AutoRefactor project delivers free software that automatically refactor code bases.

The aim is to fix language/API usage in order to deliver smaller, more maintainable and more expressive code bases.

This is an Eclipse plugin to automatically refactor Java code bases.

You will find much more information on [http://autorefactor.org](http://autorefactor.org): goals, features, usage, samples, installation, links.

## License

AutoRefactor is distributed under the terms of both the
Eclipse Public License v1.0 and the GNU GPLv3+.

See LICENSE-ECLIPSE, LICENSE-GNUGPL, and COPYRIGHT for details.
