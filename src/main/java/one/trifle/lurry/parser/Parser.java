/*
 * Copyright 2017 Aleksey Dobrynin
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
package one.trifle.lurry.parser;

import one.trifle.lurry.GQueryTemplate;
import one.trifle.lurry.model.Entity;
import one.trifle.lurry.reader.Reader;

import java.io.InputStream;
import java.util.List;

/**
 * An interface used by {@link GQueryTemplate} is used to create universal lurry query format
 *
 * @author Aleksey Dobrynin
 * @see GQueryTemplate
 * @see Reader
 */
public interface Parser {
    /**
     * A function that parse data from InputStream to specific pojo
     *
     * @param source is a result of reader working
     * @return lurry query format
     */
    List<Entity> parse(InputStream source);
}
