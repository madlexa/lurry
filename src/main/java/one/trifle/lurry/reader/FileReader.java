/*
 * Copyright 2016 Aleksey Dobrynin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package one.trifle.lurry.reader;

import one.trifle.lurry.exception.LurryPermissionException;
import org.codehaus.groovy.util.ArrayIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * implementation {@code Reader} is used to read from Files
 *
 * @author Aleksey Dobrynin
 */
public class FileReader implements Reader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileReader.class);

    private final InputStream[] streams;

    public FileReader(File... files) {
        LOGGER.debug("start read '{}' files", files.length);
        int size = files.length;
        streams = new InputStream[size];
        for (int i = 0; i < size; i++) {
            try {
                LOGGER.debug("start read file [{}]", files[i].getName());
                streams[i] = new FileInputStream(files[i]);
            } catch (FileNotFoundException exc) {
                LOGGER.error("file not found [{}]", files[i].getName(), exc);
                throw new LurryPermissionException("file not found [" + files[i].getName() + "]", exc);
            }
        }
    }

    @Override
    public Iterator<InputStream> iterator() {
        return new ArrayIterator<>(streams);
    }
}
