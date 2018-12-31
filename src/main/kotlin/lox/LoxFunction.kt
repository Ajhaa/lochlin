package lox

class LoxFunction(private val declaration : Stmt.Function) : LoxCallable {

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any? {
        var environment = Environment(interpreter.globals)
        for (i in 0 until declaration.params.size) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i))
        }

        try{
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue : Return) {
            return returnValue.value
        }
        return null
    }

    override fun arity(): Int {
        return declaration.params.size
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}