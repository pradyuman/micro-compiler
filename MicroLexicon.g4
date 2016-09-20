lexer grammar MicroLexicon;
@members {
  boolean checkLength(int test, int control) throws RuntimeException {
    if (test > 30) throw new RuntimeException("Length out of bounds");
    return true;
  }
}

KEYWORD
  : 'PROGRAM' | 'BEGIN' | 'END' | 'FUNCTION'
  | 'READ' | 'WRITE' | 'IF' | 'ELSIF' | 'ENDIF'
  | 'DO' | 'WHILE' | 'CONTINUE' | 'BREAK'
  | 'RETURN' | 'INT' | 'VOID' | 'STRING' | 'FLOAT'
  | 'TRUE' | 'FALSE' ;

OPERATOR : [+\-*/=<>(),;] | ':=' | '!=' | '<=' | '>=' ;
 
IDENTIFIER : LETTER (LETTER | DIGIT)* ; // {checkLength(getText().length(), 30)}?;

INTLITERAL : DIGIT+ ;
FLOATLITERAL : DIGIT* DOT DIGIT+ ;
STRINGLITERAL : DQ (EOS|.)*? DQ ; // {checkLength(getText().length(), 80)}?;

WHITESPACE : [ \t\r\n]+ -> skip ;
COMMENT : '--' ~[\r\n]* -> skip ;

fragment DIGIT : [0-9] ;
fragment LETTER : [a-zA-Z] ;
fragment DQ : '"' ;

fragment DOT : '.' ;
fragment EOS : '\\0' ;
fragment ESC : '\\' ;
