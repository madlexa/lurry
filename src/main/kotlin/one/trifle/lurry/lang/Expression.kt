package one.trifle.lurry.lang

sealed class Expression {
    abstract fun <T> accept(visitor: ExpressionVisitor<T>): T
}

data class LiteralExpression(val token: Token) : Expression() {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitLiteralExpression(this)
}

data class BinaryExpression(val operation: Token, val left: Expression, val right: Expression) : Expression() {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitBinaryExpression(this)
}

data class UnaryExpression(val operation: Token, val expr: Expression) : Expression() {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitUnaryExpression(this)
}

data class GroupingExpression(val expr: Expression) : Expression() {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitGroupingExpression(this)
}

data class VariableExpression(val token: Token) : Expression() {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitVarExpression(this)
}



