lexer grammar MicroLexicon;

KEYWORD
  : 'PROGRAM' | 'BEGIN' | 'END' | 'FUNCTION'
  | 'READ' | 'WRITE' | 'IF' | 'ELSIF' | 'ENDIF'
  | 'DO' | 'WHILE' | 'CONTINUE' | 'BREAK'
  | 'RETURN' | 'INT' | 'VOID' | 'STRING' | 'FLOAT'
  | 'TRUE' | 'FALSE' ;

OPERATOR : [+\-*/=<>(),;] | ':=' | '!=' | '<=' | '>=' ;
 
IDENTIFIER : LETTER (LETTER | DIGIT)* ;

INTLITERAL : DIGIT+ ;
FLOATLITERAL : DIGIT* DOT DIGIT+ ;
STRINGLITERAL : DQ (EOS|.)*? DQ ;

WHITESPACE : [ \t\r\n]+ -> skip ;
COMMENT : '--' ~[\r\n]* -> skip ;

fragment DIGIT : [0-9] ;
fragment LETTER : [a-zA-Z] ;
fragment DQ : '"' ;

fragment DOT : '.' ;
fragment EOS : '\\0' ;
fragment ESC : '\\' ;
