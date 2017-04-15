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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aleksey Dobrynin
 */
class SourceModifierUniqueParserPlugin extends AntlrParserPlugin {
    @Override
    public Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
        try {
            String text = build(parse(reader));
            return super.parseCST(sourceUnit, new StringReader(text));
        } catch (IOException exc) {
            throw new LurryIllegalArgumentException("parse unique error", exc);
        }
    }

    private List<Node> parse(Reader source) throws IOException {
        List<Node> nodes = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean isString = false;
        int chr;
        while ((chr = source.read()) != -1) {
            // key token
            if (!isString && chr == ':') {
                if (token.length() > 0) {
                    nodes.add(new Node(Token.KEY, token.toString()));
                    token.setLength(0);
                } else {
                    nodes.get(nodes.size() - 1).token = Token.KEY;
                }
                continue;
            }

            // next token
            if (!isString && Character.isWhitespace(chr)) {
                if (token.length() > 0) {
                    nodes.add(new Node(Token.VALUE, token.toString()));
                    token.setLength(0);
                }
                continue;
            }

            if (chr == '"') {
                isString = !isString;
                continue;
            }

            //build token
            token.append((char) chr);
        }
        // add last token
        if (token.length() > 0) {
            nodes.add(new Node(Token.VALUE, token.toString()));
        }
        return nodes;
    }

    private String build(List<Node> nodes) {
        StringBuilder result = new StringBuilder().append('[').append('\n');
        int nodesSize = nodes.size();
        for (int i = 0; i < nodesSize; i++) {
            if (nodes.get(i).token == Token.KEY) {
                result.append('\t').append('"').append(nodes.get(i).value).append('"').append('\t').append(':').append(' ').append('[');
            } else if (nodes.get(i).token == Token.VALUE) {
                result.append('"').append(nodes.get(i).value).append('"');
                if (i + 1 < nodesSize) {
                    if (nodes.get(i + 1).token == Token.VALUE) {
                        result.append(',');
                    } else {
                        result.append(']').append(',').append('\n');
                    }
                } else {
                    result.append(']');
                }
            }
        }
        return result.append('\n').append(']').toString();
    }

    private enum Token {
        KEY, VALUE
    }

    private static class Node {
        private final String value;
        private Token token;

        Node(Token token, String value) {
            this.token = token;
            this.value = value;
        }
    }
}
