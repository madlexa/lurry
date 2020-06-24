package one.trifle.lurry.lang

import java.io.InputStream

class Lexer(source: InputStream) {
    private val reader = CharReader(source.buffered().iterator())
    fun tokenize(): List<Token> {
        val result = ArrayList<Token>()
        while (reader.peekNext() != Char.MIN_VALUE) {
            reader.next()
            val token: Token? = when (val ch = reader.peek()) {
                '(' -> Token(TokenType.LEFT_PAREN, TokenValue.EMPTY, reader.line, reader.position)
                ')' -> Token(TokenType.RIGHT_PAREN, TokenValue.EMPTY, reader.line, reader.position)
                '-' -> Token(TokenType.MINUS, TokenValue.EMPTY, reader.line, reader.position)
                '+' -> Token(TokenType.PLUS, TokenValue.EMPTY, reader.line, reader.position)
                '*' -> Token(TokenType.STAR, TokenValue.EMPTY, reader.line, reader.position)
                ';' -> Token(TokenType.SEMICOLON, TokenValue.EMPTY, reader.line, reader.position)
                ' ', '\t', '\r', '\n' -> null
                '=' -> Token(if (reader.testNext('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL, TokenValue.EMPTY, reader.line, reader.position)
                '!' -> Token(if (reader.testNext('=')) TokenType.BANG_EQUAL else TokenType.BANG, TokenValue.EMPTY, reader.line, reader.position)
                '<' -> Token(if (reader.testNext('=')) TokenType.LESS_EQUAL else TokenType.LESS, TokenValue.EMPTY, reader.line, reader.position)
                '>' -> Token(if (reader.testNext('=')) TokenType.GREATER_EQUAL else TokenType.GREATER, TokenValue.EMPTY, reader.line, reader.position)
                '"' -> string()
                '/' -> slash()
                else -> when {
                    ch.isDigit() -> number()
                    ch.isLetter() || ch == '_' -> identifier()
                    else -> throw RuntimeException("Unexpected char [$ch] line: ${reader.line} position ${reader.position}")
                }
            }
            if (token != null) result += token
        }
        result += Token.EOF
        return result
    }

    private fun slash(): Token? = if (reader.testNext('/')) { // comment
        while (reader.peek() != '\n' && reader.peek() != Char.MIN_VALUE) reader.next() // ignore comment
        null
    } else {
        Token(TokenType.SLASH, TokenValue.EMPTY, reader.line, reader.position)
    }

    private fun string(): Token {
        val buffer = StringBuilder()
        val line = reader.line
        val position = reader.position
        while (reader.peekNext() != '"' && reader.peekNext() != Char.MIN_VALUE) {
            var ch = reader.next()
            if (ch == '\\') {
                ch = when (val nextCh = reader.peekNext()) {
                    't' -> '\t'
                    'n' -> '\n'
                    'r' -> '\r'
                    '"' -> '\"'
                    '\'' -> '\''
                    else -> nextCh // TODO
                }
                reader.next()
                buffer.append(ch)
            } else {
                buffer.append(ch)
            }
        }
        when (reader.peekNext()) {
            '"' -> reader.next()
            Char.MIN_VALUE -> throw RuntimeException("Unterminated string line: ${reader.line} position ${reader.position}")
        }
        return Token(TokenType.STRING, TokenValue(buffer.toString()), line, position)
    }

    private fun number(): Token {
        val buffer = StringBuilder()
        val line = reader.line
        val position = reader.position
        var ch = reader.peek()
        buffer.append(ch)
        while (reader.peekNext().isDigit()) {
            ch = reader.next()
            buffer.append(ch)
        }
        if (reader.peekNext() == '.' || reader.peekNext().isDigit()) {
            reader.next()
            buffer.append(reader.peek())
            while (reader.peekNext().isDigit()) {
                ch = reader.next()
                buffer.append(ch)
            }
        }
        val number: String = buffer.toString()
        val value: Number = if (number.contains('.')) {
            number.toDouble() // todo
        } else {
            number.toInt() // todo
        }

        return Token(TokenType.NUMBER, TokenValue(value), line, position)
    }

    private fun identifier(): Token {
        val buffer = StringBuilder()
        val line = reader.line
        val position = reader.position
        buffer.append(reader.peek())
        while (reader.peekNext().isDigit() || reader.peekNext().isLetter() || reader.peekNext() == '_') {
            buffer.append(reader.next())
        }
        return when (val identifier = buffer.toString()) {
            "false" -> Token(TokenType.FALSE, TokenValue.EMPTY, line, position)
            "true" -> Token(TokenType.TRUE, TokenValue.EMPTY, line, position)
            "null" -> Token(TokenType.NULL, TokenValue.EMPTY, line, position)
            "var" -> Token(TokenType.VAR, TokenValue.EMPTY, line, position)
            "println" -> Token(TokenType.PRINT, TokenValue.EMPTY, line, position)
            else -> Token(TokenType.IDENTIFIER, TokenValue(identifier), line, position)
        }
    }
}

private class CharReader(private val source: ByteIterator) {
    private var current: Char = Char.MIN_VALUE
    private var next: Char = Char.MIN_VALUE
    var line: Int = 1
        private set
    var position: Int = -1
        private set

    init {
        next()
    }

    fun peek(): Char = current
    fun peekNext(): Char = next

    fun next(): Char {
        current = next
        next = if (source.hasNext()) {
            source.nextByte().toChar().apply {
                if (this == '\n') {
                    position = 0
                    line++
                } else {
                    position++
                }
            }
        } else {
            Char.MIN_VALUE
        }
        return peek()
    }

    fun testNext(ch: Char): Boolean {
        if (next == ch) {
            next()
            return true
        }
        return false
    }
}