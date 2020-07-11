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
import kotlin.collections.ArrayList
import kotlin.reflect.jvm.jvmName

sealed class ExpressionVisitor<T> {
    abstract fun visitBinaryExpression(expr: BinaryExpression): T
    abstract fun visitUnaryExpression(expr: UnaryExpression): T
    abstract fun visitLiteralExpression(expr: LiteralExpression): T
    abstract fun visitGroupingExpression(expr: GroupingExpression): T
    abstract fun visitVarExpression(expr: VariableExpression): T
    abstract fun define(name: String, value: T?)
    abstract fun visitAssignExpression(expr: AssignExpression): T
    abstract fun visitLogicalExpression(expr: LogicalExpression): T
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
                left is Number && right is Number -> getNumberInstance(left, right).multiply(left, right)
                else -> throw LurryInterpretationException("Unsupported operation STAR between ${left?.run { this::class.jvmName }} and ${right?.run { this::class.jvmName }}", expr.operation.line, expr.operation.position)
            }
            TokenType.SLASH -> when {
                left is Number && right is Number -> getNumberInstance(left, right).divide(left, right)
                else -> throw LurryInterpretationException("Unsupported operation SLASH between ${left?.run { this::class.jvmName }} and ${right?.run { this::class.jvmName }}", expr.operation.line, expr.operation.position)
            }
            TokenType.PLUS -> when {
                left is String && right is String -> left + right
                left is String -> left + right.toString()
                right is String -> left.toString() + right
                left is Number && right is Number -> getNumberInstance(left, right).plus(left, right)
                else -> throw LurryInterpretationException("Unsupported operation PLUS between ${left?.run { this::class.jvmName }} and ${right?.run { this::class.jvmName }}", expr.operation.line, expr.operation.position)
            }
            TokenType.MINUS -> when {
                left is Number && right is Number -> getNumberInstance(left, right).minus(left, right)
                else -> throw LurryInterpretationException("Unsupported operation MINUS between ${left?.run { this::class.jvmName }} and ${right?.run { this::class.jvmName }}", expr.operation.line, expr.operation.position)
            }
            TokenType.EQUAL_EQUAL -> when {
                left is Number && right is Number -> getNumberInstance(left, right).compareTo(left, right) == 0
                else -> left == right
            }
            TokenType.BANG_EQUAL -> when {
                left is Number && right is Number -> getNumberInstance(left, right).compareTo(left, right) != 0
                else -> left != right
            }
            TokenType.GREATER -> compareObjects(expr, left, right) > 0
            TokenType.GREATER_EQUAL -> compareObjects(expr, left, right) >= 0
            TokenType.LESS -> compareObjects(expr, left, right) < 0
            TokenType.LESS_EQUAL -> compareObjects(expr, left, right) <= 0
            else -> throw LurryInterpretationException("Unsupported operation ${expr.operation.type}", expr.operation.line, expr.operation.position)
        }
    }

    override fun visitUnaryExpression(expr: UnaryExpression): Any? {
        val value = evaluate(expr.expr)
        return when (expr.operation.type) {
            TokenType.MINUS -> when (value) {
                is Number -> getNumberInstance(value, value).multiply(value, -1)
                else -> throw LurryInterpretationException("Unsupported operation MINUS ${value?.javaClass?.name}", expr.operation.line, expr.operation.position)
            }
            TokenType.BANG -> if (value is Boolean) return !value else throw LurryInterpretationException("Unsupported operation BANG on ${value?.run { this::class.java.name }}", expr.operation.line, expr.operation.position)
            else -> throw LurryInterpretationException("Unsupported operation ${expr.operation.type}", expr.operation.line, expr.operation.position)
        }
    }

    override fun visitLiteralExpression(expr: LiteralExpression): Any? = when (expr.token.type) {
        TokenType.TRUE -> true
        TokenType.FALSE -> false
        TokenType.NULL -> null
        TokenType.NUMBER -> expr.token.value
        TokenType.STRING -> expr.token.value
        else -> expr.token.value
    }

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

    override fun visitLogicalExpression(expr: LogicalExpression): Any? = when (expr.operation.type) {
        TokenType.AND -> evaluate(expr.left).let { value ->
            if (value !is Boolean) throw LurryInterpretationException("Unsupported operation AND on ${value?.run { this::class.java.name }}", expr.operation.line, expr.operation.position)
            if (value == false) return@let false
            val right = evaluate(expr.right)
            if (right !is Boolean) throw LurryInterpretationException("Unsupported operation AND on ${right?.run { this::class.java.name }}", expr.operation.line, expr.operation.position)
            return@let right
        }
        TokenType.OR -> evaluate(expr.left).let { value ->
            if (value !is Boolean) throw LurryInterpretationException("Unsupported operation OR on ${value?.run { this::class.java.name }}", expr.operation.line, expr.operation.position)
            if (value == true) return@let true
            val right = evaluate(expr.right)
            if (right !is Boolean) throw LurryInterpretationException("Unsupported operation OR on ${right?.run { this::class.java.name }}", expr.operation.line, expr.operation.position)
            return@let right
        }
        else -> throw LurryInterpretationException("Unsupported operation '${expr.operation.type}'", expr.operation.line, expr.operation.position)
    }

    override fun visitAssignExpression(expr: AssignExpression): Any? {
        val value = evaluate(expr.value)
        val distance = locals[expr]
        if (distance != null) {
            environment?.assignAt(distance, slots[expr]!!, value)
        } else {
            globals[expr.name.value.toString()] = value
        }
        return value
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

    private fun compareObjects(expr: BinaryExpression, left: Any?, right: Any?) = when {
        left is Number && right is Number -> getNumberInstance(left, right).compareTo(left, right)
        left is Comparable<*> && right is Comparable<*> -> compareValues(left, right)
        else -> throw LurryInterpretationException("Unsupported operation 'compareTo' between ${left?.run { this::class.jvmName }} and ${right?.run { this::class.jvmName }}", expr.operation.line, expr.operation.position)
    }

    private fun getNumberInstance(left: Number, right: Number): NumberInstance = when {
        left is Double || right is Double -> DoubleInstance
        left is Float || right is Float -> FloatInstance
        left is BigDecimal || right is BigDecimal -> BigDecimalInstance
        left is BigInteger || right is BigInteger -> BigIntegerInstance
        left is Long || right is Long -> LongInstance
        else -> IntInstance
    }

    private interface NumberInstance {
        fun plus(left: Number, right: Number): Number
        fun minus(left: Number, right: Number): Number
        fun multiply(left: Number, right: Number): Number
        fun divide(left: Number, right: Number): Number
        fun compareTo(left: Number, right: Number): Int
    }

    private object IntInstance : NumberInstance {
        override fun plus(left: Number, right: Number): Int = left.toInt() + right.toInt()
        override fun minus(left: Number, right: Number): Int = left.toInt() - right.toInt()
        override fun multiply(left: Number, right: Number): Int = left.toInt() * right.toInt()
        override fun divide(left: Number, right: Number): Int = left.toInt() / right.toInt()
        override fun compareTo(left: Number, right: Number): Int = left.toInt().compareTo(right.toInt())
    }

    private object LongInstance : NumberInstance {
        override fun plus(left: Number, right: Number): Long = left.toLong() + right.toLong()
        override fun minus(left: Number, right: Number): Long = left.toLong() - right.toLong()
        override fun multiply(left: Number, right: Number): Long = left.toLong() * right.toLong()
        override fun divide(left: Number, right: Number): Long = left.toLong() / right.toLong()
        override fun compareTo(left: Number, right: Number): Int = left.toLong().compareTo(right.toLong())
    }

    private object FloatInstance : NumberInstance {
        override fun plus(left: Number, right: Number): Float = left.toFloat() + right.toFloat()
        override fun minus(left: Number, right: Number): Float = left.toFloat() - right.toFloat()
        override fun multiply(left: Number, right: Number): Float = left.toFloat() * right.toFloat()
        override fun divide(left: Number, right: Number): Float = left.toFloat() / right.toFloat()
        override fun compareTo(left: Number, right: Number): Int = left.toFloat().compareTo(right.toFloat())
    }

    private object DoubleInstance : NumberInstance {
        override fun plus(left: Number, right: Number): Double = left.toDouble() + right.toDouble()
        override fun minus(left: Number, right: Number): Double = left.toDouble() - right.toDouble()
        override fun multiply(left: Number, right: Number): Double = left.toDouble() * right.toDouble()
        override fun divide(left: Number, right: Number): Double = left.toDouble() / right.toDouble()
        override fun compareTo(left: Number, right: Number): Int = left.toDouble().compareTo(right.toDouble())
    }

    private object BigIntegerInstance : NumberInstance {
        private fun <R> eval(left: Number, right: Number, block: (l: BigInteger, r: BigInteger) -> R): R =
                if (left is BigInteger && right is BigInteger) block(left, right)
                else if (left is BigInteger) block(left, BigInteger(right.toString()))
                else if (right is BigInteger) block(BigInteger(left.toString()), right)
                else block(BigInteger(left.toString()), BigInteger(right.toString()))

        override fun plus(left: Number, right: Number): BigInteger = eval(left, right) { l, r -> l + r }
        override fun minus(left: Number, right: Number): BigInteger = eval(left, right) { l, r -> l - r }
        override fun multiply(left: Number, right: Number): BigInteger = eval(left, right) { l, r -> l * r }
        override fun divide(left: Number, right: Number): BigInteger = eval(left, right) { l, r -> l / r }
        override fun compareTo(left: Number, right: Number): Int = eval(left, right) { l, r -> l.compareTo(r) }
    }

    private object BigDecimalInstance : NumberInstance {
        private fun <R> eval(left: Number, right: Number, block: (l: BigDecimal, r: BigDecimal) -> R): R =
                if (left is BigDecimal && right is BigDecimal) block(left, right)
                else if (left is BigDecimal) block(left, BigDecimal(right.toString()))
                else if (right is BigDecimal) block(BigDecimal(left.toString()), right)
                else block(BigDecimal(left.toString()), BigDecimal(right.toString()))

        override fun plus(left: Number, right: Number): BigDecimal = eval(left, right) { l, r -> l + r }
        override fun minus(left: Number, right: Number): BigDecimal = eval(left, right) { l, r -> l - r }
        override fun multiply(left: Number, right: Number): BigDecimal = eval(left, right) { l, r -> l * r }
        override fun divide(left: Number, right: Number): BigDecimal = eval(left, right) { l, r -> l / r }
        override fun compareTo(left: Number, right: Number): Int = eval(left, right) { l, r -> l.compareTo(r) }
    }
}

