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

    private fun declaration(): Statement = when(reader.peek().type) {
            TokenType.VAR -> varDeclaration()
            TokenType.FUN -> TODO()
            else -> statement()
        }

    private fun varDeclaration(): Statement {
        if(!reader.testNext(TokenType.IDENTIFIER)) throw RuntimeException("Expect variable name.")
        val name = reader.peek()
        reader.next()
        val value: Expression = if (reader.test(TokenType.EQUAL))
            expression()
        else
            LiteralExpression(Token.NULL)
        if (!reader.test(TokenType.SEMICOLON))  throw RuntimeException("Expect ';' after variable declaration")
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
        if(!reader.test(TokenType.SEMICOLON)) throw RuntimeException("Expect ';' after value.")
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
                    throw RuntimeException("Expect ')' after expression  line: ${line}  position: ${position}")
                }
                GroupingExpression(expr)
            }
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

    fun test(type: TokenType): Boolean {
        if (current.type == type) {
            next()
            return true
        }
        return false
    }
}
