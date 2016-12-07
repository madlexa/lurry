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
class YamlParserTest {
    @Test
    void oneEntityOneQuery() {
        List<Entity> entities = new YamlParser().parse(new ByteArrayInputStream("""
one.trifle.lurry.parser.YamlParserTest\$Person:
    Test query: |-
        Test sql
""".getBytes("UTF-8")))
        assertEquals([new Entity(Person, [new Query("Test query", "Test sql")] as Query[])], entities)
    }

    @Test
    void manyEntitiesManyQueries() {
        List<Entity> entities = new YamlParser().parse(new ByteArrayInputStream("""
one.trifle.lurry.parser.YamlParserTest\$Person:
    query 1.1: |-
        sql 1.1
    query 1.2: |-
        sql 1.2
one.trifle.lurry.parser.YamlParserTest\$Company:
    query 2.1: |-
        sql 2.1
    query 2.2: |-
        sql 2.2
""".getBytes("UTF-8")))
        entities.sort({ a, b -> (a.name.getName() <=> b.name.getName()) })
        entities.each { it.queries.sort({ a, b -> (a.name <=> b.name) }) }
        assertEquals([
                new Entity(Company, [
                        new Query("query 2.1", "sql 2.1"),
                        new Query("query 2.2", "sql 2.2")
                ] as Query[]),
                new Entity(Person, [
                        new Query("query 1.1", "sql 1.1"),
                        new Query("query 1.2", "sql 1.2")
                ] as Query[])
        ], entities)
    }

    @Test(expected = LurryParseFormatException)
    void formatException() {
        new YamlParser().parse(new ByteArrayInputStream("bla-bla".getBytes("UTF-8")))
    }

    @Test(expected = LurryPermissionException)
    void readException() {
        InputStream stream = mock(InputStream)
        when(stream.read(any(byte))).thenThrow(new IOException())
        new YamlParser().parse(stream)
    }

    @Test(expected = LurryParseFormatException)
    void classNotFoundException() {
        new YamlParser().parse(new ByteArrayInputStream("""
test:
    Test query: |-
        Test sql
""".getBytes("UTF-8")))
    }

    static class Person {}

    static class Company {}
}
