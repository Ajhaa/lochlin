package tool

import java.io.PrintWriter

fun main(args : Array<String>) {
    var outputDir = args[0]
    defineAst(outputDir, "Expr",
              "Binary   - left : Expr, operator : Token, right : Expr",
              "Grouping - expression : Expr",
              "Literal  - value : Any?",
              "Unary    - operator : Token, right : Expr")
}

fun defineAst(outputDir : String, baseName : String, vararg types : String) {
    val path = "$outputDir/$baseName.kt"
    val writer = PrintWriter(path, "UTF-8")

    writer.println("package lox;")
    writer.println()
    writer.println("abstract class $baseName {")

    defineVisitor(writer, baseName, types)

    for (type in types) {
        val className = type.split("-")[0].trim()
        val fields = type.split("-")[1].trim()
        defineType(writer, baseName, className, fields)
    }

    writer.println();
    writer.println("    abstract fun<R> accept(visitor : Visitor<R>) : R");
    writer.println("}")
    writer.close()
}

fun defineType(writer : PrintWriter, baseName : String,
               className : String, fields: String) {
    var splitFields = fields.split(",")
    var fieldString = ""

    for (s in splitFields) {
        var new = "val " + s.trim()
        fieldString += "$new, "
    }

    fieldString = fieldString.dropLast(2)

    writer.println("    class $className (")
    writer.println("        $fieldString) : $baseName() {")
    writer.println("        override fun<R> accept(visitor : Visitor<R>) : R {")
    writer.println("            return visitor.visit$className$baseName(this)")
    writer.println("        }")
    writer.println("    }")
    writer.println()
}

fun defineVisitor(writer : PrintWriter, baseName : String, types : Array<out String>) {
    writer.println("    interface Visitor<R> {")
    for (type in types) {
        var typeName = type.split("-")[0].trim()
        writer.println("        fun visit$typeName$baseName(${baseName.toLowerCase()} : $typeName) : R")
    }

    writer.println("    }")
}