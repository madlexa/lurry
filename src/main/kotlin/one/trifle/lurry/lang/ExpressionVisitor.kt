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

import java.lang.reflect.Executable
import java.lang.reflect.Modifier
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Types
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
    abstract fun visitCallExpression(expr: CallExpression): T
    abstract fun visitMethodCallExpression(expr: MethodCallExpression): T
    abstract fun visitFieldCallExpression(expr: FieldCallExpression): T
    abstract fun <R> visitBlock(env: Map<String, Any?>, block: () -> R): R
    abstract fun <R> createMapper(call: (Map<String, Any?>) -> R): LurryMapper<R>
    abstract fun <R> createFunction(params: List<Token>, call: (Map<String, Any?>) -> R): LurryFunction<R>

    interface LurryMapper<R> {
        fun call(arg: ResultSet): R
    }

    interface LurryFunction<R> {
        fun call(vararg args: Any?): R
    }
}

class ExpressionInterpreter : ExpressionVisitor<Any?>() {
    private var environment: Environment? = null

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

    override fun visitVarExpression(expr: VariableExpression): Any? = environment?.getAt(expr)

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
        environment?.assignAt(expr, value)
        return value
    }

    private fun evaluate(expr: Expression): Any? = expr.accept(this)

    override fun define(name: String, value: Any?) {
        if (environment == null) {
            environment = Environment(null)
        }
        environment!!.define(name, value)
    }

    override fun <R> visitBlock(env: Map<String, Any?>, block: () -> R): R {
        val last = environment
        try {
            environment = Environment(last).apply {
                env.forEach { (name, value) -> define(name, value) }
            }
            return block()
        } finally {
            environment = last
        }
    }

    override fun visitCallExpression(expr: CallExpression): Any? {
        val function = evaluate(expr.call)
        val arguments = expr.arguments.map { arg -> evaluate(arg) }
        return when (function) {
            is LurryFunction<*> -> function.call(*arguments.toTypedArray())
            is Class<*> -> findExecutable(function.constructors, arguments)?.newInstance(*arguments.toTypedArray())
                    ?: throw LurryInterpretationException("Constructor not found exception", expr.line, expr.position)
            else -> throw LurryInterpretationException("Can only call functions or constructors", expr.line, expr.position)
        }
    }

    override fun visitMethodCallExpression(expr: MethodCallExpression): Any? {
        // todo cache
        val args = expr.arguments.map { arg -> evaluate(arg) }
        var obj = evaluate(expr.obj)
                ?: throw LurryInterpretationException("Null pointer exception", expr.line, expr.position)
        val clazz = if (obj is Class<*>) obj else obj.javaClass
        val method = findExecutable(clazz.declaredMethods, args) { method -> method.name == expr.method.value }
                ?: throw LurryInterpretationException("Method ${expr.method.value} not found exception", expr.line, expr.position)
        if (!method.isAccessible && Modifier.isPublic(method.modifiers)) method.isAccessible = true
        return method.invoke(if (obj is Class<*>) null else obj, *args.toTypedArray())
    }

    override fun visitFieldCallExpression(expr: FieldCallExpression): Any? {
        val obj = evaluate(expr.obj)
                ?: throw LurryInterpretationException("Null pointer exception", expr.line, expr.position)
        val field = obj.javaClass.fields.find { f -> f.name == expr.field.value }
        return if (field != null) {
            if (expr.value != null) {
                evaluate(expr.value).run {
                    field.set(obj, this)
                }
            } else {
                field.get(obj)
            }
        } else {
            if (expr.value != null) {
                // setter
                evaluate(expr.value).run {
                    val name = "set" + expr.field.value.toString().capitalize()
                    val setter = findExecutable(obj.javaClass.declaredMethods, listOf(this)) { method -> method.name == name }
                            ?: throw LurryInterpretationException("Method '${name}' not found exception", expr.line, expr.position)
                    setter.invoke(obj, this)
                }
            } else {
                // getter
                val name = "get" + expr.field.value.toString().capitalize()
                val getter = obj.javaClass.declaredMethods.find { method -> method.name == name }
                        ?: throw LurryInterpretationException("Method '${name}' not found exception", expr.line, expr.position)
                getter.invoke(obj)
            }
        }
    }

    override fun <R> createMapper(call: (Map<String, Any?>) -> R) = InterpretationMapper(call)
    override fun <R> createFunction(params: List<Token>, call: (Map<String, Any?>) -> R) = InterpretationFunction(params, call)

    class Environment(private val enclosing: Environment? = null) {
        private val variables: MutableMap<String, Any?> = HashMap()
        fun define(name: String, value: Any?) {
            variables[name] = value
        }

        fun getAt(expr: VariableToken): Any? {
            val name = expr.name.value as String
            var environment: Environment? = this
            while (environment != null) {
                if (environment.variables.containsKey(name)) {
                    return environment.variables[name]
                }
                environment = environment.enclosing
            }
            throw LurryInterpretationException("Undefined variable '${name}'", expr.name.line, expr.name.position)
        }

        fun assignAt(expr: VariableToken, value: Any?) {
            val name = expr.name.value as String
            var environment: Environment? = this
            while (environment != null) {
                if (environment.variables.containsKey(name)) {
                    environment.variables[name] = value
                    return
                }
                environment = environment.enclosing
            }
            throw LurryInterpretationException("Undefined variable '${name}'", expr.name.line, expr.name.position)
        }
    }

    class InterpretationFunction<R>(private val params: List<Token>, private val body: (Map<String, Any?>) -> R) : LurryFunction<R> {
        override fun call(vararg args: Any?): R {
            val vars = if (params.isEmpty()) {
                mapOf()
            } else {
                (params.indices).map { i -> params[i].value.toString() to args[i] }.toMap()
            }
            return body(vars)
        }
    }

    class InterpretationMapper<R>(private val body: (Map<String, Any?>) -> R) : LurryMapper<R> {
        override fun call(arg: ResultSet): R {
            val row: MutableMap<String, Any?> = HashMap()
            val metadata: ResultSetMetaData = arg.metaData
            (1..metadata.columnCount).forEach { index ->
                val name: String = metadata.getColumnName(index)
                val value: Any? = when (metadata.getColumnType(index)) {
                    Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR -> arg.getString(index)
                    Types.NUMERIC, Types.DECIMAL -> arg.getBigDecimal(index)
                    Types.BIT -> arg.getBoolean(index)
                    Types.TINYINT -> arg.getByte(index)
                    Types.SMALLINT -> arg.getShort(index)
                    Types.BIGINT -> arg.getLong(index)
                    Types.REAL, Types.FLOAT -> arg.getFloat(index)
                    Types.DOUBLE -> arg.getDouble(index)
                    Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> arg.getBytes(index)
                    Types.DATE -> arg.getDate(index)
                    Types.TIME -> arg.getTime(index)
                    Types.TIMESTAMP -> arg.getTimestamp(index)
                    Types.ARRAY -> arg.getArray(index)
                    Types.BLOB -> arg.getBlob(index)
                    Types.NULL -> null
                    else -> arg.getString(index)
                }
                row["#${name}"] = value
            }
            return body(row)
        }
    }

    private fun <T : Executable> findExecutable(executables: Array<T>, arguments: List<Any?>, condition: (T) -> Boolean = { true }): T? {
        val getPrimitive: (Class<*>) -> Class<*> = { clazz: Class<*> ->
            if (clazz.isPrimitive) {
                clazz
            } else {
                val field = clazz.declaredFields.find { field -> field.name == "TYPE" }
                if (field == null || !Modifier.isPublic(field.modifiers) || !Modifier.isStatic(field.modifiers)) {
                    clazz
                } else {
                    field.get(null).let { c ->
                        if (c is Class<*>) {
                            c
                        } else {
                            clazz
                        }
                    }
                }
            }
        }
        val isAssignableFrom: (Class<*>, Class<*>) -> Boolean = { c1: Class<*>, c2: Class<*> ->
            var class1 = c1
            var class2 = c2
            if (class1.isPrimitive || class2.isPrimitive) {
                class1 = getPrimitive(c1)
                class2 = getPrimitive(c2)
            }
            class1 == class2 || class1.isAssignableFrom(class2)
        }

        return executables.filter(condition).find { exec ->
            if (exec.parameters.size != arguments.size) return@find false
            arguments.asSequence().map { arg -> arg?.javaClass }
                    .forEachIndexed { index, arg ->
                        if (arg != null && !isAssignableFrom(exec.parameters[index].type, arg)) {
                            return@find false
                        }
                    }
            return@find true
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

object ExpressionPrinter : ExpressionVisitor<String>() {
    override fun visitBinaryExpression(expr: BinaryExpression): String = parenthesize(expr.operation.type.name, expr.left, expr.right)
    override fun visitLogicalExpression(expr: LogicalExpression): String = parenthesize(expr.operation.type.name, expr.left, expr.right)
    override fun visitUnaryExpression(expr: UnaryExpression): String = parenthesize(expr.operation.type.name, expr.expr)
    override fun visitGroupingExpression(expr: GroupingExpression): String = parenthesize("group", expr.expr)
    override fun visitLiteralExpression(expr: LiteralExpression): String = expr.token.stringValue()
    override fun visitVarExpression(expr: VariableExpression): String = expr.name.stringValue()
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
                        is Token -> part.stringValue()
                        else -> part.toString()
                    }
                }.joinToString(" "))
                .append(")")
                .toString()
    }

    private fun Token.stringValue(): String = when (type) {
        TokenType.TRUE -> "TRUE"
        TokenType.FALSE -> "FALSE"
        TokenType.NULL -> "NULL"
        else -> value.toString()
    }

    override fun visitCallExpression(expr: CallExpression): String =
            "call ${print(expr.call)} (${expr.arguments.joinToString(", ") { arg -> print(arg) }})"

    override fun visitMethodCallExpression(expr: MethodCallExpression): String =
            "${print(expr.obj)}.${expr.method.stringValue()}(${expr.arguments.joinToString(", ") { arg -> print(arg) }})"

    override fun visitFieldCallExpression(expr: FieldCallExpression): String = if (expr.value != null) {
        parenthesize("set", expr.obj, expr.field, expr.value)
    } else {
        parenthesize("get", expr.obj, expr.field)
    }

    override fun <R> visitBlock(env: Map<String, Any?>, block: () -> R): R = block()
    override fun <R> createMapper(call: (Map<String, Any?>) -> R) = EmptyMapper(call)
    override fun <R> createFunction(params: List<Token>, call: (Map<String, Any?>) -> R) = EmptyFunction(call)

    class EmptyMapper<R>(private val body: (Map<String, Any?>) -> R) : LurryMapper<R> {
        override fun call(arg: ResultSet): R = body(mapOf())
    }

    class EmptyFunction<R>(private val body: (Map<String, Any?>) -> R) : LurryFunction<R> {
        override fun call(vararg args: Any?): R = body(mapOf())
    }
}