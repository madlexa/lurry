package one.trifle.lurry

import one.trifle.lurry.connection.LurrySource
import org.junit.Test

import static junit.framework.TestCase.assertEquals

class LQueryTest {
    @Test
    void "empty template"() {
        // INIT
        def query = new LQuery("", new LurrySource() {})
        def params = [:]

        // EXEC
        def results = query.sql(params)

        // CHECK
        assertEquals(results, "")

    }

    @Test
    void "constant template"() {
        // INIT
        def query = new LQuery("constant", new LurrySource() {})
        def params = [test: "test"]

        // EXEC
        def results = query.sql(params)

        // CHECK
        assertEquals(results, "constant")

    }
}
