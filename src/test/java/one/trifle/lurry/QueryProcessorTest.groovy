package one.trifle.lurry

import one.trifle.lurry.model.Query
import spock.lang.Specification
import spock.lang.Unroll

import javax.sql.DataSource

import java.sql.Connection
import java.sql.DatabaseMetaData

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
        when(metaData.getDriverName()).thenReturn(driverName)

        new QueryProcessor(source).exec(new Query("test", template), params as Map<String, Object>) == result

        where:
        template                                        | params                          | driverName || result
        'bla ${test} bla'                               | [test: "1"]                     | ""         || "bla 1 bla"
        'bla $test bla'                                 | [test: 1, t1: 0]                | ""         || "bla 1 bla"
        'bla ${test == "1" ? res[0] : res[1]} bla'      | [test: "1", res: ["yes", "no"]] | ""         || "bla yes bla"
        'bla ${test == 1 ? "yes" : "no"} bla'           | [test: 2]                       | ""         || "bla no bla"
        'bla ${t1} ${t2} bla'                           | [t1: 1, t2: 2]                  | ""         || "bla 1 2 bla"
        'bla <% out << t1 %> ${t2} bla'                 | [t1: 1, t2: 2]                  | ""         || "bla 1 2 bla"
        'ID IN (<% out << (ids.join(",")) %>)'          | [ids: [1, 2, 3]]                | ""         || "ID IN (1,2,3)"
        'ID IN (${ids.collect{"\'$it\'"}.join(\',\')})' | [ids: ["1", "2", "3"]]          | ""         || "ID IN ('1','2','3')"
        "a = '\$a' AND b = '\$b'"                       | [a: "a"]                        | ""         || "a = 'a' AND b = 'null'"
        "a = '\${a.escape()}' AND b = '\$b'"            | [a: "'a\\'"]                    | ""         || "a = '''a\\''' AND b = 'null'"
        'a = ${a.escape()}'                             | [a: 'a']                        | ""         || 'a = a'
        'a = ${a.quote()}'                              | [a: 'a']                        | ""         || "a = 'a'"
//        'a in (${a.join()})'                            | [a: [1, 2, 3]]                  | ""                      || "a in (1,2,3)"
//        'a in (${a.join()})'                            | [a: ['1', '2', '3']]            | ""                      || "a in ('1','2','3')"
//        "a = '\${a.escape()}' AND b = '\$b'"            | [a: "'a\\'"]                    | "com.mysql.jdbc.Driver" || "a = '''a\\\\''' AND b = 'null'"
    }
}
