package one.trifle.lurry.database

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Aleksey Dobrynin
 */
class DatabaseTypeTest extends Specification {

    @Unroll
    "DatabaseType.of(#driver) == #type"() {
        expect:
        DatabaseType.of(driver) == type

        where:
        driver                                          || type
        "com.mysql.jdbc.Driver"                         || DatabaseType.MYSQL
        "oracle.jdbc.OracleDriver"                      || DatabaseType.ORACLE
        "org.postgresql.Driver"                         || DatabaseType.POSTGRE
        "org.h2.Driver"                                 || DatabaseType.H2
        "com.ibm.db2.jcc.DB2Driver"                     || DatabaseType.DB2
        "com.microsoft.sqlserver.jdbc.SQLServerDriver"  || DatabaseType.MSSQL
        "org.sqlite.JDBC"                               || DatabaseType.SQLITE
        "org.apache.cassandra.cql.jdbc.CassandraDriver" || DatabaseType.CASSANDRA
        ""                                              || DatabaseType.DEFAULT
        "test"                                          || DatabaseType.DEFAULT
    }

    @Unroll
    "#type.mixed == #clazz"() {
        expect:
        type.mixed == clazz

        where:
        type                   || clazz
        DatabaseType.MYSQL     || MySqlSafeString
        DatabaseType.ORACLE    || DefaultSafeString
        DatabaseType.POSTGRE   || DefaultSafeString
        DatabaseType.H2        || DefaultSafeString
        DatabaseType.DB2       || DefaultSafeString
        DatabaseType.MSSQL     || DefaultSafeString
        DatabaseType.SQLITE    || DefaultSafeString
        DatabaseType.CASSANDRA || DefaultSafeString
        DatabaseType.DEFAULT   || DefaultSafeString
    }

}