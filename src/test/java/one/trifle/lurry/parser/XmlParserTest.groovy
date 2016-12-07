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
    <entity name="one.trifle.lurry.parser.XmlParserTest\$Person">
        <queries>
            <query name="Test query">Test sql</query>
        </queries>
    </entity>
</entities>
""".getBytes("UTF-8")))
        assertEquals([new Entity(Person, [new Query("Test query", "Test sql")] as Query[])], entities)
        assertEquals("Test sql", entities.first().queries.first().sql)
    }

    @Test
    void manyEntitiesManyQueries() {
        List<Entity> entities = new XmlParser().parse(new ByteArrayInputStream("""
<entities>
    <entity name="one.trifle.lurry.parser.XmlParserTest\$Company">
        <queries>
            <query name="query 1.1">sql 1.1</query>
            <query name="query 1.2">sql 1.2</query>
        </queries>
    </entity>
        <entity name="one.trifle.lurry.parser.XmlParserTest\$Person">
        <queries>
            <query name="query 2.1">sql 2.1</query>
            <query name="query 2.2">sql 2.2</query>
        </queries>
    </entity>
</entities>
""".getBytes("UTF-8")))
        entities.sort({ a, b -> (a.name.getName() <=> b.name.getName()) })
        entities.each { it.queries.sort({ a, b -> (a.name <=> b.name) }) }

        assertEquals([
                new Entity(Company, [
                        new Query("query 1.1", "sql 1.1"),
                        new Query("query 1.2", "sql 1.2")
                ] as Query[]),
                new Entity(Person, [
                        new Query("query 2.1", "sql 2.1"),
                        new Query("query 2.2", "sql 2.2")
                ] as Query[])
        ], entities)
        assertEquals("sql 1.2", entities.first().queries.last().sql)
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

    @Test(expected = LurryParseFormatException)
    void classNotFoundException() {
        new XmlParser().parse(new ByteArrayInputStream("""
<entities>
    <entity name="test">
        <queries />
    </entity>
</entities>
""".getBytes("UTF-8")))
    }


    static class Person {}

    static class Company {}
}
