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

    override fun visitVarStatement(stmt: VarStatement): Unit = visitor.define(stmt.name.value.value.toString(), evaluate(stmt.initializer))

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
        is Token -> part.value.value.toString()
        else -> part.toString()
    }

    override fun visitPrintStatement(stmt: PrintStatement): String = parenthesize("println", stmt.expression)
}