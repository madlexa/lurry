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
import java.util.Arrays;
import java.util.Iterator;

/**
 * implementation {@code Reader} is used to read from Files
 *
 * @author Aleksey Dobrynin
 */
public class FileReader implements Reader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileReader.class);

    private final File[] files;

    public FileReader(File... files) {
        this.files = files;
    }

    @Override
    public Iterator<InputStream> iterator() {
        return new ArrayIterator<>(
                Arrays.stream(files)
                        .map(this::toInputStream)
                        .toArray(InputStream[]::new)
        );
    }

    private InputStream toInputStream(File file) {
        try {
            LOGGER.debug("start read file [{}]", file.getName());
            return new FileInputStream(file);
        } catch (FileNotFoundException exc) {
            LOGGER.error("file not found [{}]", file.getName(), exc);
            throw new LurryPermissionException("file not found [" + file.getName() + "]", exc);
        }
    }
}
