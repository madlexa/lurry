package one.trifle.lurry.lang

sealed class Statement {
    abstract fun <T> accept(visitor: StatementVisitor<T>): T
}

class ExpressionStatement(val expression: Expression): Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitExpressionStatement(this)
}