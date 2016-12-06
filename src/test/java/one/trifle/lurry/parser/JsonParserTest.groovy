package one.trifle.lurry.parser

import groovy.transform.CompileStatic
import one.trifle.lurry.exception.LurryParseFormatException
import one.trifle.lurry.exception.LurryPermissionException
import one.trifle.lurry.model.Entity
import one.trifle.lurry.model.Query
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@CompileStatic
class JsonParserTest {
    @Test
    void oneEntityOneQuery() {
        List<Entity> entities = new JsonParser().parse(new ByteArrayInputStream("""
[{
    "name": "Test name",
    "queries": [{
        "name": "Test query",
        "sql": "Test sql"
    }]
}]
""".getBytes("UTF-8")))
        assertEquals([
                new Entity("Test name",
                        [new Query("Test query", "Test sql")] as Query[])], entities)
    }

    @Test
    void manyEntitiesManyQueries() {
        List<Entity> entities = new JsonParser().parse(new ByteArrayInputStream("""
[{
    "name": "name 1",
    "queries": [{
        "name": "query 1.1",
        "sql": "sql 1.1"
    },{
        "name": "query 1.2",
        "sql": "sql 1.2"
    }]
},{
    "name": "name 2",
    "queries": [{
        "name": "query 2.1",
        "sql": "sql 2.1"
    },{
        "name": "query 2.2",
        "sql": "sql 2.2"
    }]
}]
""".getBytes("UTF-8")))
        entities.sort({ a, b -> (a.name <=> b.name) })
        entities.each { it.queries.sort({ a, b -> (a.name <=> b.name) }) }
        assertEquals([
                new Entity("name 1", [
                        new Query("query 1.1", "sql 1.1"),
                        new Query("query 1.2", "sql 1.2")
                ] as Query[]),
                new Entity("name 2", [
                        new Query("query 2.1", "sql 2.1"),
                        new Query("query 2.2", "sql 2.2")
                ] as Query[])], entities)
    }

    @Test(expected = LurryParseFormatException)
    void formatException() {
        new JsonParser().parse(new ByteArrayInputStream("bla-bla".getBytes("UTF-8")))
    }

    @Test(expected = LurryPermissionException)
    void readException() {
        InputStream stream = mock(InputStream)
        when(stream.read(any(byte))).thenThrow(new IOException())
        new JsonParser().parse(stream)
    }
}
