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
package one.trifle.lurry.parser;

import one.trifle.lurry.exception.LurryParseFormatException;
import one.trifle.lurry.model.Entity;
import one.trifle.lurry.model.Query;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * implementation {@code Parser} is used to convert xml to lurry format
 * <p>
 * xml example:
 * <pre>
 * &lt;entities&gt;
 *     &lt;entity name="com.mysite.Person"&gt;
 *         &lt;queries&gt;
 *             &lt;query name="get"&gt;&lt;![CDATA[
 *             SELECT *
 *             FROM persons
 *             WHERE ${id ? "ID = $id" : ''}
 *                   ${login ? 'login = ' + login : ''}
 *             ]]&gt;&lt;/query&gt;
 *             &lt;query name="delete"&gt;&lt;![CDATA[
 *             DELETE FROM persons
 *             WHERE ${id ? 'ID = ' + id : ''}
 *                   ${login ? 'login = ' + login : ''}
 *             ]]&gt;&lt;/query&gt;
 *         &lt;/queries&gt;
 *     &lt;/entity&gt;
 *     &lt;entity name="com.mysite.Company"&gt;
 *         &lt;queries&gt;
 *             &lt;query name="get"&gt;SELECT * FROM companies WHERE ID = $id&lt;/query&gt;
 *             &lt;query name="delete"&gt;DELETE FROM companies WHERE ID = $id&lt;/query&gt;
 *         &lt;/queries&gt;
 *     &lt;/entity&gt;
 * &lt;/entities&gt;
 * </pre>
 *
 * @author Aleksey Dobrynin
 */
public class XmlParser implements Parser {
    @Override
    public List<Entity> parse(InputStream source) {
        try {
            return new Mapper(source).map();
        } catch (SAXException | ParserConfigurationException | IOException exc) {
            throw new LurryParseFormatException("xml parse error", exc);
        }
    }

    private static final class SAXHandler extends DefaultHandler {

        private final List<Entity> entities = new ArrayList<>();
        private Entity entity = null;

        private List<Query> queries = null;
        private Query query = null;

        private String content = null;

        List<Entity> getEntities() {
            return entities;
        }

        @Override
        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes)
                throws SAXException {

            switch (qName) {
                case "entity":
                    try {
                        entity = new Entity(Class.forName(attributes.getValue("name")));
                        entities.add(entity);
                    } catch (ClassNotFoundException exc) {
                        throw new LurryParseFormatException("xml parse error", exc);
                    }
                    queries = new ArrayList<>();
                    break;
                case "query":
                    query = new Query(attributes.getValue("name"));
                    queries.add(query);
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName,
                               String qName) throws SAXException {
            switch (qName) {
                //Add the employee to list once end tag is found
                case "entity":
                    entity.setQueries(queries.toArray(new Query[0]));
                    break;
                case "query":
                    query.setSql(content);
                    break;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            content = String.copyValueOf(ch, start, length).trim();
        }
    }

    private final class Mapper {
        private final InputStream source;

        Mapper(InputStream source) {
            this.source = source;
        }

        List<Entity> map() throws ParserConfigurationException, SAXException, IOException {
            SAXHandler handler = new SAXHandler();
            SAXParserFactory.newInstance().newSAXParser().parse(source, handler);
            return handler.getEntities();
        }
    }
}
