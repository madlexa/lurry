/*
 * Copyright 2020 Aleksey Dobrynin
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

package one.trifle.lurry.lang.interpreter

import java.lang.reflect.Executable
import java.lang.reflect.Modifier

class LClass(private val clazz: Class<*>, private val obj: Any? = null) {
    fun invoke(name: String, args: List<Any?>, exc: Exception): Any? {
        val method = findExecutable(clazz.declaredMethods, args) { method -> method.name == name } ?: throw exc
        if (!method.isAccessible && Modifier.isPublic(method.modifiers)) method.isAccessible = true
        return method.invoke(obj, *args.toTypedArray())
    }

    fun newInstance(args: List<Any?>, exc: Exception): Any = findExecutable(clazz.constructors, args)
            ?.newInstance(*args.toTypedArray()) ?: throw exc

    private fun <T : Executable> findExecutable(executables: Array<T>, arguments: List<Any?>, condition: (T) -> Boolean = { true }): T? {
        val getPrimitive: (Class<*>) -> Class<*> = { clazz: Class<*> ->
            if (clazz.isPrimitive) {
                clazz
            } else {
                val field = clazz.declaredFields.find { field -> field.name == "TYPE" }
                if (field == null || !Modifier.isPublic(field.modifiers) || !Modifier.isStatic(field.modifiers)) {
                    clazz
                } else {
                    field.get(null).let { c ->
                        if (c is Class<*>) {
                            c
                        } else {
                            clazz
                        }
                    }
                }
            }
        }
        val isAssignableFrom: (Class<*>, Class<*>) -> Boolean = { c1: Class<*>, c2: Class<*> ->
            var class1 = c1
            var class2 = c2
            if (class1.isPrimitive || class2.isPrimitive) {
                class1 = getPrimitive(c1)
                class2 = getPrimitive(c2)
            }
            class1 == class2 || class1.isAssignableFrom(class2)
        }

        return executables.filter(condition).find { exec ->
            if (exec.parameters.size != arguments.size) return@find false
            arguments.asSequence().map { arg -> arg?.javaClass }
                    .forEachIndexed { index, arg ->
                        if (arg != null && !isAssignableFrom(exec.parameters[index].type, arg)) {
                            return@find false
                        }
                    }
            return@find true
        }
    }
}