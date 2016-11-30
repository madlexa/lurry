package one.trifle.lurry

import one.trifle.lurry.model.Query
import spock.lang.Specification
import spock.lang.Unroll

class QueryProcessorTest extends Specification {

    @Unroll
    def "template(#sql, #params) == #result"() {
        expect:
        new QueryProcessor().exec(new Query(name: "test", sql: template), params as Map<String, Object>) == result

        where:
        template                                        | params                          || result
        'bla ${test} bla'                               | [test: "1"]                     || "bla 1 bla"
        'bla $test bla'                                 | [test: 1, t1: 0]                || "bla 1 bla"
        'bla ${test == "1" ? res[0] : res[1]} bla'      | [test: "1", res: ["yes", "no"]] || "bla yes bla"
        'bla ${test == 1 ? "yes" : "no"} bla'           | [test: 2]                       || "bla no bla"
        'bla ${t1} ${t2} bla'                           | [t1: 1, t2: 2]                  || "bla 1 2 bla"
        'bla <% out << t1 %> ${t2} bla'                 | [t1: 1, t2: 2]                  || "bla 1 2 bla"
        'ID IN (<% out << (ids.join(",")) %>)'          | [ids: [1, 2, 3]]                || "ID IN (1,2,3)"
        'ID IN (${ids.collect{"\'$it\'"}.join(\',\')})' | [ids: ["1", "2", "3"]]          || "ID IN ('1','2','3')"
        'a = \'$a\' AND b = \'$b\''                     | [a: "a"]                        || "a = 'a' AND b = 'null'"
    }
}
