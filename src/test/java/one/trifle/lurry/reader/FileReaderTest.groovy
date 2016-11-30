package one.trifle.lurry.reader

import groovy.transform.CompileStatic
import one.trifle.lurry.exception.LurryPermissionException
import org.junit.Test

import static org.junit.Assert.fail

@CompileStatic
class FileReaderTest {
    @Test
    void readNothing() {
        new FileReader().each { fail() }
    }

    @Test(expected = NullPointerException)
    void emptyElement() {
        new FileReader([null] as File[])
    }

    @Test(expected = LurryPermissionException)
    void permissionElement() {
        new FileReader([new File("")] as File[])
    }
}
