/*
 * Copyright 2016 Aleksey Dobrynin
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
public class SourceModifierParserPlugin extends AntlrParserPlugin {
    @Override
    public Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
        try {
            String text = modify(reader);
            return super.parseCST(sourceUnit, new StringReader(text));
        } catch (IOException exc) {
            throw new LurryIllegalArgumentException("parse unique error", exc);
        }
    }

    private String modify(Reader source) throws IOException {
        StringBuilder text = new StringBuilder();
        StringBuilder token = new StringBuilder();
        Token last = Token.NONE;
        int chr;
        while ((chr = source.read()) != -1) {
            if (chr == ':') {
                if (last == Token.NONE) {
                    text.append('[');
                } else {
                    text.append(']').append(',');
                }
                text.append(token).append(':').append('[');
                token.setLength(0);
                last = Token.KEY;
                continue;
            }

            if (chr == ' ' || chr == '\n' || chr == '\t') {
                if (token.length() > 0) {
                    if (last == Token.VALUE) {
                        text.append(',');
                    }
                    text.append('"').append(token).append('"');
                    token.setLength(0);
                    last = Token.VALUE;
                }
                continue;
            }

            if (chr == '"' || chr == '\\') {
                token.append('\\');
            }
            token.append((char) chr);
        }
        if (token.length() > 0) {
            if (last == Token.VALUE) {
                text.append(',');
            }
            text.append('"').append(token).append('"');
        }
        text.append(']').append(']');
        return text.toString();
    }

    enum Token {
        KEY, VALUE, NONE
    }
}
