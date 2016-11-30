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
 * TODO
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
                    entity = new Entity();
                    entity.setName(attributes.getValue("name"));
                    queries = new ArrayList<>();
                    break;
                case "query":
                    query = new Query();
                    query.setName(attributes.getValue("name"));
            }
        }

        @Override
        public void endElement(String uri, String localName,
                               String qName) throws SAXException {
            switch (qName) {
                //Add the employee to list once end tag is found
                case "entity":
                    entity.setQueries(queries.toArray(new Query[0]));
                    entities.add(entity);
                    break;
                case "query":
                    query.setSql(content);
                    queries.add(query);
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
