package one.trifle.lurry.lang

import java.util.*

class Parser(tokens: List<Token>) {
    private val reader = TokenReader(tokens)

    fun parse(): List<Statement> {
        val statements = ArrayList<Statement>()
        reader.next()
        while (reader.peek() != Token.EOF) {
//            reader.next()
            statements += declaration()
        }
        return statements
    }

    private fun declaration(): Statement {
        // TODO CLASS/FUN/VAR
        return statement()
    }

    private fun statement(): Statement {
        // todo FOR/IF/PRINT/RETURN/WHILE/LEFT_BRACE
        return expressionStatement()
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
        val expr: Expression = or()
        // TODO EQ
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
        val expr: Expression = comparison()
        // TODO BANG_EQUAL, EQUAL_EQUAL
        return expr
    }

    private fun comparison(): Expression {
        val expr: Expression = addition()
        // TODO GREATER, GREATER_EQUAL, LESS, LESS_EQUAL
        return expr
    }

    private fun addition(): Expression {
        var expr: Expression = multiplication()
        while (reader.peek().type == TokenType.MINUS || reader.peek().type == TokenType.PLUS) {
            val operator: Token = reader.peek()
            reader.next()
            val right: Expression = multiplication()
            expr = BinaryExpression(operator, expr, right)
        }
        return expr
    }

    private fun multiplication(): Expression {
        var expr: Expression = unary()
        while (reader.peek().type == TokenType.SLASH || reader.peek().type == TokenType.STAR) {
            val operator: Token = reader.peek()
            reader.next()
            val right: Expression = unary()
            expr = BinaryExpression(operator, expr, right)
        }
        return expr
    }

    private fun unary(): Expression {
        // todo BANG
        if (reader.peek().type == TokenType.MINUS) {
            val operator: Token = reader.peek()
            reader.next()
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

    //< Functions call
    //> primary
    private fun primary(): Expression {
        val expr = when (reader.peek().type) {
            TokenType.FALSE -> LiteralExpression(reader.peek())
            TokenType.TRUE -> LiteralExpression(reader.peek())
            TokenType.NIL -> LiteralExpression(reader.peek())
            TokenType.NUMBER -> LiteralExpression(reader.peek())
            TokenType.STRING -> LiteralExpression(reader.peek())
            TokenType.LEFT_PAREN -> {
                val line = reader.peek().line
                val position = reader.peek().position
                reader.next()
                val expr: Expression = expression()
                if (reader.peek().type != TokenType.RIGHT_PAREN) {
                    throw RuntimeException("Expect ')' after expression  line: ${line}  position: ${position}")
                }
                GroupingExpression(expr)
            }
            // TODO IDENTIFIER
            else -> throw RuntimeException("Expect expression [${reader.peek()}] line: ${reader.peek().line}  position: ${reader.peek().position}")
        }
        reader.next()
        return expr
    }
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

    fun next(): Token {
        current = next
        if (++position < tokens.size) {
            next = tokens[position]
        } else {
            next = Token.EOF
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
}
