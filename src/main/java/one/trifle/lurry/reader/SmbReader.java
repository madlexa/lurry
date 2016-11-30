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

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import one.trifle.lurry.exception.LurryPermissionException;
import org.codehaus.groovy.util.ArrayIterator;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Iterator;

/**
 * implementation {@code Reader} is used to read from samba
 *
 * @author Aleksey Dobrynin
 */
public class SmbReader implements Reader {
    private final InputStream[] streams;

    public SmbReader(String login, String password, String... paths) {
        int size = paths.length;
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication("", login, password);
        streams = new InputStream[size];
        for (int i = 0; i < size; i++) {
            try {
                SmbFile file = new SmbFile(paths[i], auth);
                streams[i] = new SmbFileInputStream(file);
            } catch (MalformedURLException | SmbException | UnknownHostException exc) {
                throw new LurryPermissionException("smb exception [" + paths[i] + "]", exc);
            }
        }
    }

    @Override
    public Iterator<InputStream> iterator() {
        return new ArrayIterator<>(streams);
    }
}
