package lox

class Environment(val enclosing : Environment?) {
    private val values = HashMap<String, Any?>()

    constructor() : this(null)

    fun get(name : Token) : Any? {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme)
        }

        if (enclosing != null) {
            return enclosing.get(name)
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun getAt(distance: Int, name: String) : Any? {
        return ancestor(distance)!!.values.get(name)
    }

    fun assign(name : Token, value : Any?) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value)
            return;
        }

        if(enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError(name, "Undefined variable '${name.lexeme}'.")
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance)!!.values.put(name.lexeme, value)
    }

    fun define(name : String, value : Any?) {
        values.put(name, value)
    }

    fun ancestor(distance: Int) : Environment? {
        var environment: Environment?= this

        for (i in 0 until distance) {
            environment = environment!!.enclosing
        }

        return environment
    }
}