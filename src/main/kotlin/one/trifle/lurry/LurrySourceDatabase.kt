package one.trifle.lurry

import one.trifle.lurry.connection.DatabaseType
import one.trifle.lurry.connection.LurrySource
import one.trifle.lurry.connection.map
import one.trifle.lurry.connection.use
import org.slf4j.LoggerFactory
import javax.sql.DataSource

class LurrySourceDatabase(private val source: DataSource) : LurrySource {
    private val LOGGER = LoggerFactory.getLogger(LurrySourceDatabase::class.java)

    override val type: DatabaseType = source.connection.use { conn ->
        val metaData = conn.metaData
        if (metaData == null) {
            LOGGER.error("metaData empty")
            throw LurrySqlException("metaData empty")
        }
        return@use DatabaseType.of(metaData.databaseProductName)
    } ?: DatabaseType.DEFAULT

    override fun execute(query: LQuery, params: Map<String, Any>) = source.connection.use { conn ->
        return@use conn.createStatement().use { stmt ->
            return@use stmt.executeQuery(query.sql(params, type.mixed)).map { columns, result ->
                return@map DatabaseRow(columns.map { field ->  field to result.getObject(field) }.toMap())
            }
        }
    } ?: emptyList()

    data class DatabaseRow(private val data: Map<String, Any>) : LurrySource.Row {
        override fun toMap(): Map<String, Any> = data
    }
}
