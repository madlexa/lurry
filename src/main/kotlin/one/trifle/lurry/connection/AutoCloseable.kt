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
package one.trifle.lurry.connection

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

inline fun <R> Connection.use(block: (Connection) -> R): R {
    try {
        return block(this)
    } finally {
        this.close()
    }
}

inline fun <R> Statement.use(block: (Statement) -> R): R {
    try {
        return block(this)
    } finally {
        this.close()
    }
}

inline fun <R> ResultSet.use(block: (ResultSet) -> R): R {
    try {
        return block(this)
    } finally {
        this.close()
    }
}
