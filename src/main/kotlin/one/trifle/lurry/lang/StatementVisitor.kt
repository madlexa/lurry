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
    abstract fun visitBlockStatement(stmt: BlockStatement): T
    abstract fun visitIfStatement(stmt: IfStatement): T
    abstract fun visitMapperStatement(stmt: MapperStatement): T
    abstract fun visitFunctionStatement(stmt: FunctionStatement): T
}

class StatementInterpreter(visitor: ExpressionInterpreter) : StatementVisitor<Any?>(visitor) {
    fun interpret(stmts: List<Statement>): Any? {
        var result: Any? = null
        for (stmt in stmts) {
            result = execute(stmt)
        }
        return result
    }

    override fun visitExpressionStatement(stmt: ExpressionStatement): Any? = evaluate(stmt.expression)

    override fun visitVarStatement(stmt: VarStatement): Unit = visitor.define(stmt.name.value.toString(), evaluate(stmt.initializer))

    override fun visitPrintStatement(stmt: PrintStatement): Any? {
        println(evaluate(stmt.expression))
        return null
    }

    override fun visitBlockStatement(stmt: BlockStatement): Any? {
        visitor.visitBlock(mapOf()) {
            stmt.statements.forEach { statement ->
                execute(statement)
            }
        }
        return null
    }

    override fun visitIfStatement(stmt: IfStatement): Any? {
        val condition = evaluate(stmt.condition)
        if (condition !is Boolean) throw LurryInterpretationException("Expected boolean condition. Actual: [${condition?.let { this::class.simpleName } ?: "NULL"}]", stmt.condition.line, stmt.condition.position)
        return if (condition) {
            execute(stmt.than)
        } else if (stmt.`else` != null) {
            execute(stmt.`else`)
        } else {
            null
        }
    }

    override fun visitMapperStatement(stmt: MapperStatement) {
        visitor.define(stmt.name.value.toString(), visitor.createMapper { args ->
            visitor.visitBlock(args) {
                stmt.body.forEach { statement ->
                    execute(statement)
                }
            }
        })
    }

    override fun visitFunctionStatement(stmt: FunctionStatement) {
        visitor.define(stmt.name.value.toString(), visitor.createFunction(stmt.params) { args ->
            visitor.visitBlock(args) {
                stmt.body.forEach { statement ->
                    execute(statement)
                }
            }
        })
    }

    private fun execute(stmt: Statement): Any? = stmt.accept(this)

    private fun evaluate(expression: Expression): Any? = expression.accept(visitor)
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

    override fun visitBlockStatement(stmt: BlockStatement): String =
            "(block${stmt.statements.joinToString(separator = "") { statement -> "\n" + statement.accept(this) }})"

    override fun visitMapperStatement(stmt: MapperStatement) =
            """mapper ${stmt.name.value} (${stmt.params.map { param -> param.value }.joinToString(", ")}) {
            |${stmt.body.map { statement -> statement.accept(this) }.joinToString("\n")}
            |}""".trimMargin()

    override fun visitFunctionStatement(stmt: FunctionStatement) =
            """fun ${stmt.name.value} (${stmt.params.map { param -> param.value }.joinToString(", ")}) {
            |${stmt.body.map { statement -> statement.accept(this) }.joinToString("\n")}
            |}""".trimMargin()

    override fun visitIfStatement(stmt: IfStatement): String =
            """(if ${stmt.condition.accept(visitor)}
                |than: ${stmt.than.accept(this)}
                |else: ${stmt.`else`?.accept(this)}
                |)""".trimMargin()
}