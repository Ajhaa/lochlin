package lox

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

class Lox {
    companion object {
        private val interpreter : Interpreter = Interpreter()

        var hadError = false
        var hadRuntimeError = false

        @JvmStatic
        fun main(args : Array<String>) {
            if (args.size > 1) {
                println("Usage: jlox [script]")
                System.exit(64)
            } else if (args.size == 1) {
                runFile(args[0])
            } else {
                runPrompt()
            }
        }

        fun runFile(path : String) {
            var bytes = Files.readAllBytes(Paths.get(path))
            run(String(bytes, Charset.defaultCharset()))
            if (hadError) {
                System.exit(65)
            }
            if (hadRuntimeError) {
                System.exit(70)
            }
        }

        fun runPrompt() {
            while (true) {
                print("> ")
                run(readLine()!!)
                hadError = false;
            }
        }

        fun run(source : String) {
            var scanner = Scanner(source)
            var tokens = scanner.scanTokens()
            val parser = Parser(tokens)
            val expression = parser.parse()

            if (hadError) return

            interpreter.interpret(expression)

        }

        fun error(line : Int, message : String) {
            report(line, "", message)
        }

        fun error(token: Token, message: String) {
            if (token.type === TokenType.EOF) {
                report(token.line, " at end", message)
            } else {
                report(token.line, " at '" + token.lexeme + "'", message)
            }
        }

        fun runtimeError(error : RuntimeError) {
            System.err.println("${error.message}\n[line ${error.token.line}]")
            hadRuntimeError = true
        }

        private fun report(line: Int, where : String, message : String) {
            System.err.println("Line [$line] Error$where: $message")
            hadError = true
        }
    }
}