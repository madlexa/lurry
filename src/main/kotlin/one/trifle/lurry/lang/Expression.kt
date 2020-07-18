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

sealed class Expression(val line: Int, val position: Int) {
    abstract fun <T> accept(visitor: ExpressionVisitor<T>): T
}

data class LiteralExpression(val token: Token) : Expression(token.line, token.position) {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitLiteralExpression(this)
}

data class BinaryExpression(val operation: Token, val left: Expression, val right: Expression) : Expression(operation.line, operation.position) {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitBinaryExpression(this)
}

data class UnaryExpression(val operation: Token, val expr: Expression) : Expression(operation.line, operation.position) {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitUnaryExpression(this)
}

data class GroupingExpression(val expr: Expression) : Expression(expr.line, expr.position) {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitGroupingExpression(this)
}

interface VariableToken {
    val name: Token
}

data class VariableExpression(override val name: Token) : Expression(name.line, name.position), VariableToken {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitVarExpression(this)
}

data class AssignExpression(override val name: Token, val value: Expression) : Expression(name.line, name.position), VariableToken {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitAssignExpression(this)
}

data class LogicalExpression(val operation: Token, val left: Expression, val right: Expression) : Expression(operation.line, operation.position) {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitLogicalExpression(this)
}
