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
package one.trifle.lurry.database

import java.util.*

/**
 * An enum for resolve database driver name to normal usage name
 * this enum also contains class with specific methods for database
 *
 * @author Aleksey Dobrynin
 */
enum class DatabaseType constructor(private val driverClass: String, private val databaseName: String, val mixed: Class<*>) {
    MYSQL("com.mysql.jdbc.Driver", "MySQL", MySqlSafeString::class.java),
    ORACLE("oracle.jdbc.OracleDriver", "Oracle", DefaultSafeString::class.java),
    POSTGRE("org.postgresql.Driver", "PostgreSQL", DefaultSafeString::class.java),
    H2("org.h2.Driver", "H2", DefaultSafeString::class.java),
    DB2("com.ibm.db2.jcc.DB2Driver", "Db2", DefaultSafeString::class.java),
    MSSQL("com.microsoft.sqlserver.jdbc.SQLServerDriver", "SQLServer", DefaultSafeString::class.java),
    SQLITE("org.sqlite.JDBC", "SQLite", DefaultSafeString::class.java),
    CASSANDRA("org.apache.cassandra.cql.jdbc.CassandraDriver", "Cassandra", DefaultSafeString::class.java),
    DEFAULT("", "", DefaultSafeString::class.java);

    companion object {
        @JvmStatic fun of(name: String): DatabaseType {
            return Arrays.stream(values())
                    .filter { type -> type.databaseName.equals(name, ignoreCase = true) }
                    .findFirst()
                    .orElse(DEFAULT)
        }
    }
}
