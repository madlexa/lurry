package com.github.madlexa.lurry.reader;

/**
 * TODO
 */
public enum ReaderType {
    FILE(FileReader.class),
    HTTP(HttpReader.class),
    SMB(SmbReader.class);

    private final Class<? extends Reader> reader;

    ReaderType(Class<? extends Reader> reader) {
        this.reader = reader;
    }

    public Class<? extends Reader> get() {
        return reader;
    }
}
