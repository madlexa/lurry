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

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Types

class LMapper(private val body: (Map<String, Any?>) -> Any?) {
    fun call(arg: ResultSet): Any? {
        val row: MutableMap<String, Any?> = HashMap()
        val metadata: ResultSetMetaData = arg.metaData
        (1..metadata.columnCount).forEach { index ->
            val name: String = metadata.getColumnName(index)
            val value: Any? = when (metadata.getColumnType(index)) {
                Types.CHAR, Types.VARCHAR, Types.LONGVARCHAR -> arg.getString(index)
                Types.NUMERIC, Types.DECIMAL -> arg.getBigDecimal(index)
                Types.BIT -> arg.getBoolean(index)
                Types.TINYINT -> arg.getByte(index)
                Types.SMALLINT -> arg.getShort(index)
                Types.BIGINT -> arg.getLong(index)
                Types.REAL, Types.FLOAT -> arg.getFloat(index)
                Types.DOUBLE -> arg.getDouble(index)
                Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> arg.getBytes(index)
                Types.DATE -> arg.getDate(index)
                Types.TIME -> arg.getTime(index)
                Types.TIMESTAMP -> arg.getTimestamp(index)
                Types.ARRAY -> arg.getArray(index)
                Types.BLOB -> arg.getBlob(index)
                Types.NULL -> null
                else -> arg.getString(index)
            }
            row["#${name}"] = value
        }
        return body(row)
    }
}