/*
 * Copyright 2017 Aleksey Dobrynin
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
package one.trifle.lurry.mapper.version_1;

import one.trifle.lurry.exception.LurryIllegalArgumentException;
import org.codehaus.groovy.antlr.AntlrParserPlugin;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Reduction;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author Aleksey Dobrynin
 */
class SourceModifierMapperParserPlugin extends AntlrParserPlugin {
    @Override
    public Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
        try {
            String text = replace(reader);
            return super.parseCST(sourceUnit, new StringReader(text));
        } catch (IOException exc) {
            throw new LurryIllegalArgumentException("parse unique error", exc);
        }
    }

    String replace(Reader source) throws IOException {
        StringBuilder text = new StringBuilder();
        int last = -1;
        boolean isStringOne = false;
        boolean isStringDouble = false;
        int chr;
        while ((chr = source.read()) != -1) {
            switch (chr) {
                case '\'':
                    if (!isStringDouble) {
                        if (last != '\\') {
                            isStringOne = !isStringOne;
                        }
                    }
                    break;
                case '"':
                    if (!isStringOne) {
                        if (last != '\\') {
                            isStringDouble = !isStringDouble;
                        }
                    }
                    break;
                case '#':
                    if (!isStringDouble && !isStringOne) {
                        last = chr;
                        text.append(Token.SHARP.replace);
                        continue;
                    }
                    break;
            }
            last = chr;
            text.append((char) chr);
        }
        return text.toString();
    }

    private enum Token {
        SHARP("data.");

        private final String replace;

        Token(String replace) {
            this.replace = replace;
        }
    }
}
