package one.trifle.lurry

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Aleksey Dobrynin
 */
class DatabaseTypeTest extends Specification {
    @Unroll
    "MYSQL escape(#str) == #escape"() {
        expect:
        ((DatabaseType) type).escape(str) == escape

        where:
        type               | str          || escape
        DatabaseType.MYSQL | "test"       || "test"
        DatabaseType.MYSQL | null         || null
        DatabaseType.MYSQL | "\"'test'\"" || "\"''test''\""
        DatabaseType.MYSQL | "\\test\\"   || "\\\\test\\\\"
    }

    @Unroll
    "escape(#str) == #escape"() {
        expect:
        DatabaseType.values().findAll { it != DatabaseType.MYSQL }.each {
            assert it.escape(str) == escape
        }

        where:
        str          || escape
        "test"       || "test"
        null         || null
        "\"'test'\"" || "\"''test''\""
        "\\test\\"   || "\\test\\"
    }

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

}