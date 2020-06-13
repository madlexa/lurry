package one.trifle.lurry.lang

sealed class StatementVisitor<T>(val visitor: ExpressionVisitor<T>) {
    abstract fun visitExpressionStatement(stmt: ExpressionStatement): T
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

    private fun evaluate(expression: Expression): Any? = expression.accept(visitor)
}

class StatementPrinter(visitor: ExpressionPrinter) : StatementVisitor<String>(visitor) {
    fun print(stmts: List<Statement>): String = stmts.asSequence()
            .map { stmt -> print(stmt) }.joinToString("\n")

    private fun print(stmt: Statement): String = stmt.accept(this)

    override fun visitExpressionStatement(stmt: ExpressionStatement): String = parenthesize(";", stmt.expression)

    private fun parenthesize(name: String, vararg exprs: Expression): String = StringBuilder().run {
        this.append("(")
                .append(name)
                .append(exprs.asSequence().map { expr -> expr.accept(visitor) }.joinToString(" "))
                .append(")")
                .toString()
    }
}