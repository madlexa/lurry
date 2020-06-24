package one.trifle.lurry.lang

import groovy.transform.CompileStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static org.junit.jupiter.api.Assertions.assertEquals

@CompileStatic
class ParserTest {
    PrintStream standardOutput
    ByteArrayOutputStream testOutput = new ByteArrayOutputStream()

    @BeforeEach
    void init() {
        standardOutput = System.out
        System.out = new PrintStream(testOutput, true, "UTF-8")
    }

    @AfterEach
    void finish() {
        System.out.close()
        System.out = standardOutput
    }

    @ParameterizedTest
    @ValueSource(strings = ["literal/number", "literal/string",
            "operation/number", "operation/string",
            "variable/global"])
    void test(String name) {
        InputStream code = readResource("${name}.lurry")
        Lexer lexer = new Lexer(code)
        Parser parser = new Parser(lexer.tokenize())
        List<Statement> expressions = parser.parse()

        def ast = new StatementPrinter(new ExpressionPrinter()).print(expressions)
        InputStream tree = readResource("${name}.tree")
        assertEquals(tree.text, ast)

        new StatementInterpreter(new ExpressionInterpreter()).interpret(expressions)
        InputStream result = readResource("${name}.result")
        assertEquals(result.text, testOutput.toString())
    }

    private static InputStream readResource(String fileName) {
        return new FileInputStream(new File(ParserTest.class.classLoader.getResource(fileName).getFile()))
    }
}
