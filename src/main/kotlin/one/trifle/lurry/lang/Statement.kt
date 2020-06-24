package one.trifle.lurry.lang

sealed class Statement {
    abstract fun <T> accept(visitor: StatementVisitor<T>): T
}

class ExpressionStatement(val expression: Expression): Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitExpressionStatement(this)
}
class VarStatement(val name: Token, val initializer: Expression): Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitVarStatement(this)
}

class PrintStatement(val expression: Expression): Statement() {
    override fun <T> accept(visitor: StatementVisitor<T>): T = visitor.visitPrintStatement(this)
}