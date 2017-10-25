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
package one.trifle.lurry

import groovy.lang.Closure
import groovy.text.GStringTemplateEngine
import groovy.text.Template
import org.codehaus.groovy.runtime.DefaultGroovyMethods
import java.util.*

/**
 * Processor for transform Lurry-Template to Sql String
 *
 * @param string GString sql template
 * @param source connection source with database type
 *
 * @author Aleksey Dobrynin
 */
data class LQuery(private val string: String) {
    private val template: Template = GStringTemplateEngine().createTemplate(string)

    /**
     * generate sql from template
     *
     * @param params placeholders
     *
     * @return correct sql string
     */
    fun sql(params: Map<String, Any>, mixed: Class<*>): String = DefaultGroovyMethods.use(LQuery::class.java, mixed,
            object : Closure<String>(this, this) {
                fun doCall(): String {
                    return template.make(params.convert())
                            .toString()
                }
            })

    private fun Map<String, Any>.convert() = object : HashMap<String, Any>() {
        override fun containsKey(key: String) = true
    }.apply { putAll(this@convert) }
}