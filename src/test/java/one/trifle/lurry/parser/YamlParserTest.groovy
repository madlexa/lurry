package one.trifle.lurry.parser

import groovy.transform.CompileStatic
import one.trifle.lurry.exception.LurryParseFormatException
import one.trifle.lurry.mapper.LurryMapper
import one.trifle.lurry.mapper.MapperVersion
import one.trifle.lurry.mapper.custom.DefaultRowMapper
import one.trifle.lurry.model.Entity
import org.junit.Ignore
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
        assertEquals(1, entities.size())
        assertEquals(Person.class, entities[0].name)
        assertEquals(1, entities[0].queries.size())
        assertEquals("Test query", entities[0].queries[0].name)
        assertEquals("Test sql", entities[0].queries[0].sql)
        assertEquals(DefaultRowMapper.class, entities[0].queries[0].mapper)
    }

    @Test
    void customMapperClass() {
        List<Entity> entities = new YamlParser().parse(new ByteArrayInputStream("""
one.trifle.lurry.parser.YamlParserTest\$Person:
    Test query: 
        sql: |-
            Test sql
        mapper: one.trifle.lurry.parser.YamlParserTest\$TestMapper
""".getBytes("UTF-8")))
        assertEquals(1, entities.size())
        assertEquals(Person.class, entities[0].name)
        assertEquals(1, entities[0].queries.size())
        assertEquals("Test query", entities[0].queries[0].name)
        assertEquals("Test sql", entities[0].queries[0].sql)
        assertEquals(TestMapper.class, entities[0].queries[0].mapper)
    }

    @Test
    @Ignore
    void groovyMapperV1() {
        List<Entity> entities = new YamlParser().parse(new ByteArrayInputStream("""
one.trifle.lurry.parser.YamlParserTest\$Person:
    Test query: 
        sql: |-
            SELECT p.ID, p.login, c.ID AS cID, c.value
            FROM persons p
            LEFT JOIN contacts c ON c.personID = p.ID
            WHERE \${id ? "p.ID = \$id" : ''}
                  \${login ? "p.login = \${login.escape()} " : ""}
        mapper:
            version: 1.0
            unique: |-
                main: ID
                contact: cID
            import: |-
                one.trifle.lurry.parser.YamlParserTest\$Person
                one.trifle.lurry.parser.YamlParserTest\$Contact
            mapping: |-
                main: new Person(id: row.ID, login: row.login, contacts: [])
                contact:
                    #main.contacts << new Contact(id: row.cID, value: row.value)
""".getBytes("UTF-8")))
        assertEquals(1, entities.size())
        assertEquals(Person.class, entities[0].name)
        assertEquals(1, entities[0].queries.size())
        assertEquals("Test query", entities[0].queries[0].name)
        assertEquals("Test sql", entities[0].queries[0].sql)
        assertEquals(TestMapper.class, entities[0].queries[0].mapper)
    }

    @Test
    void manyEntitiesManyQueries() {
        List<Entity> entities = new YamlParser().parse(new ByteArrayInputStream("""
one.trifle.lurry.parser.YamlParserTest\$Person:
    query 1.1: |-
        sql 1.1
    query 1.2: 
        sql: sql 1.2
        mapper: one.trifle.lurry.parser.YamlParserTest\$TestMapper
one.trifle.lurry.parser.YamlParserTest\$Company:
    query 2.1: |-
        sql 2.1
    query 2.2: |-
        sql 2.2
""".getBytes("UTF-8")))
        entities.sort({ a, b -> (a.name.simpleName <=> b.name.simpleName) })
        entities.each { it.queries.sort({ a, b -> (a.name <=> b.name) }) }

        assertEquals(2, entities.size())
        assertEquals(Company.class, entities[0].name)
        assertEquals(Person.class, entities[1].name)
        assertEquals(2, entities[0].queries.size())
        assertEquals(2, entities[1].queries.size())

        assertEquals("query 2.1", entities[0].queries[0].name)
        assertEquals("sql 2.1", entities[0].queries[0].sql)
        assertEquals(DefaultRowMapper.class, entities[0].queries[0].mapper)

        assertEquals("query 2.2", entities[0].queries[1].name)
        assertEquals("sql 2.2", entities[0].queries[1].sql)
        assertEquals(DefaultRowMapper.class, entities[0].queries[1].mapper)

        assertEquals("query 1.1", entities[1].queries[0].name)
        assertEquals("sql 1.1", entities[1].queries[0].sql)
        assertEquals(DefaultRowMapper.class, entities[1].queries[0].mapper)

        assertEquals("query 1.2", entities[1].queries[1].name)
        assertEquals("sql 1.2", entities[1].queries[1].sql)
        assertEquals(TestMapper.class, entities[1].queries[1].mapper)
    }

    @Test(expected = LurryParseFormatException)
    void formatException() {
        new YamlParser().parse(new ByteArrayInputStream("bla-bla".getBytes("UTF-8")))
    }

    @Test(expected = LurryParseFormatException)
    void readException() {
        InputStream stream = mock(InputStream)
        when(stream.read(any(byte))).thenThrow(new IOException())
        new YamlParser().parse(stream)
    }

    static class Person {
        int id
        String login
        List<Contact> contacts
    }

    static class Contact {
        int id
        String value
    }
    static class Company {}

    static class TestMapper implements LurryMapper {
        MapperVersion getVersion() { null }
    }
}
