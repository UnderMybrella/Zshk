parser grammar zshParser;

options { tokenVocab=zshLexer; }

script: listWithOptionalTerminator;

simpleOrComplexCommand: simpleCommand | complexCommands;

simpleCommand: commandModifier? commandName=literal (literal | shortFlagGroupWithOptionalValue | longFlagGroupWithOptionalValue)*;
pipeline: COPROC? simpleOrComplexCommand (pipe simpleOrComplexCommand)*;
sublist: pipeline (sublistJoiner pipeline)*;

pipe
    : '|' #PipeStdoutToStdin
    | '|' '&' #PipeStdoutAndStderrToStdin
    ;

sublistJoiner
    : '&&'
    | '||'
    ;

list
    : sublist? (listTerminator sublist)* listTerminator
    | '(' sublist? (listTerminator sublist)* listTerminator* ')'
    | '{' sublist? (listTerminator sublist)* listTerminator* '}'
    ;
listWithOptionalTerminator: sublist? (listTerminator sublist)* listTerminator*;

listTerminator: listTerminatorWait | listTerminatorBackground;

listTerminatorWait: (';' | NL)+;

listTerminatorBackground: '&' ('|' | '!')?;

complexCommands
    : ifThenFi
    | ifElifElse
    | ifSingular
    | forInDoDone
    | forLiteralsDoDone
    | whileDoDone
    | untilDoDone
    | repeatDoDone
    | caseInEsac
    | selectDoDone
    | forkProcess
    | codeblock
    | tryAlways
    | functionDeclaration
    | simpleFunctionListDeclaration
    | simpleFunctionSingleDeclaration
    | conditionalExpression
    ;

ifThenFi: IF conditional THEN list (ELIF conditional THEN list)* (ELSE list)? FI;
ifElifElse: IF conditional '{' list '}' (ELIF conditional '{' list '}')* (ELSE '{' list '}')?;
ifSingular: IF conditional sublist;

forInDoDone: FOR identifier+ (IN literal+ (';' | NL)+ | '(' literal+ ')' (';' | NL)*) DO list DONE;
forLiteralsDoDone: FOR '(' '(' literal ';' literal ';' literal ')' ')' DO list DONE;

whileDoDone: WHILE conditional DO list DONE;
untilDoDone: UNTIL conditional DO list DONE;
repeatDoDone: REPEAT arithmeticStatement DO list DONE;
caseInEsac: CASE literal IN casePattern* ESAC;
selectDoDone: SELECT literal (IN literal+ (';' | NL)+) DO list DONE;

casePattern: '('? literal ('|' literal)* ')' list caseTerminator;
caseTerminator: (';' (';' | '&' | '|'));

forkProcess: '(' listWithOptionalTerminator ')';
codeblock: '{' listWithOptionalTerminator '}';
tryAlways:  '{' tryList=list '}' ALWAYS (';' | NL)* '{' alwaysList=list '}';
functionDeclaration: FUNCTION identifier ('(' ')')? (';' | NL)* '{' list '}';
simpleFunctionListDeclaration: identifier '(' ')' (';' | NL)* '{' list '}';
simpleFunctionSingleDeclaration: identifier '(' ')' (';' | NL)* simpleOrComplexCommand;

conditional
    : list
    | conditionalExpression
    ;

conditionalExpression
    : '[' '[' expression ']' ']'
    ;

expression
    : literal #expressionLiteral
    | shortFlagGroup literal #expressionShortFlagLiteral
    | literal shortFlagGroup literal #expressionShortFlagLiterals
    | literal '=' '='? literal #expressionStringEquals
    | literal '!' '=' literal #expressionStringNotEquals
    | literal '=' '~' literal #expressionStringNotEqualsRegexp
    | literal '<' literal #expressionStringLessThan
    | literal '>' literal #expressionStringGreaterThan
    | '(' expression ')' #expressionIsTrue
    | '!' expression #expressionIsFalse
    | expression AND expression #expressionAnd
    | expression OR expression #expressionOr
    ;

commandModifier
    : SUB
    | BUILTIN
    | COMMAND shortFlagGroup*
    | EXEC shortFlagGroupWithOptionalValue*
    | NOCORRECT
    | NOGLOB
    ;

identifier
    : IDENTIFIER
    | SUB
    | BUILTIN
    | COMMAND
    | EXEC
    | NOCORRECT
    | NOGLOB
    | BOOL_LITERAL
    | NULL_LITERAL
    ;

literal
    : integerLiteral
    | floatLiteral
    | CHAR_LITERAL
    | quotedString
    | BOOL_LITERAL
    | NULL_LITERAL
    | identifier
    | variableReference
    | commandSubstitutionLiteral
    | arithmeticLiteral
//    | TEXT_BLOCK // Java17
    ;

commandSubstitutionLiteral: '$(' listWithOptionalTerminator ')';
arithmeticLiteral: ARITHMETIC_OPEN arithmeticExpression ARITHMETIC_CLOSE;

integerLiteral
    : DECIMAL_LITERAL
    | HEX_LITERAL
    | OCT_LITERAL
    | BINARY_LITERAL
    | HASH_LITERAL
    | BRACKET_LITERAL
    ;

floatLiteral
    : FLOAT_LITERAL
//    | HEX_FLOAT_LITERAL
    ;

shortFlagGroup: SUB name=IDENTIFIER;
longFlagGroup: DEC name=IDENTIFIER;

shortFlagGroupWithOptionalValue: shortFlagGroup ('=' value=literal)?;
longFlagGroupWithOptionalValue: longFlagGroup ('=' value=literal)?;

quotedString
    : SUBSTITUTION_QUOTING
        (ESCAPES
        | STRING_CHARACTERS
        | variableReference
        | commandSubstitutionLiteral
        | arithmeticLiteral
        )*
      END_QUOTED_STRING
    ;

variableReference
    : VARIABLE_REFERENCE
    ;

arithmeticExpression
    : (arithmeticStatement (',' arithmeticStatement)*)?
    ;

arithmeticStatement
    :   arithmeticModifier* '(' arithmeticStatement ')' #GroupedArithmeticOperation
    |   arithmeticStatement '?' arithmeticStatement ':' arithmeticStatement #TernaryArithmeticExpression
    |   IDENTIFIER arithmeticAssignmentOperator arithmeticStatement #ArithmeticAssignment
    |   arithmeticStatement arithmeticOperator arithmeticStatement #ArithmeticOperation
    |   arithmeticModifier* ('++' | '--')? identifier ('++' | '--')? #ArithmeticVariableReference
    |   arithmeticModifier* (integerLiteral | floatLiteral) #ArithmeticNumericalLiteral
    ;

arithmeticModifier
    : '!'
    | '~'
    | '+'
    | '-'
    ;

arithmeticAssignmentOperator
    : '='
    | '+='
    | '-='
    | '*='
    | '/='
    | '%='
    | '^='
    | '|='
    | '<<='
    | '>>='
    | '&' '&' '='
    | '|' '|' '='
    | '^' '^' '='
    | '*' '*' '='
    ;

arithmeticOperator
    : '<' '<'
    | '>' '>'
    | '&'
    | '^'
    | '|'
    | '*' '*'
    | '*'
    | '/'
    | '%'
    | '+'
    | '-'
    | '<'
    | '>'
    | '<='
    | '>='
    | '=='
    | '!='
    | '&&'
    | '||'
    | '^' '^'
    ;