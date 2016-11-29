package com.github.madlexa.lurry.parser;

import com.github.madlexa.lurry.model.Entity;

import java.io.InputStream;
import java.util.List;

/**
 * TODO
 */
public interface Parser {
    List<Entity> parse(InputStream source);
}
