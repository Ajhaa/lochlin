package lox

var globals = Environment()

fun globals() : Environment {
    clock()
    return globals
}

private fun clock() {
    globals.define("clock", object : LoxCallable {
        override fun arity(): Int = 0

        override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
            return System.currentTimeMillis() / 1000.0
        }

        override fun toString(): String {
            return "<native fn>"
        }
    })
}