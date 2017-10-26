package one.trifle.lurry

import java.sql.SQLException

class LurrySqlException(message: String, th: Throwable? = null): SQLException(message, th)