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

import java.util.*

class Parser(tokens: List<Token>) {
    private val reader = TokenReader(tokens)

    fun parse(): List<Statement> {
        val statements = ArrayList<Statement>()
        reader.next()
        while (reader.peek() != Token.EOF) {
            statements += declaration()
        }
        return statements
    }

    private fun declaration(): Statement = when (reader.peek().type) {
        TokenType.VAR -> varDeclaration()
        TokenType.FUN -> TODO()
        else -> statement()
    }

    private fun varDeclaration(): Statement {
        if (!reader.testNext(TokenType.IDENTIFIER)) throw LurryParserException("Expect variable name '${reader.peekNext().value}'.", reader.peekNext().line, reader.peekNext().position)
        val name = reader.peekAndNext()
        val value: Expression = if (reader.test(TokenType.EQUAL))
            expression()
        else
            LiteralExpression(Token.NULL)
        if (!reader.test(TokenType.SEMICOLON)) throw LurryParserException("Expect ';' after variable declaration", reader.peek().line, reader.peek().position)
        return VarStatement(name, value)
    }

    private fun statement(): Statement = when (reader.peek().type) {
        TokenType.PRINT -> printStatement()
        // todo FOR/IF/RETURN/WHILE/LEFT_BRACE
        else -> expressionStatement()
    }

    private fun printStatement(): Statement {
        reader.next()
        val value: Expression = expression()
        if (!reader.test(TokenType.SEMICOLON)) throw LurryParserException("Expect ';' after value.", reader.peek().line, reader.peek().position)
        return PrintStatement(value)
    }

    private fun expressionStatement(): Statement {
        val expr: Expression = expression()
        // TODO SEMICOLON
        return ExpressionStatement(expr)
    }

    private fun expression(): Expression {
        return assignment()
    }

    private fun assignment(): Expression {
        var expr: Expression = or()
        if (reader.peek().type == TokenType.EQUAL) {
            val equals: Token = reader.peekAndNext()
            val value: Expression = assignment()
            expr = when (expr) {
                is VariableExpression -> AssignExpression(expr.token, value)
                // todo GetExpression
                else -> throw LurryParserException("Invalid assignment target.", equals.line, equals.position)
            }
            reader.next()
        }
        return expr
    }

    private fun or(): Expression {
        val expr: Expression = and()
        // TODO OR
        return expr
    }

    private fun and(): Expression {
        val expr: Expression = equality()
        // TODO AND
        return expr
    }

    private fun equality(): Expression {
        var expr: Expression = comparison()

        while (reader.peek().type.includes(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator: Token = reader.peekAndNext()
            val right: Expression = comparison()
            expr = BinaryExpression(operator, expr, right)
        }
        return expr
    }

    private fun comparison(): Expression {
        var expr: Expression = addition()
        while (reader.peek().type.includes(TokenType.GREATER, TokenType.LESS, TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL)) {
            val operator: Token = reader.peekAndNext()
            val right: Expression = addition()
            expr = BinaryExpression(operator, expr, right)
        }
        return expr
    }

    private fun addition(): Expression {
        var expr: Expression = multiplication()
        while (reader.peek().type.includes(TokenType.MINUS, TokenType.PLUS)) {
            val operator: Token = reader.peekAndNext()
            val right: Expression = multiplication()
            expr = BinaryExpression(operator, expr, right)
        }
        return expr
    }

    private fun multiplication(): Expression {
        var expr: Expression = unary()
        while (reader.peek().type.includes(TokenType.SLASH, TokenType.STAR)) {
            val operator: Token = reader.peekAndNext()
            val right: Expression = unary()
            expr = BinaryExpression(operator, expr, right)
        }
        return expr
    }

    private fun unary(): Expression {
        // todo BANG
        if (reader.peek().type == TokenType.MINUS) {
            val operator: Token = reader.peekAndNext()
            val right: Expression = unary()
            return UnaryExpression(operator, right)
        }
        return call()
    }

    private fun call(): Expression {
        val expr: Expression = primary()
        // TODO CALL
        return expr
    }

    private fun primary(): Expression {
        val expr = when (reader.peek().type) {
            TokenType.FALSE,
            TokenType.TRUE,
            TokenType.NULL,
            TokenType.NUMBER,
            TokenType.STRING -> LiteralExpression(reader.peek())
            TokenType.IDENTIFIER -> VariableExpression(reader.peek())
            TokenType.LEFT_PAREN -> {
                val line = reader.peek().line
                val position = reader.peek().position
                reader.next()
                val expr: Expression = expression()
                if (reader.peek().type != TokenType.RIGHT_PAREN) {
                    throw LurryParserException("Expect ')' after expression", line, position)
                }
                GroupingExpression(expr)
            }
            else -> throw LurryParserException("Expect expression [${reader.peek()}]", reader.peek().line, reader.peek().position)
        }
        reader.next()
        return expr
    }

    private fun TokenType.includes(vararg types: TokenType): Boolean = types.any { type -> this == type }
}

private class TokenReader(private val tokens: List<Token>) {
    private var current: Token = Token.EOF
    private var next: Token = Token.EOF
    private var position: Int = -1

    init {
        next()
    }

    fun peek(): Token = current
    fun peekNext(): Token = next
    fun peekAndNext(): Token = current.apply { next() }

    fun next(): Token {
        current = next
        next = if (++position < tokens.size) {
            tokens[position]
        } else {
            Token.EOF
        }
        return peek()
    }

    fun testNext(type: TokenType): Boolean {
        if (next.type == type) {
            next()
            return true
        }
        return false
    }

    fun test(type: TokenType): Boolean {
        if (current.type == type) {
            next()
            return true
        }
        return false
    }
}
