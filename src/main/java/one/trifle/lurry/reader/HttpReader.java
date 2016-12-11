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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

/**
 * implementation {@code Reader} is used to read from URLs
 *
 * @author Aleksey Dobrynin
 */
public class HttpReader implements Reader {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpReader.class);

    private final URL[] urls;

    public HttpReader(URL... urls) {
        this.urls = urls;
    }

    @Override
    public Iterator<InputStream> iterator() {
        return new ArrayIterator<>(
                Arrays.stream(urls)
                        .map(this::toInputStream)
                        .toArray(InputStream[]::new)
        );
    }

    private InputStream toInputStream(URL url) {
        try {
            LOGGER.debug("start read url [{}]", url.getFile());
            return url.openStream();
        } catch (IOException exc) {
            LOGGER.error("url not read [{}]", url.getFile(), exc);
            throw new LurryPermissionException("url not read [" + url.getFile() + "]", exc);
        }
    }
}
