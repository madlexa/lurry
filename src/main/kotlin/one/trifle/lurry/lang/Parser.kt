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

class Parser(tokens: List<Token>) {
    private val reader = TokenReader(tokens)

    fun parse(): List<Statement> {
        val statements = ArrayList<Statement>()
        reader.next()
        while (reader.peek() != Token.EOF) {
            statements += main()
        }
        return statements
    }

    private fun main(): Statement = when (reader.peek().type) {
        TokenType.IDENTIFIER -> mapperDeclaration()
        TokenType.IMPORT -> importDeclaration()
        else -> declaration()
    }

    private fun declaration(): Statement = when (reader.peek().type) {
        TokenType.VAR -> varDeclaration()
        TokenType.FUN -> functionDeclaration()
        else -> statement()
    }

    private fun varDeclaration(): Statement {
        if (!reader.testNext(TokenType.IDENTIFIER)) throw LurryParserException("Expect variable name '${reader.peekNext().value}'.", reader.peekNext().line, reader.peekNext().position)
        val name = reader.peekAndNext()
        val value: Expression = if (reader.test(TokenType.EQUAL))
            expression()
        else
            LiteralExpression(Token.NULL)
        reader.test(TokenType.SEMICOLON)
        return VarStatement(name, value)
    }

    private fun functionDeclaration(): Statement {
        val name = reader.next()
        reader.next()
        if (!reader.test(TokenType.LEFT_PAREN)) throw LurryParserException("Expect '(' after function name.", reader.peek().line, reader.peek().position)
        val params = getParameters()
        val body: List<Statement> = when (reader.peekAndNext().type) {
            TokenType.EQUAL -> listOf(ReturnStatement(declaration()))
            TokenType.LEFT_BRACE -> block()
            else -> throw LurryParserException("Expect '{' or '=' before function body.", reader.peek().line, reader.peek().position)
        }
        return FunctionStatement(name, params, BlockStatement(body))
    }

    private fun mapperDeclaration(): Statement {
        val name = reader.peekAndNext()
        if (!reader.test(TokenType.LEFT_PAREN)) throw LurryParserException("Expect '(' after mapper name.", reader.peek().line, reader.peek().position)
        val primaryKeys = getParameters()
        val body: List<Statement> = when (reader.peekAndNext().type) {
            TokenType.EQUAL -> listOf(ReturnStatement(declaration()))
            TokenType.LEFT_BRACE -> block()
            else -> throw LurryParserException("Expect '{' or '=' before mapper body.", reader.peek().line, reader.peek().position)
        }
        return MapperStatement(name, primaryKeys, BlockStatement(body))
    }

    private fun importDeclaration(): Statement {
        reader.next()
        val path = StringBuilder()
        do {
            val dir = reader.peek()
            if (dir.type != TokenType.IDENTIFIER) throw LurryParserException("Expect class path after import declaration", dir.line, dir.position)
            path.append(dir.value).append(".")
            reader.next()
        } while (reader.test(TokenType.DOT))
        path.setLength(path.length - 1)
        val alias = reader.peek()
        val name: String = if (alias.type == TokenType.IDENTIFIER && alias.value == "as") {
            if (!reader.testNext(TokenType.IDENTIFIER)) throw LurryParserException("Expect class alias after import declaration", reader.peek().line, reader.peek().position)
            reader.peekAndNext().value.toString()
        } else {
            path.toString().let { str ->
                str.substring(str.lastIndexOf('.') + 1)
            }
        }
        return ImportStatement(name, path.toString())
    }

    private fun statement(): Statement = when (reader.peek().type) {
        TokenType.PRINT -> printStatement()
        TokenType.IF -> ifStatement()
        TokenType.FOR -> forStatement()
        TokenType.RETURN -> returnStatement()
        TokenType.WHILE -> whileStatement()
        TokenType.LEFT_BRACE -> blockStatement()
        else -> expressionStatement()
    }

    private fun block(): List<Statement> {
        val statements: MutableList<Statement> = ArrayList()
        while (!reader.peek().type.includes(TokenType.RIGHT_BRACE, TokenType.EOF)) {
            statements += declaration()
        }
        if (!reader.test(TokenType.RIGHT_BRACE)) {
            throw LurryParserException("Expect '}' after block.", reader.peek().line, reader.peek().position)
        }
        return statements
    }

