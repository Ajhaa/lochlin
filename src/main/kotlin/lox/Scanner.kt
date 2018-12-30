package lox

import lox.TokenType.*;
class Scanner(private val source : String) {
    private var tokens : MutableList<Token> = ArrayList()
    private var keywords = lox.keywords()

    private var start = 0
    private var current = 0
    private var line = 1

    fun scanTokens() : List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(EOF, "", null, line))
        return tokens
    }

    private fun isAtEnd() : Boolean {
        return current >= source.length
    }

    private fun scanToken() {

        var c = advance()
        when (c) {
            '(' -> addToken(LEFT_PAREN)
            ')' -> addToken(RIGHT_PAREN)
            '{' -> addToken(LEFT_BRACE)
            '}' -> addToken(RIGHT_BRACE)
            ',' -> addToken(COMMA)
            '.' -> addToken(DOT)
            '-' -> addToken(MINUS)
            '+' -> addToken(PLUS)
            ';' -> addToken(SEMICOLON)
            '*' -> addToken(STAR)
            '!' -> addToken(if (match('=')) BANG_EQUAL else BANG)
            '=' -> addToken(if (match('=')) EQUAL_EQUAL else EQUAL)
            '<' -> addToken(if (match('=')) LESS_EQUAL else LESS)
            '>' -> addToken(if (match('=')) GREATER_EQUAL else GREATER)
            '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(SLASH)
                }
            }
            ' ',  '\r',  '\t' -> {} //ignore these three
            '\n' -> line++
            '"' -> string()
            else -> {
                if (isDigit(c)) {
                    number()
                } else if (isAlpha(c)) {
                    identifier()
                } else {
                    Lox.error(line, "Unexpected character.")
                }
            }
        }
    }

    private fun identifier() {
        while (isAlphaNumberic(peek())) advance()

        val text = source.substring(start, current)

        var type = keywords.get(text)
        if (type == null)  type = IDENTIFIER
        addToken(type)
    }

    private fun number() {
        while (isDigit(peek())) advance()

        if (peek() == '.' && isDigit(peekNext())) {
            advance()

            while (isDigit(peek())) advance()
        }

        addToken(NUMBER, source.substring(start, current).toDouble())
    }

    private fun string() : TokenType? {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string")
            return null
        }

        advance()
        var value = source.substring(start + 1, current - 1)
        addToken(STRING, value)
        return null
    }

    private fun match(c : Char) : Boolean {
        if (isAtEnd()) return false
        if (source.get(current) != c) return false

        current++
        return true
    }

    private fun peek() : Char {
        if (isAtEnd()) return '\uFF00'
        return source.get(current)
    }

    private fun peekNext() : Char {
        if (current + 1 >= source.length) return '\uFF00'

        return source.get(current + 1)
    }

    private fun isAlpha(c : Char) : Boolean {
        return  (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c == '_')
    }

    private fun isAlphaNumberic(c : Char) : Boolean {
        return isAlpha(c) || isDigit(c)
    }

    private fun isDigit(c : Char) : Boolean {
        return c >= '0' && c <= '9'
    }

    private fun advance() : Char {
        current++
        return source.get(current - 1)
    }

    private fun addToken(type : TokenType) {
        addToken(type, null)
    }

    private fun addToken(type : TokenType, literal : Any?) {
        var text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }
}