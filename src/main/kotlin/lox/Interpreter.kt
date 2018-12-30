package lox;

import lox.TokenType.*

class Interpreter() : Expr.Visitor<Any?> {

    fun interpret(expr : Expr?) {
        try {
            val value = evaluate(expr)
            println(stringify(value))
        } catch (error : RuntimeError) {
            Lox.runtimeError(error)
        }
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            MINUS -> {
                checkNumberOperand(expr.operator, right)
                return -(right as Double)
            }
            BANG -> return !isTruthy(right)
            else -> return null
        }
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double > right as Double
            }
            LESS -> {
                checkNumberOperands(expr.operator, left, right)
                return right as Double > left as Double
            }
            GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double >= right as Double
            }
            LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double <= right as Double
            }
            BANG_EQUAL -> {
                return !isEqual(left, right)
            }
            EQUAL_EQUAL -> {
                return isEqual(left, right)
            }
            MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double - right as Double
            }
            SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double / right as Double
            }
            STAR -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double * right as Double
            }
            PLUS -> {
                if (left is Double && right is Double) {
                    return left + right
                }

                if (left is String && right is String) {
                    return left + right
                }

                throw RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.")
            }
            else -> return null
        }
    }

    private fun isTruthy(obj : Any?) : Boolean{
        if (obj  == null) return false
        if (obj is Boolean) return obj

        return true

    }

    private fun isEqual(a : Any?, b : Any?) : Boolean {
        if (a == null && b == null) {
            return true
        }

        if (a == null) return false

        return a.equals(b)
    }

    private fun  stringify(obj : Any?) : String {
        if (obj == null) return "nil"

        if (obj is Double) {
           if (obj % 1.0 == 0.0) {
               return obj.toString().dropLast(1)
           }
        }

        return obj.toString()
    }

    private fun evaluate(expr : Expr?) : Any? {
        return expr!!.accept(this)
    }

    private fun checkNumberOperand(operator : Token, operand : Any?) {
        if (operand is Double) return;
        throw RuntimeError(operator, "Operand must be a number.")
    }

    private fun checkNumberOperands(operator: Token,
                                    left: Any?, right: Any?) {

        if (left is Double && right is Double) return

        throw RuntimeError(operator, "Operands must be numbers.")
    }
}
