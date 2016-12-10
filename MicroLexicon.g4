lexer grammar MicroLexicon;

KEYWORD
  : 'PROGRAM' | 'BEGIN' | 'END' | 'FUNCTION'
  | 'READ' | 'WRITE' | 'IF' | 'ELSIF' | 'ENDIF'
  | 'DO' | 'WHILE' | 'CONTINUE' | 'BREAK'
  | 'RETURN' | 'INT' | 'VOID' | 'STRING' | 'FLOAT'
  | 'TRUE' | 'FALSE' ;

OPERATOR : [+\-*/=<>(),;] | ':=' | '!=' | '<=' | '>=' ;
 
IDENTIFIER : LETTER (LETTER | DIGIT)* {
    if (getText().length() > 32)
        throw new MicroRuntimeException("Identifier is more than 30 characters");
};

INTLITERAL : DIGIT+ ;
FLOATLITERAL : DIGIT* DOT DIGIT+ ;
STRINGLITERAL : DQ (EOS|.)*? DQ {
    if (getText().length() > 81)
        throw new MicroRuntimeException("String literal is more than 80 characters");
};

WHITESPACE : [ \t\r\n]+ -> skip ;
COMMENT : '--' ~[\r\n]* -> skip ;

fragment DIGIT : [0-9] ;
fragment LETTER : [a-zA-Z] ;
fragment DQ : '"' ;

fragment DOT : '.' ;
fragment EOS : '\\0' ;
fragment ESC : '\\' ;
