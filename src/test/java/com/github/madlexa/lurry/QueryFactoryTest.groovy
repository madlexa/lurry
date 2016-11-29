package com.github.madlexa.lurry

import com.github.madlexa.lurry.parser.Parser
import com.github.madlexa.lurry.reader.Reader
import org.junit.Test
import org.mockito.Mockito

class QueryFactoryTest {
    @Test
    void init() {
        Reader reader = Mockito.mock(Reader.class)
        Parser parser = Mockito.mock(Parser.class)

        QueryFactory factory = new QueryFactory(reader, parser)
    }
}
