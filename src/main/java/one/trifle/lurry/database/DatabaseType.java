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
    MYSQL("com.mysql.jdbc.Driver", MySqlSafeString.class),
    ORACLE("oracle.jdbc.OracleDriver", DefaultSafeString.class),
    POSTGRE("org.postgresql.Driver", DefaultSafeString.class),
    H2("org.h2.Driver", DefaultSafeString.class),
    DB2("com.ibm.db2.jcc.DB2Driver", DefaultSafeString.class),
    MSSQL("com.microsoft.sqlserver.jdbc.SQLServerDriver", DefaultSafeString.class),
    SQLITE("org.sqlite.JDBC", DefaultSafeString.class),
    CASSANDRA("org.apache.cassandra.cql.jdbc.CassandraDriver", DefaultSafeString.class),
    DEFAULT("", DefaultSafeString.class);

    private final String driverName;
    private final Class mixed;

    DatabaseType(String driverName, Class mixed) {
        this.driverName = driverName;
        this.mixed = mixed;
    }

    public static DatabaseType of(String driverName) {
        return Arrays.stream(values())
                .filter(type -> type.driverName.equals(driverName))
                .findFirst()
                .orElse(DEFAULT);
    }

    public Class getMixed() {
        return mixed;
    }
}
