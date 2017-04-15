/*
 * Copyright 2017 Aleksey Dobrynin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package one.trifle.lurry.mapper.version_1

import org.junit.Test

/**
 * @author Aleksey Dobrynin
 */
class LurryMapperCompilerTest {
    @Test
    void "simple"() {
        String source = """
main: personID
contact: personID
"""

        assert new LurryMapperCompiler(source, null, null).parse().unique == [
                main   : ['personID'],
                contact: ["personID"]
        ]
    }

    @Test
    void "array"() {
        String source = """
main: personID
contact: personID contactID
"""

        assert new LurryMapperCompiler(source, null, null).parse().unique == [
                main   : ['personID'],
                contact: ["personID", "contactID"]
        ]
    }

    @Test
    void "quote"() {
        String source = """
main: "person id"
"contact name": "data:1" "data:2"
"""

        assert new LurryMapperCompiler(source, null, null).parse().unique == [
                main          : ['person id'],
                "contact name": ['data:1', 'data:2']
        ]
    }

    @Test
    void "without spaces"() {
        String source = 'main:personID "contact name":"data:1" "data:2"'

        assert new LurryMapperCompiler(source, null, null).parse().unique == [
                main          : ['personID'],
                "contact name": ['data:1', 'data:2']
        ]
    }


    @Test
    void "many spaces"() {
        String source = """

  main : "person id"
contact: personID       test.contactID
"my data:": dataID 

personID1 
    1 
    contactID

"""

        assert new LurryMapperCompiler(source, null, null).parse().unique == [
                main      : ['person id'],
                contact   : ["personID", "test.contactID"],
                "my data:": ["dataID", "personID1", "1", "contactID"]
        ]
    }
}
