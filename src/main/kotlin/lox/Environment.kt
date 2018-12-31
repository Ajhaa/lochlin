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

    fun define(name : String, value : Any?) {
        values.put(name, value)
    }
}