package one.trifle.lurry;

import one.trifle.lurry.model.Entity;
import one.trifle.lurry.model.Query;
import one.trifle.lurry.parser.Parser;
import one.trifle.lurry.reader.Reader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 */
public class QueryFactory {

    private final Reader reader;
    private final Parser parser;
    private Map<String, Query> cache = null;
    private QueryProcessor processor = new QueryProcessor();

    public QueryFactory(Reader reader, Parser parser) {
        this.reader = reader;
        this.parser = parser;
    }

    private void init() {
        synchronized (this) {
            if (cache == null) {
                cache = new HashMap<>();
                for (InputStream source : reader) {
                    for (Entity entity : parser.parse(source)) {
                        for (Query query : entity.getQueries()) {
                            cache.put(getCacheKey(entity.getName(), query.getName()), query);
                        }
                    }
                }
            }
        }
    }

    public String get(String entity, String query, Map<String, Object> params) {
        if (cache == null) {
            init();
        }
        Query data = cache.get(getCacheKey(entity, query));
        if (data == null) {
            return "";
        }
        return processor.exec(data, params);
    }

    private String getCacheKey(String entity, String query) {
        return entity + "_" + query;
    }
}
