package lox

class LoxClass(val name: String,
               val methods: MutableMap<String, LoxFunction>): LoxCallable {

    fun findMethod(instance: LoxInstance, name: String) : LoxFunction? {
        if (methods.containsKey(name)) {
            return methods[name]?.bind(instance)
        }

        return null
    }

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any? {
        val instance = LoxInstance(this)
        val initializer = methods.get("init")

        initializer?.bind(instance)?.call(interpreter, arguments)

        return instance
    }

    override fun arity(): Int {
        val initializer = methods.get("init")
        return initializer?.arity() ?: 0
    }

    override fun toString(): String {
        return name
    }
}