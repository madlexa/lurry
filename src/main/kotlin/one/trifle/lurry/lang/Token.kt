package one.trifle.lurry.lang

data class Token(val type: TokenType, val value: TokenValue, val line: Int, val position: Int) {
    companion object {
        val EOF = Token(TokenType.EOF, TokenValue.EMPTY, 0, 0)
    }

    override fun toString(): String {
        return type.toString() + if (!value.isEmpty()) " $value" else ""
    }
}
data class TokenValue(val value: Any?) {
    companion object{
        val EMPTY = TokenValue(null)
    }

    fun isEmpty() = value == null

    override fun toString(): String {
        return "$value"
    }
}
