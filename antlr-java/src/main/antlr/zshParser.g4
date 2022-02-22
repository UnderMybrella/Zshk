parser grammar zshParser;

options { tokenVocab=zshLexer; }

script: listWithOptionalTerminator;

simpleOrComplexCommand: simpleCommand | complexCommands;

simpleCommand: commandModifier? commandName=literal (literal | shortFlagGroupWithOptionalValue | longFlagGroupWithOptionalValue)*;
pipeline: COPROC? simpleOrComplexCommand (pipe simpleOrComplexCommand)*;
sublist: pipeline (sublistJoiner pipeline)*;

pipe
    : '|'
    | '|' '&'
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

whileDoDone: WHILE list DO list DONE;
untilDoDone: UNTIL list DO list DONE;
repeatDoDone: REPEAT literal DO list DONE;
caseInEsac: CASE literal IN ('('? literal ('|' literal)* ')' list (';' (';' | '&' | '|')))* ESAC;
selectDoDone: SELECT literal (IN literal+ (';' | NL)+) DO list DONE;

forkProcess: '(' listWithOptionalTerminator ')';
codeblock: '{' listWithOptionalTerminator '}';
tryAlways:  '{' tryList=list '}' ALWAYS (';' | NL)* '{' alwaysList=list '}';
functionDeclaration: FUNCTION identifier ('(' ')')? (';' | NL)* '{' list '}';
simpleFunctionListDeclaration: identifier '(' ')' (';' | NL)* '{' list '}';
simpleFunctionSingleDeclaration: identifier '(' ')' (';' | NL)* '{' simpleCommand '}';

conditional
    : list
    | conditionalExpression
    ;

conditionalExpression
    : '[' '[' identifier ']' ']'
    ;

expression
    : flaggedExpression
    | literal shortFlagGroup literal
    | literal '=' '='? literal
    | literal '!' '=' literal
    | literal '=' '~' literal
    | literal '<' literal
    | literal '>' literal
    | '(' expression ')'
    | '!' expression
    | expression AND expression
    | expression OR expression
    ;

flaggedExpression: shortFlagGroup literal;

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
    | '$(' '(' arithmeticExpression ')' ')'
//    | TEXT_BLOCK // Java17
    ;

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
        )*
      END_QUOTED_STRING
    ;

variableReference
    : VARIABLE_REFERENCE
    | EXIT_CODE_VAR_REF
    ;

arithmeticExpression
    : (arithmeticStatement (',' arithmeticStatement)*)?
    ;

arithmeticStatement
    :   '(' arithmeticStatement ')' #GroupedOperation
    |   arithmeticStatement '?' arithmeticStatement ':' arithmeticStatement #TernaryExpression
    |   IDENTIFIER arithmeticAssignmentOperator arithmeticStatement #Assignment
    |   arithmeticStatement arithmeticOperator arithmeticStatement #Operation
    |   ('++' | '--' | '!' | '~' | '+' | '-')?
            (
                integerLiteral
                | floatLiteral
                | identifier
            )
        ('++' | '--')? #LiteralValue
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