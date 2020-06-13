package one.trifle.lurry.lang

import groovy.transform.CompileStatic
import org.junit.Test

import static junit.framework.TestCase.assertEquals

@CompileStatic
class LexerTest {
    @Test
    void "empty code"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("".bytes))

        // EXEC
        def tokens = lexer.tokenize()

        // CHECK
        assertEquals("EOF",
                new TokenCollection(tokens).toString())
    }

    @Test
    void "2"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("2".bytes))

        // EXEC
        def tokens = lexer.tokenize()

        // CHECK
        assertEquals("""
NUMBER 2
EOF""".trim(),
                new TokenCollection(tokens).toString())
    }

    @Test
    void "(2)"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("(2)".bytes))

        // EXEC
        def tokens = lexer.tokenize()

        // CHECK
        assertEquals("""
LEFT_PAREN
NUMBER 2
RIGHT_PAREN
EOF""".trim(),
                new TokenCollection(tokens).toString())
    }

    @Test
    void "21.12"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("21.12".bytes))

        // EXEC
        def tokens = lexer.tokenize()

        // CHECK
        assertEquals("""
NUMBER 21.12
EOF""".trim(),
                new TokenCollection(tokens).toString())
    }

    @Test
    void "2 + 3"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("2 + 3".bytes))

        // EXEC
        def tokens = lexer.tokenize()

        // CHECK
        assertEquals("""
NUMBER 2
PLUS
NUMBER 3
EOF""".trim(),
                new TokenCollection(tokens).toString())
    }

    @Test
    void "21+32*43/115"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("21+32*43/115".bytes))

        // EXEC
        def tokens = lexer.tokenize()

        // CHECK
        assertEquals("""
NUMBER 21
PLUS
NUMBER 32
STAR
NUMBER 43
SLASH
NUMBER 115
EOF""".trim(),
                new TokenCollection(tokens).toString())
    }

    @Test
    void "(2 + 2) * 2"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("(2 + 2) * 2".bytes))

        // EXEC
        def tokens = lexer.tokenize()

        // CHECK
        assertEquals("""
LEFT_PAREN
NUMBER 2
PLUS
NUMBER 2
RIGHT_PAREN
STAR
NUMBER 2
EOF""".trim(),
                new TokenCollection(tokens).toString())
    }

    private static class TokenCollection {
        private final List<Token> tokens;

        TokenCollection(List<Token> tokens) {
            this.tokens = tokens
        }

        @Override
        String toString() {
            tokens.collect { it.toString() }.join("\n")
        }
    }
}