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

import java.io.InputStream

class Lexer(source: InputStream) {
    private val reader = CharReader(source.buffered().iterator())
    fun tokenize(): List<Token> {
        val result = ArrayList<Token>()
        while (reader.peekNext() != Char.MIN_VALUE) {
            reader.next()
            val token: Token? = when (val ch = reader.peek()) {
                '(' -> Token(TokenType.LEFT_PAREN, null, reader.line, reader.position)
                ')' -> Token(TokenType.RIGHT_PAREN, null, reader.line, reader.position)
                '-' -> Token(TokenType.MINUS, null, reader.line, reader.position)
                '+' -> Token(TokenType.PLUS, null, reader.line, reader.position)
                '*' -> Token(TokenType.STAR, null, reader.line, reader.position)
                ';' -> Token(TokenType.SEMICOLON, null, reader.line, reader.position)
                ' ', '\t', '\r', '\n' -> null
                '=' -> Token(if (reader.testNext('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL, null, reader.line, reader.position)
                '!' -> Token(if (reader.testNext('=')) TokenType.BANG_EQUAL else TokenType.BANG, null, reader.line, reader.position)
                '<' -> Token(if (reader.testNext('=')) TokenType.LESS_EQUAL else TokenType.LESS, null, reader.line, reader.position)
                '>' -> Token(if (reader.testNext('=')) TokenType.GREATER_EQUAL else TokenType.GREATER, null, reader.line, reader.position)
                '&' -> Token(if (reader.testNext('&')) TokenType.AND else TokenType.AMPERSAND, null, reader.line, reader.position)
                '|' -> Token(if (reader.testNext('|')) TokenType.OR else TokenType.VERTICAL_BAR, null, reader.line, reader.position)
                '"' -> string()
                '/' -> slash()
                else -> when {
                    ch.isDigit() -> number()
                    ch.isLetter() || ch == '_' -> identifier()
                    else -> throw LurryLexerException("Unexpected char [$ch]", reader.line, reader.position)
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
        Token(TokenType.SLASH, null, reader.line, reader.position)
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
            Char.MIN_VALUE -> throw LurryLexerException("Unterminated string", reader.line, reader.position)
        }
        return Token(TokenType.STRING, buffer.toString(), line, position)
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

        return Token(TokenType.NUMBER, value, line, position)
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
            "false" -> Token(TokenType.FALSE, null, line, position)
            "true" -> Token(TokenType.TRUE, null, line, position)
            "null" -> Token(TokenType.NULL, null, line, position)
            "var" -> Token(TokenType.VAR, null, line, position)
            "println" -> Token(TokenType.PRINT, null, line, position)
            else -> Token(TokenType.IDENTIFIER, identifier, line, position)
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