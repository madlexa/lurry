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
package one.trifle.lurry;

import java.util.Arrays;

/**
 * specific methods for database type
 *
 * @author Aleksey Dobrynin
 */
public enum DatabaseType {
    MYSQL("com.mysql.jdbc.Driver") {
        @Override
        public String escape(String str) {
            if (str == null) {
                return null;
            }
            StringBuilder to = new StringBuilder();
            for (char symbol : str.toCharArray()) {
                if (symbol == '\'' || symbol == '\\') {
                    to.append(symbol);
                }
                to.append(symbol);
            }
            return to.toString();
        }
    },
    ORACLE("oracle.jdbc.OracleDriver"),
    POSTGRE("org.postgresql.Driver"),
    H2("org.h2.Driver"),
    DB2("com.ibm.db2.jcc.DB2Driver"),
    MSSQL("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    SQLITE("org.sqlite.JDBC"),
    CASSANDRA("org.apache.cassandra.cql.jdbc.CassandraDriver"),
    DEFAULT("");

    private final String driverName;

    DatabaseType(String driverName) {
        this.driverName = driverName;
    }

    public static DatabaseType of(String driverName) {
        return Arrays.stream(values())
                .filter(type -> type.driverName.equals(driverName))
                .findFirst()
                .orElse(DEFAULT);
    }

    /**
     * escape special characters to remove sql injection
     *
     * @param str sql string
     * @return escaping string
     */
    public String escape(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder to = new StringBuilder();
        for (char symbol : str.toCharArray()) {
            if (symbol == '\'') {
                to.append(symbol);
            }
            to.append(symbol);
        }
        return to.toString();
    }
}
