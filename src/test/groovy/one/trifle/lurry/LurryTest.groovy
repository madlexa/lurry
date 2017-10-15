package one.trifle.lurry

import one.trifle.lurry.connection.LurrySource
import org.junit.Test

import static junit.framework.TestCase.assertEquals

class LurryTest {
    @Test
    void "simple api"() {
        // INIT
        def source = new LurrySource() {}
        def lurry = new Lurry(source)
        def query = new LQuery("person", "all")
        def params = [:]

        // EXEC
        def results = lurry.<Person> query(query, params)

        // CHECK
        assertEquals(results, [])
    }

    static class Person {}
}