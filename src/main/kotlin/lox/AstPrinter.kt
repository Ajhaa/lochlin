package lox

class AstPrinter : Expr.Visitor<String> {
    fun print(expr : Expr?) : String {
        return expr!!.accept(this)
    }

    override fun visitBinaryExpr(expr: Expr.Binary): String {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): String {
        return parenthesize("group", expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): String {
        return expr.value.toString()
    }

    override fun visitUnaryExpr(expr: Expr.Unary): String {
        return parenthesize(expr.operator.lexeme, expr.right)
    }

    override fun visitVariableExpr(expr: Expr.Variable): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitAssignExpr(expr: Expr.Assign): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitLogicalExpr(expr: Expr.Logical): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitCallExpr(expr: Expr.Call): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun parenthesize(name : String, vararg exprs : Expr) : String {
        var builder = StringBuilder()

        builder.append("(").append(name)

        for (expr in exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }

        builder.append(")")

        return builder.toString()
    }
}