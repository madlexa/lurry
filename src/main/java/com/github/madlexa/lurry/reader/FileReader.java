package com.github.madlexa.lurry.reader;

import com.github.madlexa.lurry.exception.LurryPermissionException;
import org.codehaus.groovy.util.ArrayIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * TODO
 */
public class FileReader implements Reader {
    private final InputStream[] streams;

    public FileReader(File... files) {
        int size = files.length;
        streams = new InputStream[size];
        for (int i = 0; i < size; i++) {
            try {
                streams[i] = new FileInputStream(files[i]);
            } catch (FileNotFoundException exc) {
                throw new LurryPermissionException("file not found [" + files[i].getName() + "]", exc);
            }
        }
    }

    @Override
    public Iterator<InputStream> iterator() {
        return new ArrayIterator<>(streams);
    }
}
