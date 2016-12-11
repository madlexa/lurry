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
        driver       || type
        "MySQL"      || DatabaseType.MYSQL
        "Oracle"     || DatabaseType.ORACLE
        "PostgreSQL" || DatabaseType.POSTGRE
        "H2"         || DatabaseType.H2
        "Db2"        || DatabaseType.DB2
        "SQLServer"  || DatabaseType.MSSQL
        "SQLite"     || DatabaseType.SQLITE
        "Cassandra"  || DatabaseType.CASSANDRA
        ""           || DatabaseType.DEFAULT
        "test"       || DatabaseType.DEFAULT

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