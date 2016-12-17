package one.trifle.lurry.logic

import one.trifle.lurry.exception.LurryIllegalArgumentException
import one.trifle.lurry.exception.LurrySqlException
import one.trifle.lurry.model.Query
import spock.lang.Specification
import spock.lang.Unroll

import javax.sql.DataSource

import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.SQLException

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class QueryProcessorTest extends Specification {

    @Unroll
    "template(#template, #params, #driverName) == #result"() {
        expect:
        DatabaseMetaData metaData = mock(DatabaseMetaData)
        Connection connection = mock(Connection)
        DataSource source = mock(DataSource)

        when(source.getConnection()).thenReturn(connection)
        when(connection.getMetaData()).thenReturn(metaData)
        when(metaData.getDatabaseProductName()).thenReturn(driverName)

        new QueryProcessor(source).prepare(new Query("test", template), params as Map<String, Object>) == result

        where:
        template                                        | params                                 | driverName   || result
        'bla ${test} bla'                               | [test: "1"]                            | ""           || "bla 1 bla"
        'bla $test bla'                                 | [test: 1, t1: 0]                       | ""           || "bla 1 bla"
        'bla ${test == "1" ? res[0] : res[1]} bla'      | [test: "1", res: ["yes", "no"]]        | ""           || "bla yes bla"
        'bla ${test == 1 ? "yes" : "no"} bla'           | [test: 2]                              | "PostgreSQL" || "bla no bla"
        'bla ${t1} ${t2} bla'                           | [t1: 1, t2: 2]                         | ""           || "bla 1 2 bla"
        'bla <% out << t1 %> ${t2} bla'                 | [t1: 1, t2: 2]                         | ""           || "bla 1 2 bla"
        'ID IN (<% out << (ids.join(",")) %>)'          | [ids: [1, 2, 3]]                       | ""           || "ID IN (1,2,3)"
        'ID IN (${ids.collect{"\'$it\'"}.join(\',\')})' | [ids: ["1", "2", "3"]]                 | ""           || "ID IN ('1','2','3')"
        "a = '\$a' AND b = '\$b'"           | [a: "a"]                               | ""           || "a = 'a' AND b = 'null'"
        "a = \${a.escape()} AND b = '\$b'"  | [a: "'a\\'"]                           | ""           || "a = '''a\\''' AND b = 'null'"
        'a = ${a.escape()}'                 | [a: 'a']                               | ""           || "a = 'a'"
        'a in (${a.join()})'                | [a: [1, 2, 3, 0.7] as Number[]]        | ""           || "a in (1,2,3,0.7)"
        'a in (${a.join()})'                | [a: ['1', "\\'2\\'", '3'] as String[]] | "PostgreSQL" || "a in ('1','\\''2\\''','3')"

        "a = \${a.escape()} AND b = '\$b'"  | [a: "'a\\'"]                           | "MySQL"      || "a = '''a\\\\''' AND b = 'null'"
        'a in (${a.join()})'                | [a: [1, 2, 3] as Number[]]             | "MySQL"      || "a in (1,2,3)"
        'a in (${a.join()})'                | [a: ['1', "\\'2\\'", '3'] as String[]] | "MySQL"      || "a in ('1','\\\\''2\\\\''','3')"
        '${String.valueOf(" ").hashCode()}' | [:]                                    | "MySQL"      || "32"
    }

    @Unroll
    "some error"() {
        when:
        new QueryProcessor(null)

        then:
        thrown(LurryIllegalArgumentException)
//-----------------
        when:
        DataSource source = mock(DataSource)

        when(source.getConnection()).thenReturn(null)

        new QueryProcessor(source).prepare(new Query("test", "bla \${"), [:] as Map<String, Object>)

        then:
        thrown(LurrySqlException)
//-----------------
        when:
        source = mock(DataSource)
        Connection connection = mock(Connection)

        when(source.getConnection()).thenReturn(connection)
        when(connection.getMetaData()).thenReturn(null)

        new QueryProcessor(source).prepare(new Query("test", "bla \${"), [:] as Map<String, Object>)

        then:
        thrown(LurrySqlException)
//-----------------
        when:
        DatabaseMetaData metaData = mock(DatabaseMetaData)

        when(source.getConnection()).thenReturn(connection)
        when(connection.getMetaData()).thenReturn(metaData)
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL")

        new QueryProcessor(source).prepare(new Query("test", "bla \${"), [:] as Map<String, Object>)

        then:
        thrown(LurrySqlException)
//-----------------
        when:
        when(source.getConnection()).thenThrow(SQLException)

        new QueryProcessor(source).prepare(new Query("test", "bla \${"), [:] as Map<String, Object>)

        then:
        thrown(LurrySqlException)
    }
}