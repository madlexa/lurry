package com.github.madlexa.lurry

import com.github.madlexa.lurry.parser.Parser
import com.github.madlexa.lurry.reader.Reader
import org.junit.Test

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class QueryFactoryTest {
    @Test
    void init() {
        Reader reader = mock(Reader.class)
        Parser parser = mock(Parser.class)

        when(reader.iterator()).thenReturn([mock(InputStream)].iterator())

        QueryFactory factory = new QueryFactory(reader, parser)
        factory.get("", "", [:])
    }
}
