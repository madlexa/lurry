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
    "name": "one.trifle.lurry.parser.JsonParserTest\$Person",
    "queries": [{
        "name": "Test query",
        "sql": "Test sql"
    }]
}]
""".getBytes("UTF-8")))
        assertEquals([
                new Entity(Person.class,
                        [new Query("Test query", "Test sql")] as Query[])], entities)
    }

    @Test
    void manyEntitiesManyQueries() {
        List<Entity> entities = new JsonParser().parse(new ByteArrayInputStream("""
[{
    "name": "one.trifle.lurry.parser.JsonParserTest\$Person",
    "queries": [{
        "name": "query 1.1",
        "sql": "sql 1.1"
    },{
        "name": "query 1.2",
        "sql": "sql 1.2"
    }]
},{
    "name": "one.trifle.lurry.parser.JsonParserTest\$Company",
    "queries": [{
        "name": "query 2.1",
        "sql": "sql 2.1"
    },{
        "name": "query 2.2",
        "sql": "sql 2.2"
    }]
}]
""".getBytes("UTF-8")))
        entities.sort({ a, b -> (a.name.getName() <=> b.name.getName()) })
        entities.each { it.queries.sort({ a, b -> (a.name <=> b.name) }) }
        assertEquals([
                new Entity(Company.class, [
                        new Query("query 2.1", "sql 2.1"),
                        new Query("query 2.2", "sql 2.2")
                ] as Query[]),
                new Entity(Person.class, [
                        new Query("query 1.1", "sql 1.1"),
                        new Query("query 1.2", "sql 1.2")
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

    @Test(expected = LurryParseFormatException)
    void classNotFoundException() {
        new JsonParser().parse(new ByteArrayInputStream("""
[{
    "name": "test",
    "queries": []
}]
""".getBytes("UTF-8")))
    }

    static class Person {}

    static class Company {}
}
