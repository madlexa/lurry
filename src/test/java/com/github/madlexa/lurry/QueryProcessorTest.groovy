package com.github.madlexa.lurry

import com.github.madlexa.lurry.model.Query
import org.junit.Test

class QueryProcessorTest {
    @Test
    void init() {
        assert new QueryProcessor().exec(new Query(name: "test", sql: 'bla $test bla'), [test: 1, t1: 0]) == "bla 1 bla"
//        assert new QueryProcessor().exec(new Query(name: "test", sql: 'bla ${test} bla'), [test1: 1]) == "bla  bla"
        assert new QueryProcessor().exec(new Query(name: "test", sql: 'bla ${test == 1 ? "yes" : "no"} bla'), [test: 1]) == "bla yes bla"
        assert new QueryProcessor().exec(new Query(name: "test", sql: 'bla ${test == 1 ? "yes" : "no"} bla'), [test: 2]) == "bla no bla"
        assert new QueryProcessor().exec(new Query(name: "test", sql: 'bla ${t1} ${t2} bla'), [t1: 1, t2: 2]) == "bla 1 2 bla"
        assert new QueryProcessor().exec(new Query(name: "test", sql: 'bla <% out << t1 %> ${t2} bla'), [t1: 1, t2: 2]) == "bla 1 2 bla"
    }
}
