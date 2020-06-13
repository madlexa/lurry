package one.trifle.lurry

import groovy.transform.CompileStatic
import one.trifle.lurry.connection.DefaultSafeString
import one.trifle.lurry.connection.MySqlSafeString
import org.junit.Test

import static junit.framework.TestCase.assertEquals

@CompileStatic
class LQueryTest {
    @Test
    void "empty template"() {
        // INIT
        def query = new LQuery("")
        Map<String, Object> params = [:]

        // EXEC
        def results = query.sql(params, DefaultSafeString.class)

        // CHECK
        assertEquals(results, "")
    }

    @Test
    void "constant template"() {
        // INIT
        def query = new LQuery("constant")
        def params = [test: "test"]

        // EXEC
        def results = query.sql(params, DefaultSafeString.class)

        // CHECK
        assertEquals(results, "constant")
    }

    @Test
    void "default connector with number params"() {
        // INIT
        def query = new LQuery("my test = \${test}")
        def params = [test: 1]

        // EXEC
        def results = query.sql(params, DefaultSafeString.class)

        // CHECK
        assertEquals(results, "my test = 1")
    }

    @Test
    void "default connector with string quote params"() {
        // INIT
        def query = new LQuery("my test = \${test.escape()}")
        def params = [test: "'1\\"]

        // EXEC
        def results = query.sql(params, DefaultSafeString.class)

        // CHECK
        assertEquals(results, "my test = '''1\\'")
    }

    @Test
    void "mysql connector with string quote params"() {
        // INIT
        def query = new LQuery("my test = \${test.escape()}")
        def params = [test: "'1\\"]

        // EXEC
        def results = query.sql(params, MySqlSafeString.class)

        // CHECK
        assertEquals(results, "my test = '''1\\\\'")
    }

    @Test
    void "default connector with list params"() {
        // INIT
        def query = new LQuery("my test in (\${test.join()})")
        def params = [test: [1,2,3] as Integer[]]

        // EXEC
        def results = query.sql(params, DefaultSafeString.class)

        // CHECK
        assertEquals(results, "my test in (1,2,3)")
    }

    @Test
    void "mysql connector with list params"() {
        // INIT
        def query = new LQuery("my test in (\${test.join()})")
        def params = [test: [1,2,3] as Integer[]]

        // EXEC
        def results = query.sql(params, MySqlSafeString.class)

        // CHECK
        assertEquals(results, "my test in (1,2,3)")
    }

    @Test
    void "default connector with list string params"() {
        // INIT
        def query = new LQuery("my test in (\${test.join()})")
        def params = [test: [1,2,3] as String[]]

        // EXEC
        def results = query.sql(params, DefaultSafeString.class)

        // CHECK
        assertEquals(results, "my test in ('1','2','3')")
    }

    @Test
    void "mysql connector with list string params"() {
        // INIT
        def query = new LQuery("my test in (\${test.join()})")
        def params = [test: [1,2,3] as String[]]

        // EXEC
        def results = query.sql(params, MySqlSafeString.class)

        // CHECK
        assertEquals(results, "my test in ('1','2','3')")
    }
}
