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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

/**
 * implementation {@code Reader} is used to read from URLs
 *
 * @author Aleksey Dobrynin
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