class ExpressionPrinter : ExpressionVisitor<String>() {
    override fun visitBinaryExpression(expr: BinaryExpression): String = parenthesize(expr.operation.type.name, expr.left, expr.right)
    override fun visitLogicalExpression(expr: LogicalExpression): String = parenthesize(expr.operation.type.name, expr.left, expr.right)
    override fun visitUnaryExpression(expr: UnaryExpression): String = parenthesize(expr.operation.type.name, expr.expr)
    override fun visitGroupingExpression(expr: GroupingExpression): String = parenthesize("group", expr.expr)
    override fun visitLiteralExpression(expr: LiteralExpression): String = expr.token.value.toString()
    override fun visitVarExpression(expr: VariableExpression): String = expr.token.value.toString()
    override fun define(name: String, value: String?) {}
    override fun visitAssignExpression(expr: AssignExpression): String = parenthesize("=", expr.name, expr.value)
    private fun print(expr: Expression): String = expr.accept(this)
    private fun parenthesize(name: String, vararg parts: Any): String = StringBuilder().run {
        this.append("(")
                .append(name)
                .append(" ")
                .append(parts.asSequence().map { part ->
                    when (part) {
                        is Expression -> print(part)
                        is Token -> part.value
                        else -> part.toString()
                    }
                }.joinToString(" "))
                .append(")")
                .toString()
    }
}