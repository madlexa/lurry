package com.github.madlexa.lurry.parser;

/**
 * TODO
 */
public enum ParserType {
    JSON(JsonParser.class),
    XML(XmlParser.class),
    YAML(YamlParser.class);

    private final Class<? extends Parser> parser;

    ParserType(Class<? extends Parser> parser) {
        this.parser = parser;
    }

    public Class<? extends Parser> get() {
        return parser;
    }
}
