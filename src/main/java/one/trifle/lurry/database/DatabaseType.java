/*
 * Copyright 2016 Aleksey Dobrynin
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
package one.trifle.lurry.database;

import java.util.Arrays;

/**
 * An enum for resolve database driver name to normal usage name
 * this enum also contains class with specific methods for database
 *
 * @author Aleksey Dobrynin
 */
public enum DatabaseType {
    MYSQL("com.mysql.jdbc.Driver", "MySQL", MySqlSafeString.class),
    ORACLE("oracle.jdbc.OracleDriver", "Oracle", DefaultSafeString.class),
    POSTGRE("org.postgresql.Driver", "PostgreSQL", DefaultSafeString.class),
    H2("org.h2.Driver", "H2", DefaultSafeString.class),
    DB2("com.ibm.db2.jcc.DB2Driver", "Db2", DefaultSafeString.class),
    MSSQL("com.microsoft.sqlserver.jdbc.SQLServerDriver", "SQLServer", DefaultSafeString.class),
    SQLITE("org.sqlite.JDBC", "SQLite", DefaultSafeString.class),
    CASSANDRA("org.apache.cassandra.cql.jdbc.CassandraDriver", "Cassandra", DefaultSafeString.class),
    DEFAULT("", "", DefaultSafeString.class);

    private final String driverClass;
    private final String databaseName;
    private final Class mixed;

    DatabaseType(String driverClass, String databaseName, Class mixed) {
        this.driverClass = driverClass;
        this.databaseName = databaseName;
        this.mixed = mixed;
    }

    public static DatabaseType of(String databaseName) {
        return Arrays.stream(values())
                .filter(type -> type.databaseName.equalsIgnoreCase(databaseName))
                .findFirst()
                .orElse(DEFAULT);
    }

    public Class getMixed() {
        return mixed;
    }
}
