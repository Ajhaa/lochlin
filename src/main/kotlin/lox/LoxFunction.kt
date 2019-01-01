package lox

class LoxFunction(private val declaration : Stmt.Function,
                  private val closure : Environment,
                  private val isInitializer: Boolean) : LoxCallable {


    fun bind(instance: LoxInstance) : LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInitializer)
    }

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any? {
        val environment = Environment(closure)

        for (i in 0 until declaration.params.size) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i))
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue : Return) {
            if (isInitializer) return closure.getAt(0, "this")
            return returnValue.value
        }

        if (isInitializer) return closure.getAt(0, "this")
        return null
    }

    override fun arity(): Int {
        return declaration.params.size
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}