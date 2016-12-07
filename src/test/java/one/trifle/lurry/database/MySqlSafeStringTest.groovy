package one.trifle.lurry.database

import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Aleksey Dobrynin
 */
class MySqlSafeStringTest extends Specification {

    @Unroll
    "MySqlSafeString.escape(#str) == #result"() {
        expect:
        MySqlSafeString.escape(str) == result

        where:
        str      || result
        ""       || "''"
        null     || null
        "test"   || "'test'"
        "'test'" || "'''test'''"
        "it's"   || "'it''s'"
        "it\\'s" || "'it\\\\''s'"
    }

    @Unroll
    "MySqlSafeString.join(#arr) == #result"() {
        expect:
        MySqlSafeString.join(arr) == result

        where:
        arr                                              || result
        [""] as String[]                                 || "''"
        [null] as String[]                               || ""
        ["test"] as String[]                             || "'test'"
        ["'test'"] as String[]                           || "'''test'''"
        ["it's"] as String[]                             || "'it''s'"
        ["it\\'s"] as String[]                           || "'it\\\\''s'"
        ["test", null, "", "it's", "it\\'s"] as String[] || "'test','','it''s','it\\\\''s'"
        [1] as Number[]                                  || "1"
        [1, 2] as Number[]                               || "1,2"
        [1, null, 2] as Number[]                         || "1,2"
    }
}
