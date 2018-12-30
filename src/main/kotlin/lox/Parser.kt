package lox

import lox.TokenType.*;
import java.time.temporal.TemporalAdjusters.previous
import java.time.temporal.TemporalAdjusters.previous





class Parser(private val tokens : List<Token>) {
    private class ParseError : RuntimeException()

    private var current = 0

    fun parse() : Expr? {
        try {
            return expression()
        } catch (error : ParseError) {
            return null
        }
    }

    private fun expression() : Expr {
        return equality()
    }

    private fun equality() : Expr {
        var expr = comparison()

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison() : Expr {
        var expr = addition()

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            val operator = previous()
            val right = addition()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun addition() : Expr {
        var expr = multiplication()

        while (match(MINUS, PLUS)) {
            val operator = previous()
            val right = multiplication()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr;
    }

    private fun multiplication() : Expr {
        var expr = unary()

        while (match(SLASH, STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary() : Expr {
        if (match(BANG, MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary() : Expr {
        if (match(FALSE)) return Expr.Literal(false)
        if (match(TRUE)) return Expr.Literal(true)
        if (match(NIL)) return Expr.Literal(null)

        if (match(NUMBER, STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(LEFT_PAREN)) {
            val expr = expression()
            consume(RIGHT_PAREN, "Expect ')' after expression")
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expect expression")
    }

    private fun match(vararg types : TokenType) : Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun consume(type : TokenType, message : String) : Token {
        if (check(type)) return advance()

        throw error(peek(), message)
    }

    private fun check(type : TokenType) : Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance() : Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd() : Boolean {
        return peek().type == EOF
    }

    private fun peek() : Token {
        return tokens.get(current)
    }

    private fun previous() : Token {
        return tokens.get(current - 1)
    }

    private fun error(token : Token, message : String) : ParseError {
        Lox.error(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            when (peek().type) {
                CLASS, FUN, VAR, FOR, IF, WHILE, PRINT, RETURN -> return
                else -> advance()
            }
        }
    }
}