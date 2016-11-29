package com.github.madlexa.lurry.reader;

import com.github.madlexa.lurry.exception.LurryPermissionException;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import org.codehaus.groovy.util.ArrayIterator;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Iterator;

/**
 * TODO
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
