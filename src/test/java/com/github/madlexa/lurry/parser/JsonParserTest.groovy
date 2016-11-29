package com.github.madlexa.lurry.parser

import com.github.madlexa.lurry.exception.LurryParseFormatException
import com.github.madlexa.lurry.exception.LurryPermissionException
import com.github.madlexa.lurry.model.Entity
import com.github.madlexa.lurry.model.Query
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

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
                [name   : "Test name",
                 queries: [
                         [name: "Test query",
                          sql : "Test sql"] as Query]] as Entity], entities)
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
                [name   : "name 1",
                 queries: [
                         [name: "query 1.1",
                          sql : "sql 1.1"] as Query,
                         [name: "query 1.2",
                          sql : "sql 1.2"] as Query,
                 ]] as Entity,
                [name   : "name 2",
                 queries: [
                         [name: "query 2.1",
                          sql : "sql 2.1"] as Query,
                         [name: "query 2.2",
                          sql : "sql 2.2"] as Query,
                 ]] as Entity], entities)
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
