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

    @ParameterizedTest(name = "test({0})")
    @ValueSource(strings = ["literal/number", "literal/string",
            "operation/number", "operation/string",
            "operation/boolean", "variable/global",
            "block/block", "if/if", "mapper/init",
            "function/function"
    ])
    void test(String name) {
        InputStream code = readResource("${name}.lurry")
        Lexer lexer = new Lexer(code)
        Parser parser = new Parser(lexer.tokenize())
        List<Statement> expressions = parser.parse()

        def ast = new StatementPrinter(ExpressionPrinter.INSTANCE).print(expressions)
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
