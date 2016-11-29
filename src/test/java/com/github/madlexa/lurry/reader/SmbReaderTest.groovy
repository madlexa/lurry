package com.github.madlexa.lurry.reader

import com.github.madlexa.lurry.exception.LurryPermissionException
import org.junit.Test

import static org.junit.Assert.fail

class SmbReaderTest {
    @Test
    void readNothing() {
        new SmbReader("login", "password").each { fail() }
    }

    @Test(expected = LurryPermissionException)
    void emptyElement() {
        new SmbReader("login", "password", [null] as String[])
    }

    @Test(expected = LurryPermissionException)
    void permissionElement() {
        new SmbReader("login", "password", ["test"] as String[])
    }
}
