/*
<project ...>
   <target name="gen-antlr" description="Generate parser from ANTLR grammar">
    <!-- <mkdir dir="src/scad2d" /> -->
    <java classname="org.antlr.v4.Tool" fork="true" failonerror="true">
        <classpath>
            <pathelement location="/home/moi/NetBeansProjects/VisualGCodeEditor/ext/antlr4-4.13.1-complete.jar" />
        </classpath>
        <arg value="-Dlanguage=Java" />
        <arg value="-visitor" />
        <arg value="-o" />
        <arg value="." />
        <arg value="src/scad2d/Scad2D.g4" />
    </java>
    </target>
</project>
*/
grammar Scad2D;

@header { package scad2d; }

file: statement* EOF;

body
    : statement                          // version courte : une seule instruction
    | '{' statement* '}'                 // version bloc : plusieurs instructions
    ;

statement
    : assignment
    | ifStatement
    | forStatement
    | moduleDef
    | circleExpr
    | rectangleExpr
    | polygonExpr
    | translateExpr
    | scaleExpr
    | rotateExpr
    | unionCall
    | differenceCall
    | intersectionCall
    | mirrorCall
    | hullCall
    | colorCall
    | echoCall
    | moduleCall
    ;


assignment: (FN|ID) '=' (expr | list) ';' ;

ifStatement: 'if' '(' expr ')' body ('else' body)? ;

forStatement: 'for' '(' ID '=' rangeExpr ')' body ;
rangeExpr: '[' expr ':' expr (':' expr)? ']' ;

echoCall       : 'echo' '(' strExpr ('+' strExpr)*  ')'  ';' ;
strExpr : STRING | expr ;

colorCall      : 'color' '(' strExpr ('+' strExpr)* ')' body ;
translateExpr  : ('translate'|'trans') '(' exprList ')' body ;
scaleExpr      : 'scale' '(' exprList ')' body ;
rotateExpr     : 'rotate' '(' expr ')' body ;
unionCall      : 'union' '(' ')' body ;
differenceCall : ('difference'|'diff') '(' ')' body ;
intersectionCall : ('intersection'|'inter') '(' ')' body ;
hullCall       : 'hull' '(' ')' body ;
mirrorCall     : 'mirror' '(' list ')' body ;

moduleDef: 'module' ID '(' paramList? ')' body ;
moduleCall: ID '(' (expr (',' expr)* )? ')' ';' ;

paramList: ID (',' ID)* ;

circleExpr: 'circle' '(' (ID '=')? expr (',' FN '=' expr)? ')' ';' ;
rectangleExpr: ( 'square' | 'rect' | 'rectangle' | 'cube') '(' ( ID '=' expr ',' ID '=' expr | expr ) ')' ';' ;
polygonExpr: ( 'poly' | 'polygon')  '(' (ID | list) ')' ';' ;

list: '[' exprList (',' exprList)* ']' ;
exprList : list | expr ;

expr:
      op=('-'|'!') expr
    | expr op=('*' | '/' | '%') expr
    | expr op=('+' | '-') expr
    | expr op=('>' | '>=' | '<' | '<=') expr
    | expr op=('=='| '!=') expr
    | expr op=('&&'| '||') expr
    | ('true'|'false'|'PI')
    | ('abs'|'sin'|'cos'|'tan'|'int') '(' expr ')' 
    | ('min'|'max') '(' expr (',' expr)* ')' 
    | NUMBER
    | ID '[' expr ']' ('[' expr ']')*
    | ID
    | '(' expr ')'
    | list
    | expr '?' expr ':' expr
    ;

WS: [ \t\r\n]+ -> skip ;
COMMENT: '//' ~[\r\n]* -> skip ;
MULTILINE_COMMENT: '/*' .*? '*/' -> skip ;

ID: [a-zA-Z_][a-zA-Z_0-9]* ;
NUMBER: [0-9]+ ('.' [0-9]+)? ;
STRING: '"' .*? '"' ;
FN: '$fn' ;
