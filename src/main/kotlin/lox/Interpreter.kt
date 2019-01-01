package lox;

import lox.TokenType.*

class Interpreter() : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {
    val globals = lox.globals()
    private var environment = globals
    private val locals = HashMap<Expr, Int?>()

    fun interpret(statements : List<Stmt>) {
        try {
            for (stmt in statements) {
                execute(stmt)
            }
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
            PERCENT -> {
                checkNumberOperands(expr.operator, left, right)
                return left as Double % right as Double
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

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = evaluate(expr.value)

        val distance = locals.get(expr)
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }
        return value
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return lookUpVariable(expr.name, expr)
    }

    private fun lookUpVariable(name: Token, expr: Expr) : Any? {
        val distance = locals.get(expr)

        if (distance != null) {
            return environment.getAt(distance, name.lexeme)
        } else {
            return globals.get(name)
        }
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)

        var arguments = ArrayList<Any>()
        for (arg in expr.arguments) {
            val expr = evaluate(arg)
            expr?.let { arguments.add(expr) }
        }

        if (callee !is LoxCallable) {
            throw RuntimeError(expr.paren, "Can only call functions and classes")
        }
        if (arguments.size != callee.arity()) {
            throw RuntimeError(expr.paren,
                    "Expected ${callee.arity()} arguments, but got ${arguments.size}.")
        }
        return callee.call(this, arguments)
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val obj = evaluate(expr.obj)
        if (obj is LoxInstance) {
            return obj.get(expr.name)
        }

        throw RuntimeError(expr.name, "Only instances have properties")
    }

    override fun visitFunctionStmt(stmt: Stmt.Function) {
        val function = LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
    }

    override fun visitReturnStmt(stmt: Stmt.Return) {
        val value = stmt.value?.let { evaluate(it) }

        throw Return(value)
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
               return obj.toString().dropLast(2)
           }
        }

        return obj.toString()
    }

    private fun evaluate(expr : Expr?) : Any? {
        return expr!!.accept(this)
    }

    private fun execute(stmt : Stmt) {
        stmt.accept(this)
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == OR) {
            if (isTruthy(left)) return left
        } else {
            if (!isTruthy(left)) return left
        }

        return evaluate(expr.right)
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val obj = evaluate(expr.obj)
        if (obj !is LoxInstance) {
            throw RuntimeError(expr.name, "Only instances have fields")
        }

        val value = evaluate(expr.value)
        obj.set(expr.name, value)
        return value
    }

    override fun visitThisExpr(expr: Expr.This): Any? {
        return lookUpVariable(expr.keyword, expr)
    }

    override fun visitBlockStmt(stmt: Stmt.Block) {
        executeBlock(stmt.statements, Environment(environment))
    }

    override fun visitClassStmt(stmt: Stmt.Class) {
        environment.define(stmt.name.lexeme, null)

        val methods = HashMap<String, LoxFunction>()
        for (method in stmt.methods) {
            val function = LoxFunction(method, environment, method.name.lexeme == "init")
            methods.put(method.name.lexeme, function)
        }

        val klass = LoxClass(stmt.name.lexeme, methods)
        environment.assign(stmt.name, klass)
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression) {
        evaluate(stmt.expression)
    }

    override fun visitIfStmt(stmt: Stmt.If) {
        if (isTruthy(evaluate(stmt.condition))) {
            stmt.thenBranch?.let { execute(it) }
        } else {
            stmt.elseBranch?.let { execute(it) }
        }
    }

    override fun visitPrintStmt(stmt: Stmt.Print) {
        val value = evaluate(stmt.expression)
        println(stringify(value))
    }

    override fun visitVarStmt(stmt: Stmt.Var) {
        var value : Any? = null

        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }

        environment.define(stmt.name.lexeme, value)
    }

    override fun visitWhileStmt(stmt: Stmt.While) {
        while (isTruthy(evaluate(stmt.condition))) {
            stmt.body?.let{ execute(it) }
        }
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

    fun resolve(expr: Expr, depth: Int) {
        locals[expr] = depth
    }

    fun executeBlock(statements : List<Stmt>, environment: Environment) {
        val previous = this.environment

        try {
            this.environment = environment

            for (stmt in statements) {
                execute(stmt)
            }
        } finally {
            this.environment = previous
        }
    }
}
