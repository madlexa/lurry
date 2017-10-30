package one.trifle.lurry

import groovy.transform.CompileStatic
import one.trifle.lurry.connection.DatabaseType
import one.trifle.lurry.connection.LurrySource
import org.jetbrains.annotations.NotNull
import org.junit.Test

import static junit.framework.TestCase.assertEquals

@CompileStatic
class LurryTest {
    @Test
    void "simple api"() {
        // INIT
        def source = new LurrySource() {
            @Override DatabaseType getType() { DatabaseType.DEFAULT }
            @Override List<LurrySource.Row> execute(@NotNull LQuery query, @NotNull Map<String, ?> params) { [] }
        }
        def lurry = new Lurry(source)
        def query = new LQuery("")
        def params = [:]

        // EXEC
        def results = lurry.<Person> query(query, params)

        // CHECK
        assertEquals(results, [])
    }

    static class Person {}
}