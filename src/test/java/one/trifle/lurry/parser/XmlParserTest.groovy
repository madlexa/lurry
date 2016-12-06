package one.trifle.lurry.parser

import groovy.transform.CompileStatic
import one.trifle.lurry.exception.LurryParseFormatException
import one.trifle.lurry.model.Entity
import one.trifle.lurry.model.Query
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

@CompileStatic
class XmlParserTest {
    @Test
    void oneEntityOneQuery() {
        List<Entity> entities = new XmlParser().parse(new ByteArrayInputStream("""
<entities>
    <entity name="Test name">
        <queries>
            <query name="Test query">Test sql</query>
        </queries>
    </entity>
</entities>
""".getBytes("UTF-8")))
        assertEquals([new Entity("Test name", [new Query("Test query", "Test sql")] as Query[])], entities)
    }

    @Test
    void manyEntitiesManyQueries() {
        List<Entity> entities = new XmlParser().parse(new ByteArrayInputStream("""
<entities>
    <entity name="name 1">
        <queries>
            <query name="query 1.1">sql 1.1</query>
            <query name="query 1.2">sql 1.2</query>
        </queries>
    </entity>
        <entity name="name 2">
        <queries>
            <query name="query 2.1">sql 2.1</query>
            <query name="query 2.2">sql 2.2</query>
        </queries>
    </entity>
</entities>
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
                ] as Query[])
        ], entities)
    }

    @Test(expected = LurryParseFormatException)
    void formatException() {
        new XmlParser().parse(new ByteArrayInputStream("bla-bla".getBytes("UTF-8")))
    }

    @Test(expected = LurryParseFormatException)
    void readException() {
        InputStream stream = mock(InputStream)
        when(stream.read(any(byte))).thenThrow(new IOException())
        new XmlParser().parse(stream)
    }
}
