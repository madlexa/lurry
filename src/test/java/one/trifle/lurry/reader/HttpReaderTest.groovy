package one.trifle.lurry.reader

import one.trifle.lurry.exception.LurryPermissionException
import org.junit.Test

import static org.junit.Assert.fail

class HttpReaderTest {
    @Test
    void readNothing() {
        new HttpReader().each { fail() }
    }

    @Test(expected = NullPointerException)
    void emptyElement() {
        new HttpReader([null] as URL[])
    }

    @Test(expected = LurryPermissionException)
    void permissionElement() {
        new HttpReader([new URL("http://localhost:1")] as URL[])
    }
}
