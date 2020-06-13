package one.trifle.lurry.lang

import java.math.BigDecimal
import java.math.BigInteger

sealed class ExpressionVisitor<T> {
    abstract fun visitBinaryExpression(expr: BinaryExpression): T
    abstract fun visitUnaryExpression(expr: UnaryExpression): T
    abstract fun visitLiteralExpression(expr: LiteralExpression): T
    abstract fun visitGroupingExpression(expr: GroupingExpression): T
}

class ExpressionInterpreter : ExpressionVisitor<Any?>() {
    override fun visitBinaryExpression(expr: BinaryExpression): Any? {
        val left: Any? = evaluate(expr.left)
        val right: Any? = evaluate(expr.right)
        return when (expr.operation.type) {
            TokenType.STAR -> when {
                left is String && right is Int -> left.repeat(right)
                left is Int && right is Int -> left * right
                left is Long && right is Long -> left * right
                // TODO Float/Double/BigInteger/BigDecimal/Char/Shot/Byte
                else -> throw RuntimeException("Unsupported operation MINUS between ${left?.javaClass?.name} and ${right?.javaClass?.name}")
            }
            TokenType.SLASH -> when {
                left is Int && right is Int -> left / right
                left is Long && right is Long -> left / right
                // TODO Float/Double/BigInteger/BigDecimal/Char/Shot/Byte
                else -> throw RuntimeException("Unsupported operation MINUS between ${left?.javaClass?.name} and ${right?.javaClass?.name}")
            }
            TokenType.PLUS -> when {
                left is String && right is String -> left + right
                left is String -> left + right.toString()
                right is String -> left.toString() + right
                left is Int && right is Int -> left + right
                left is Long && right is Long -> left + right
                // TODO Float/Double/BigInteger/BigDecimal/Char/Shot/Byte
                else -> throw RuntimeException("Unsupported operation PLUS between ${left?.javaClass?.name} and ${right?.javaClass?.name}")
            }
            TokenType.MINUS -> when {
                // todo String
                left is Int && right is Int -> left - right
                left is Long && right is Long -> left - right
                // TODO Float/Double/BigInteger/BigDecimal/Char/Shot/Byte
                else -> throw RuntimeException("Unsupported operation MINUS between ${left?.javaClass?.name} and ${right?.javaClass?.name}")
            }
            else -> throw RuntimeException("Unsupported operation ${expr.operation.type}")
        }
    }

    override fun visitUnaryExpression(expr: UnaryExpression): Any? {
        val value = evaluate(expr.expr)
        return when (expr.operation.type) {
            TokenType.MINUS -> when (value) {
                is Int -> -value
                is Long -> -value
                is Float -> -value
                is Double -> -value
                is Short -> -value
                is Byte -> -value
                is BigInteger -> -value
                is BigDecimal -> -value
                else -> throw RuntimeException("Unsupported operation MINUS ${value?.javaClass?.name}")
            }
            else -> throw RuntimeException("Unsupported operation ${expr.operation.type}")
        }
    }

    override fun visitLiteralExpression(expr: LiteralExpression): Any? = expr.token.value.value

    override fun visitGroupingExpression(expr: GroupingExpression): Any? = evaluate(expr.expr)

    private fun evaluate(expr: Expression): Any? = expr.accept(this)
}

class ExpressionPrinter : ExpressionVisitor<String>() {
    override fun visitBinaryExpression(expr: BinaryExpression): String = parenthesize(expr.operation.type.name, expr.left, expr.right)

    override fun visitUnaryExpression(expr: UnaryExpression): String = parenthesize(expr.operation.type.name, expr.expr)

    override fun visitGroupingExpression(expr: GroupingExpression): String = parenthesize("group", expr.expr)

    override fun visitLiteralExpression(expr: LiteralExpression): String = if (expr.token.value.value == null) {
        "null"
    } else {
        expr.token.value.value.toString()
    }

    private fun print(expr: Expression): String = expr.accept(this)

    private fun parenthesize(name: String, vararg exprs: Expression): String = StringBuilder().run {
        this.append("(")
                .append(name)
                .append(" ")
                .append(exprs.asSequence().map { expr -> print(expr) }.joinToString(" "))
                .append(")")
                .toString()
    }
}