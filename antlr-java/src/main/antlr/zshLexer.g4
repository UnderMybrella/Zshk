lexer grammar zshLexer;

// Keywords

BUILTIN:            'builtin';
COMMAND:            'command';
EXEC:               'exec';
NOCORRECT:          'nocorrect';
NOGLOB:             'noglob';
COPROC:             'coproc';


IF:                 'if';
THEN:               'then';
ELIF:               'elif';
ELSE:               'else';
FI:                 'fi';
FOR:                'for';
IN:                 'in';
DO:                 'do';
DONE:               'done';
WHILE:              'while';
UNTIL:              'until';
REPEAT:             'repeat';
CASE:               'case';
ESAC:               'esac';
SELECT:             'select';
ALWAYS:             'always';
FUNCTION:           'function';


DECIMAL_LITERAL:    ('0' [dD])? ('0' | [1-9] (Digits? | '_'+ Digits)) [lL]?;
HEX_LITERAL:        '0' [xX] [0-9a-fA-F] ([0-9a-fA-F_]* [0-9a-fA-F])? [lL]?;
OCT_LITERAL:        '0' [oO] '_'* [0-7] ([0-7_]* [0-7])? [lL]?;
BINARY_LITERAL:     '0' [bB] [01] ([01_]* [01])? [lL]?;
HASH_LITERAL:       [0-9]+ '#' [0-9a-zA-Z] ([0-9a-zA-Z_]* [0-9a-zA-Z])? [lL]?;
BRACKET_LITERAL:    '[' [0-9]+ ']' [0-9a-zA-Z] ([0-9a-zA-Z_]* [0-9a-zA-Z])? [lL]?;

FLOAT_LITERAL:      (Digits '.' Digits? | '.' Digits) ExponentPart? [fFdD]?
             |       Digits (ExponentPart [fFdD]? | [fFdD])
             ;

//HEX_FLOAT_LITERAL:  '0' [xX] (HexDigits '.'? | HexDigits? '.' HexDigits) [pP] [+-]? Digits [fFdD]?;

BOOL_LITERAL:       'true'
            |       'false'
            ;

CHAR_LITERAL:       '\'' (~['\\\r\n] | EscapeSequence) '\'';
//STRING_LITERAL:     '"' (~["\\\r\n] | EscapeSequence)* '"';

SUBSTITUTION_QUOTING:   '"' -> pushMode(QuotingMode);

//TEXT_BLOCK:         '"""' [ \t]* [\r\n] (. | EscapeSequence)*? '"""';

NULL_LITERAL:       'null';

// Separators

LPAREN:             '(';
RPAREN:             ')';
LBRACE:             '{';
RBRACE:             '}';
LBRACK:             '[';
RBRACK:             ']';
SEMI:               ';';
COMMA:              ',';
DOT:                '.';

// Operators

ASSIGN:             '=';
GT:                 '>';
LT:                 '<';
BANG:               '!';
TILDE:              '~';
QUESTION:           '?';
COLON:              ':';
EQUAL:              '==';
LE:                 '<=';
GE:                 '>=';
NOTEQUAL:           '!=';
AND:                '&&';
OR:                 '||';
INC:                '++';
DEC:                '--';
ADD:                '+';
SUB:                '-';
MUL:                '*';
DIV:                '/';
BITAND:             '&';
BITOR:              '|';
CARET:              '^';
MOD:                '%';

ADD_ASSIGN:         '+=';
SUB_ASSIGN:         '-=';
MUL_ASSIGN:         '*=';
DIV_ASSIGN:         '/=';
AND_ASSIGN:         '&=';
OR_ASSIGN:          '|=';
XOR_ASSIGN:         '^=';
MOD_ASSIGN:         '%=';
LSHIFT_ASSIGN:      '<<=';
RSHIFT_ASSIGN:      '>>=';
URSHIFT_ASSIGN:     '>>>=';

// Java 8 tokens

ARROW:              '->';
COLONCOLON:         '::';

// Additional symbols not defined in the lexical specification

AT:                 '@';
ELLIPSIS:           '...';

// Whitespace and comments

NL:                 [\r\n];
WS:                 [ \t\r\n\u000C] -> channel(HIDDEN);
COMMENT:            '/*' .*? '*/'    -> channel(HIDDEN);
LINE_COMMENT:       '//' ~[\r\n]*    -> channel(HIDDEN);

// Identifiers

IDENTIFIER:         Letter LetterOrDigit*;

EXIT_CODE_VAR_REF:  '$?';
VARIABLE_REFERENCE: '$' IDENTIFIER;
SUBSHELL:           '$(';

// Fragment rules

fragment ExponentPart
    : [eE] [+-]? Digits
    ;

fragment EscapeSequence
    : '\\' [btnfr"'\\]
    | '\\' ([0-3]? [0-7])? [0-7]
    | '\\' 'u'+ HexDigit HexDigit HexDigit HexDigit
    ;

fragment HexDigits
    : HexDigit ((HexDigit | '_')* HexDigit)?
    ;

fragment HexDigit
    : [0-9a-fA-F]
    ;

fragment Digits
    : [0-9] ([0-9_]* [0-9])?
    ;

fragment LetterOrDigit
    : Letter
    | [0-9]
    ;

fragment Letter
    : [a-zA-Z$_] // these are the "java letters" below 0x7F
    | ~[\u0000-\u007F\uD800-\uDBFF] // covers all characters above 0x7F which are not a surrogate
    | [\uD800-\uDBFF] [\uDC00-\uDFFF] // covers UTF-16 surrogate pairs encodings for U+10000 to U+10FFFF
    ;

mode QuotingMode;

ESCAPES: '\\' ('u' HexDigits HexDigits HexDigits HexDigits | 'x' HexDigits HexDigits | .);
STRING_CHARACTERS: ~["\\\n$]+;
QUOTED_STRING_EXIT_CODE_VAR_REF: EXIT_CODE_VAR_REF -> type(EXIT_CODE_VAR_REF);
QUOTED_STRING_VARIABLE_REFERENCE: VARIABLE_REFERENCE -> type(VARIABLE_REFERENCE);

fragment ESCAPE_CHARACTERS: ["\\/bfnrt$&];

END_QUOTED_STRING: '"' -> popMode;