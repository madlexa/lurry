/*
 * Copyright 2020 Aleksey Dobrynin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package one.trifle.lurry.lang

sealed class StatementVisitor<T>(val visitor: ExpressionVisitor<T>) {
    abstract fun visitExpressionStatement(stmt: ExpressionStatement): T
    abstract fun visitVarStatement(stmt: VarStatement): T
    abstract fun visitPrintStatement(stmt: PrintStatement): T
}

class StatementInterpreter(visitor: ExpressionInterpreter) : StatementVisitor<Any?>(visitor) {
    fun interpret(stmts: List<Statement>): Any? {
        var result: Any? = null
        for (stmt in stmts) {
            result = execute(stmt)
        }
        return result
    }

    private fun execute(stmt: Statement): Any? = stmt.accept(this)

    override fun visitExpressionStatement(stmt: ExpressionStatement): Any? = evaluate(stmt.expression)

    override fun visitVarStatement(stmt: VarStatement): Unit = visitor.define(stmt.name.value.toString(), evaluate(stmt.initializer))

    private fun evaluate(expression: Expression): Any? = expression.accept(visitor)
    override fun visitPrintStatement(stmt: PrintStatement): Any? {
        println(evaluate(stmt.expression))
        return null
    }
}

class StatementPrinter(visitor: ExpressionPrinter) : StatementVisitor<String>(visitor) {
    fun print(stmts: List<Statement>): String = stmts.asSequence()
            .map { stmt -> print(stmt) }.joinToString("\n")

    private fun print(stmt: Statement): String = stmt.accept(this)

    override fun visitExpressionStatement(stmt: ExpressionStatement): String = parenthesize(stmt.expression)

    override fun visitVarStatement(stmt: VarStatement): String = parenthesize("var", stmt.name, "=", stmt.initializer)

    private fun parenthesize(vararg parts: Any): String = parts.asSequence().map { part -> getString(part) }.joinToString(" ")

    private fun getString(part: Any): String = when (part) {
        is String -> part
        is Statement -> part.accept(this)
        is Expression -> part.accept(visitor)
        is Token -> part.value.toString()
        else -> part.toString()
    }

    override fun visitPrintStatement(stmt: PrintStatement): String = parenthesize("println", stmt.expression)
}