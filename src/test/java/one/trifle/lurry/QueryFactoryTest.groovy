package one.trifle.lurry

import groovy.transform.CompileStatic
import one.trifle.lurry.model.Entity
import one.trifle.lurry.model.Query
import one.trifle.lurry.parser.Parser
import one.trifle.lurry.reader.Reader
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@CompileStatic
class QueryFactoryTest {
    private Reader reader = mock(Reader.class)
    private Parser parser = mock(Parser.class)

    @Test
    void simple() {
        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())
        when(parser.parse(any(InputStream))).thenReturn([new Entity("entity", [new Query("query", "sql")] as Query[])])

        QueryFactory factory = new QueryFactory(reader, parser)
        assertEquals("sql", factory.get("entity", "query", [:]))
        assertEquals("", factory.get("entity", "test", [:]))
    }

    @Test
    void similarMultiple() {
        when(reader.iterator()).thenReturn([mock(InputStream), mock(InputStream)].iterator())
        when(parser.parse(any(InputStream)))
                .thenReturn([new Entity("entity", [new Query("query1", "sql1")] as Query[])])
                .thenReturn([new Entity("entity", [new Query("query2", "sql2")] as Query[])])

        QueryFactory factory = new QueryFactory(reader, parser)
        assertEquals("sql1", factory.get("entity", "query1", [:]))
        assertEquals("sql2", factory.get("entity", "query2", [:]))
    }

    @Test
    void othersMultiple() {
        when(reader.iterator()).thenReturn([mock(InputStream), mock(InputStream)].iterator())
        when(parser.parse(any(InputStream)))
                .thenReturn([
                new Entity("entity1", [new Query("query11", "sql11")] as Query[]),
                new Entity("entity2", [new Query("query21", "sql21")] as Query[])
        ])
                .thenReturn([
                new Entity("entity2", [new Query("query22", "sql22")] as Query[]),
                new Entity("entity1", [new Query("query12", "sql12")] as Query[])
        ])

        QueryFactory factory = new QueryFactory(reader, parser)
        assertEquals("sql11", factory.get("entity1", "query11", [test: "test"] as Map<String, Object>))
        assertEquals("sql12", factory.get("entity1", "query12", [test: "test"] as Map<String, Object>))
        assertEquals("sql21", factory.get("entity2", "query21", [test: "test"] as Map<String, Object>))
        assertEquals("sql22", factory.get("entity2", "query22", [test: "test"] as Map<String, Object>))
    }

}
