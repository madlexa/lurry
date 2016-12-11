package one.trifle.lurry.reader

import groovy.transform.CompileStatic
import one.trifle.lurry.exception.LurryPermissionException
import org.junit.Test

import static org.junit.Assert.fail

@CompileStatic
class SmbReaderTest {
    @Test
    void readNothing() {
        new SmbReader("login", "password").each { fail() }
    }

    @Test(expected = LurryPermissionException)
    void emptyElement() {
        new SmbReader("login", "password", [null] as String[]).iterator()
    }

    @Test(expected = LurryPermissionException)
    void permissionElement() {
        new SmbReader("login", "password", ["test"] as String[]).iterator()
    }
}
