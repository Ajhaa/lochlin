package lox

import lox.TokenType.*

fun keywords() : Map<String, TokenType> {
    var keywords : HashMap<String, TokenType> = HashMap()

    keywords.put("and",    AND)
    keywords.put("class",  CLASS)
    keywords.put("else",   ELSE)
    keywords.put("false",  FALSE)
    keywords.put("for",    FOR)
    keywords.put("fun",    FUN)
    keywords.put("if",     IF)
    keywords.put("nil",    NIL)
    keywords.put("or",     OR)
    keywords.put("print",  PRINT)
    keywords.put("return", RETURN)
    keywords.put("super",  SUPER)
    keywords.put("this",   THIS)
    keywords.put("true",   TRUE)
    keywords.put("var",    VAR)
    keywords.put("while",  WHILE)

    return keywords
}