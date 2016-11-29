package com.github.madlexa.lurry.parser;

import com.github.madlexa.lurry.exception.LurryParseFormatException;
import com.github.madlexa.lurry.exception.LurryPermissionException;
import com.github.madlexa.lurry.model.Entity;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * TODO
 */
public class JsonParser implements Parser {

    public JsonParser() {
    }


    @Override
    public List<Entity> parse(InputStream source) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        try {
            while ((length = source.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return new Mapper(result.toString("UTF-8")).map();
        } catch (IOException exc) {
            throw new LurryPermissionException("json parse error", exc);
        } catch (JsonSyntaxException exc) {
            throw new LurryParseFormatException("json parse error", exc);
        }
    }

    private final class Mapper {
        private final String json;

        Mapper(String json) {
            this.json = json;
        }

        List<Entity> map() throws IOException {
            return Arrays.asList(new Gson().fromJson(json, Entity[].class));
        }
    }
}