    private fun blockStatement(): Statement {
        reader.next()
        return BlockStatement(block())
    }

    private fun forStatement(): Statement = TODO()

    private fun returnStatement(): Statement {
        reader.next()
        return if (reader.peek().type == TokenType.RIGHT_BRACE) {
            ReturnStatement(null)
        } else {
            ReturnStatement(ExpressionStatement(expression()))
        }
    }

    private fun whileStatement(): Statement = TODO()

    private fun printStatement(): Statement {
        reader.next()
        val value: Expression = expression()
        reader.test(TokenType.SEMICOLON)
        return PrintStatement(value)
    }

    private fun ifStatement(): Statement {
        reader.next()
        val condition: Expression = expression()
        val then: Statement = statement()
        var `else`: Statement? = null
        if (reader.test(TokenType.ELSE)) {
            `else` = statement()
        }
        return IfStatement(condition, then, `else`)
    }

    private fun expressionStatement(): Statement = ExpressionStatement(expression())

    private fun expression(): Expression {
        var expr: Expression = or()
        if (reader.peek().type == TokenType.EQUAL) {
            val equals: Token = reader.peekAndNext()
            val value: Expression = expression()
            expr = when (expr) {
                is VariableExpression -> AssignExpression(expr.name, value)
                else -> throw LurryParserException("Invalid assignment target.", equals.line, equals.position)
            }
        }
        return expr
    }

    private fun or(): Expression {
        var expr: Expression = and()
        while (reader.peek().type.includes(TokenType.OR)) {
            val operator: Token = reader.peekAndNext()
            val right: Expression = and()
            expr = LogicalExpression(operator, expr, right)
        }
        return expr
    }

    private fun and(): Expression {
        var expr: Expression = equality()
        while (reader.peek().type.includes(TokenType.AND)) {
            val operator: Token = reader.peekAndNext()
            val right: Expression = equality()
            expr = LogicalExpression(operator, expr, right)
        }
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
        if (reader.peek().type.includes(TokenType.MINUS, TokenType.BANG)) {
            val operator: Token = reader.peekAndNext()
            val right: Expression = unary()
            return UnaryExpression(operator, right)
        }
        return call()
    }

    private fun call(): Expression {
        var expr: Expression = primary()
        while (true) {
            expr = when (reader.peek().type) {
                TokenType.LEFT_PAREN -> {
                    CallExpression(expr, getArguments())
                }
                TokenType.DOT -> {
                    val name: Token = reader.next()
                    if (name.type != TokenType.IDENTIFIER) throw LurryParserException("Expect property name after '.'.", name.line, name.position)
                    if (reader.testNext(TokenType.LEFT_PAREN)) {
                        MethodCallExpression(expr, name, getArguments())
                    } else {
                        if (reader.testNext(TokenType.EQUAL)) {
                            reader.next()
                            FieldCallExpression(expr, name, expression())
                        } else {
                            reader.next()
                            FieldCallExpression(expr, name)
                        }
                    }
                }
                else -> return expr
            }
        }
    }

    private fun getArguments(): List<Expression> {
        val args = ArrayList<Expression>()
        reader.next()
        if (!reader.test(TokenType.RIGHT_PAREN)) {
            do {
                args += expression()
            } while (reader.test(TokenType.COMMA))
            if (!reader.test(TokenType.RIGHT_PAREN)) throw LurryParserException("Expect ')' after arguments.", reader.peek().line, reader.peek().position)
        }
        return args
    }

    private fun getParameters(): List<Token> {
        val params = ArrayList<Token>()
        if (!reader.test(TokenType.RIGHT_PAREN)) {
            do {
                val identifier = reader.peekAndNext()
                if (identifier.type != TokenType.IDENTIFIER) throw LurryParserException("Expect identifier name.", identifier.line, identifier.position)
                params += identifier
            } while (reader.test(TokenType.COMMA))
            if (!reader.test(TokenType.RIGHT_PAREN)) throw LurryParserException("Expect ')' after identifiers.", reader.peek().line, reader.peek().position)
        }
        return params
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
        return current
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
