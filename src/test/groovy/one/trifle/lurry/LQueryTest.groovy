package one.trifle.lurry

import one.trifle.lurry.connection.DatabaseType
import one.trifle.lurry.connection.LurrySource
import org.junit.Test

import static junit.framework.TestCase.assertEquals

class LQueryTest {
    @Test
    void "empty template"() {
        // INIT
        def query = new LQuery("", new LurrySource() {
            @Override DatabaseType getType() { DatabaseType.DEFAULT }
        })
        def params = [:]

        // EXEC
        def results = query.sql(params)

        // CHECK
        assertEquals(results, "")
    }

    @Test
    void "constant template"() {
        // INIT
        def query = new LQuery("constant", new LurrySource() {
            @Override DatabaseType getType() { DatabaseType.DEFAULT }
        })
        def params = [test: "test"]

        // EXEC
        def results = query.sql(params)

        // CHECK
        assertEquals(results, "constant")
    }

    @Test
    void "default connector with number params"() {
        // INIT
        def query = new LQuery("my test = \${test}", new LurrySource() {
            @Override DatabaseType getType() { DatabaseType.DEFAULT }
        })
        def params = [test: 1]

        // EXEC
        def results = query.sql(params)

        // CHECK
        assertEquals(results, "my test = 1")
    }

    @Test
    void "default connector with string quote params"() {
        // INIT
        def query = new LQuery("my test = \${test.escape()}", new LurrySource() {
            @Override DatabaseType getType() { DatabaseType.DEFAULT }
        })
        def params = [test: "'1\\"]

        // EXEC
        def results = query.sql(params)

        // CHECK
        assertEquals(results, "my test = '''1\\'")
    }

    @Test
    void "mysql connector with string quote params"() {
        // INIT
        def query = new LQuery("my test = \${test.escape()}", new LurrySource() {
            @Override DatabaseType getType() { DatabaseType.MYSQL }
        })
        def params = [test: "'1\\"]

        // EXEC
        def results = query.sql(params)

        // CHECK
        assertEquals(results, "my test = '''1\\\\'")
    }
}
