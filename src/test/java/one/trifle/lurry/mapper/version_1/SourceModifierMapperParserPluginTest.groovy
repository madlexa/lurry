package one.trifle.lurry.mapper.version_1

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * @author Aleksey Dobrynin
 */
@CompileStatic
class SourceModifierMapperParserPluginTest {
    @Test
    void "simple"() {
        String source = """
main: new Person(id: row.ID, contacts: [])
contact: #main.contacts << new Contact(id: row.contactID)
type: #contact.type = new Type(id: row.typeID)
"""

        assert new SourceModifierMapperParserPlugin().replace(new StringReader(source)) == """
main: new Person(id: row.ID, contacts: [])
contact: data.main.contacts << new Contact(id: row.contactID)
type: data.contact.type = new Type(id: row.typeID)
"""
    }

    @Test
    void "in string one"() {
        String source = """
main: new Person(id: row.ID, contacts: [])
contact: #main.contacts << new Contact(id: row.contactID, value: 'issue #1')
"""

        assert new SourceModifierMapperParserPlugin().replace(new StringReader(source)) == """
main: new Person(id: row.ID, contacts: [])
contact: data.main.contacts << new Contact(id: row.contactID, value: 'issue #1')
"""
    }

    @Test
    void "in string double"() {
        String source = """
main: new Person(id: row.ID, contacts: [])
contact: #main.contacts << new Contact(id: row.contactID, value: "issue #1")
"""

        assert new SourceModifierMapperParserPlugin().replace(new StringReader(source)) == """
main: new Person(id: row.ID, contacts: [])
contact: data.main.contacts << new Contact(id: row.contactID, value: "issue #1")
"""
    }

    @Test
    void "string in string"() {
        String source = """
main: new Person(id: row.ID, contacts: [])
contact: #main.contacts << new Contact(id: row.contactID, value1: "issue '#1", value2: 'issue "#1')
"""

        assert new SourceModifierMapperParserPlugin().replace(new StringReader(source)) == """
main: new Person(id: row.ID, contacts: [])
contact: data.main.contacts << new Contact(id: row.contactID, value1: "issue '#1", value2: 'issue "#1')
"""
    }

    @Test
    void "string escape"() {
        String source = """
main: new Person(id: row.ID, contacts: [])
contact: #main.contacts << new Contact(id: row.contactID, value1: "issue \\"#1", value2: 'issue \\'#1')
"""
        assert new SourceModifierMapperParserPlugin().replace(new StringReader(source)) == """
main: new Person(id: row.ID, contacts: [])
contact: data.main.contacts << new Contact(id: row.contactID, value1: "issue \\"#1", value2: 'issue \\'#1')
"""
    }
}
