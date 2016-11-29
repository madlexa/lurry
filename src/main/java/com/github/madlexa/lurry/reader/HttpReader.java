package com.github.madlexa.lurry.reader;

import com.github.madlexa.lurry.exception.LurryPermissionException;
import org.codehaus.groovy.util.ArrayIterator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

/**
 * TODO
 */
public class HttpReader implements Reader {
    private final InputStream[] streams;

    public HttpReader(URL... urls) {
        int size = urls.length;
        streams = new InputStream[size];
        for (int i = 0; i < size; i++) {
            try {
                streams[i] = urls[i].openStream();
            } catch (IOException exc) {
                throw new LurryPermissionException("url not read [" + urls[i].getFile() + "]", exc);
            }
        }
    }

    @Override
    public Iterator<InputStream> iterator() {
        return new ArrayIterator<>(streams);
    }
}
