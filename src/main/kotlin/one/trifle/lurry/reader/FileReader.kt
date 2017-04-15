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
package one.trifle.lurry.reader

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

/**
 * implementation `Reader` is used to read from Files

 * @author Aleksey Dobrynin
 */
class FileReader(vararg val files: File) : Reader {
    override fun iterator(): MutableIterator<InputStream> = files.map(::FileInputStream).iterator() as MutableIterator<InputStream>
}
