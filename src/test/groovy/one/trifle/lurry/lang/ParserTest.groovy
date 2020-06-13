package one.trifle.lurry.lang

import org.junit.Test

import static junit.framework.TestCase.assertEquals

class ParserTest {
    @Test
    void "empty code"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("", ast)

        def result = new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        assertEquals(null, result)
    }

    @Test
    void "2+2*2=6"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("2+2*2".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("(;(PLUS 2 (STAR 2 2)))", ast)

        def result = new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        assertEquals(6, result)
    }

    @Test
    void "(2+2)*2=8"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("(2+2)*2".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("(;(STAR (group (PLUS 2 2)) 2))", ast)

        def result = new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        assertEquals(8, result)
    }

    @Test(expected = RuntimeException)
    void "(2+2*2=error"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("(2+2*2".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        // todo error
    }

    @Test(expected = RuntimeException)
    void "2+2*2)=error"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("2+2*2)".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        // todo error
    }

    @Test
    void "2+2+2+2+2+2=12"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("2+2+2+2+2+2".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("(;(PLUS (PLUS (PLUS (PLUS (PLUS 2 2) 2) 2) 2) 2))", ast)

        def result = new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        assertEquals(12, result)
    }

    @Test
    void "(2)=2"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("(2)".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()
        ExpressionPrinter

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("(;(group 2))", ast)

        def result = new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        assertEquals(2, result)
    }

    @Test
    void "-2=-2"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("-2".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("(;(MINUS 2))", ast)

        def result = new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        assertEquals(-2, result)
    }

    @Test
    void "--2"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("--2".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("(;(MINUS (MINUS 2)))", ast)
    }

    @Test
    void "\"string\""() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("\"string\"".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("(;string)", ast)

        def result = new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        assertEquals("string", result)
    }

    @Test
    void "\"escape \\\"string\\\' \\\\\\n\\t\""() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("\"escape \\\"string\\' \\\\\\n\\t\"".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("(;escape \"string' \\\n\t)", ast)

        def result = new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        assertEquals("""escape "string' \\
\t""", result)
    }

    @Test
    void "\"(repeat string)\" * 3"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("\"(repeat string)\" * 3".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("(;(STAR (repeat string) 3))", ast)

        def result = new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        assertEquals("(repeat string)(repeat string)(repeat string)", result)
    }

    @Test
    void "1 + \"string\" + 1"() {
        // INIT
        Lexer lexer = new Lexer(new ByteArrayInputStream("1 + \"string\" + 1".bytes))
        Parser parser = new Parser(lexer.tokenize())

        // EXEC
        List<Statement> expressions = parser.parse()

        // CHECK
        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        assertEquals("(;(PLUS (PLUS 1 string) 1))", ast)

        def result = new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        assertEquals("1string1", result)
    }
}
