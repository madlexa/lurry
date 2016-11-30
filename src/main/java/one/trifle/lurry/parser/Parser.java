package one.trifle.lurry.parser;

import one.trifle.lurry.model.Entity;

import java.io.InputStream;
import java.util.List;

/**
 * TODO
 */
public interface Parser {
    List<Entity> parse(InputStream source);
}
