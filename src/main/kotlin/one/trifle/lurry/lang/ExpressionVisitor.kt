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

import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

sealed class ExpressionVisitor<T> {
    abstract fun visitBinaryExpression(expr: BinaryExpression): T
    abstract fun visitUnaryExpression(expr: UnaryExpression): T
    abstract fun visitLiteralExpression(expr: LiteralExpression): T
    abstract fun visitGroupingExpression(expr: GroupingExpression): T
    abstract fun visitVarExpression(expr: VariableExpression): T
    abstract fun define(name: String, value: T?)
}

class ExpressionInterpreter : ExpressionVisitor<Any?>() {
    private val globals: MutableMap<String, Any?> = HashMap()
    private val environment: Environment? = null
    private val locals: MutableMap<Expression, Int> = HashMap<Expression, Int>()
    private val slots: MutableMap<Expression, Int> = HashMap<Expression, Int>()

    override fun visitBinaryExpression(expr: BinaryExpression): Any? {
        val left: Any? = evaluate(expr.left)
        val right: Any? = evaluate(expr.right)
        return when (expr.operation.type) {
            TokenType.STAR -> when {
                left is String && right is Int -> left.repeat(right)
                left is Int && right is Int -> left * right
                left is Long && right is Long -> left * right
                left is Double && right is Double -> left * right
                // TODO Float/Double/BigInteger/BigDecimal/Char/Short/Byte
                else -> throw LurryInterpretationException("Unsupported operation STAR between ${left?.javaClass?.name} and ${right?.javaClass?.name}", expr.operation.line, expr.operation.position)
            }
            TokenType.SLASH -> when {
                left is Int && right is Int -> left / right
                left is Long && right is Long -> left / right
                left is Double && right is Double -> left / right
                // TODO Float/Double/BigInteger/BigDecimal/Char/Short/Byte
                else -> throw LurryInterpretationException("Unsupported operation SLASH between ${left?.javaClass?.name} and ${right?.javaClass?.name}", expr.operation.line, expr.operation.position)
            }
            TokenType.PLUS -> when {
                left is String && right is String -> left + right
                left is String -> left + right.toString()
                right is String -> left.toString() + right

                left is Long && right is Long -> left + right
                left is Long && right is Int -> left + right
                left is Long && right is Double -> left + right
                left is Long && right is Float -> left + right
                left is Long && right is Char -> left + right.toLong()
                left is Long && right is Short -> left + right
                left is Long && right is Byte -> left + right
                left is Long && right is BigInteger -> BigInteger.valueOf(left) + right
                left is Long && right is BigDecimal -> BigDecimal.valueOf(left) + right

                left is Int && right is Int -> left + right

                left is Double && right is Double -> left + right
                // TODO Float/Double/BigInteger/BigDecimal/Char/Short/Byte
                else -> throw LurryInterpretationException("Unsupported operation PLUS between ${left?.javaClass?.name} and ${right?.javaClass?.name}", expr.operation.line, expr.operation.position)
            }
            TokenType.MINUS -> when {
                // todo String
                left is Long && right is Long -> left - right
                left is Int && right is Int -> left - right
                left is Double && right is Double -> left - right
                // TODO Float/Double/BigInteger/BigDecimal/Char/Short/Byte
                else -> throw LurryInterpretationException("Unsupported operation MINUS between ${left?.javaClass?.name} and ${right?.javaClass?.name}", expr.operation.line, expr.operation.position)
            }
            else -> throw LurryInterpretationException("Unsupported operation ${expr.operation.type}", expr.operation.line, expr.operation.position)
        }
    }

    override fun visitUnaryExpression(expr: UnaryExpression): Any? {
        val value = evaluate(expr.expr)
        return when (expr.operation.type) {
            TokenType.MINUS -> when (value) {
                is Long -> -value
                is Int -> -value
                is Float -> -value
                is Double -> -value
                is Short -> -value
                is Byte -> -value
                is BigInteger -> -value
                is BigDecimal -> -value
                else -> throw LurryInterpretationException("Unsupported operation MINUS ${value?.javaClass?.name}", expr.operation.line, expr.operation.position)
            }
            else -> throw LurryInterpretationException("Unsupported operation ${expr.operation.type}", expr.operation.line, expr.operation.position)
        }
    }

    override fun visitLiteralExpression(expr: LiteralExpression): Any? = expr.token.value

    override fun visitGroupingExpression(expr: GroupingExpression): Any? = evaluate(expr.expr)

    override fun visitVarExpression(expr: VariableExpression): Any? {
        val distance = locals[expr]
        if (distance != null) {
            return environment?.getAt(distance, slots[expr]!!)
        }
        val name = expr.token.value.toString()
        if (globals.containsKey(name)) {
            return globals[name]
        } else {
            throw LurryInterpretationException("Undefined variable '$name'", expr.token.line, expr.token.position)
        }
    }

    private fun evaluate(expr: Expression): Any? = expr.accept(this)

    override fun define(name: String, value: Any?) {
        if (environment != null) {
            environment.define(value)
        } else {
            globals[name] = value
        }
    }

    companion object {
        private class Environment(private val enclosing: Environment? = null) {
            private val values: MutableList<Any?> = ArrayList()
            fun define(value: Any?) {
                this.values += value
            }

            fun getAt(distance: Int, slot: Int): Any? {
                var environment: Environment? = this
                for (i in 0 until distance) {
                    environment = environment?.enclosing
                }
                return environment?.values?.get(slot)
            }

            fun assignAt(distance: Int, slot: Int, value: Any?) {
                var environment: Environment? = this
                for (i in 0 until distance) {
                    environment = environment?.enclosing
                }
                environment?.values?.set(slot, value)
            }

            override fun toString(): String = values.toString() + (enclosing?.run { " -> $this" } ?: "")
        }
    }
}

class ExpressionPrinter : ExpressionVisitor<String>() {
    override fun visitBinaryExpression(expr: BinaryExpression): String = parenthesize(expr.operation.type.name, expr.left, expr.right)

    override fun visitUnaryExpression(expr: UnaryExpression): String = parenthesize(expr.operation.type.name, expr.expr)

    override fun visitGroupingExpression(expr: GroupingExpression): String = parenthesize("group", expr.expr)

    override fun visitLiteralExpression(expr: LiteralExpression): String = expr.token.value.toString()

    override fun visitVarExpression(expr: VariableExpression): String = expr.token.value.toString()

    private fun print(expr: Expression): String = expr.accept(this)

    private fun parenthesize(name: String, vararg exprs: Expression): String = StringBuilder().run {
        this.append("(")
                .append(name)
                .append(" ")
                .append(exprs.asSequence().map { expr -> print(expr) }.joinToString(" "))
                .append(")")
                .toString()
    }

    override fun define(name: String, value: String?) {}
}