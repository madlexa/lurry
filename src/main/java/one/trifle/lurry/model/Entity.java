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
package one.trifle.lurry.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Larry format for aggregate queries by entity
 *
 * @author Aleksey Dobrynin
 */
public class Entity {
    /**
     * entity name
     */
    private final String name;
    /**
     * all queries for this entity
     */
    private Query[] queries;

    public Entity(String name) {
        this.name = name;
    }

    public Entity(String name, Query[] queries) {
        this.name = name;
        this.queries = queries;
    }

    public String getName() {
        return name;
    }

    public Query[] getQueries() {
        return queries;
    }

    public void setQueries(Query[] queries) {
        this.queries = queries;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Entity) {
            if (Objects.equals(name, ((Entity) obj).getName())) {
                if ((queries == null || queries.length == 0) &&
                        (((Entity) obj).getQueries() == null || ((Entity) obj).getQueries().length == 0)) {
                    return true;
                }
                return Arrays.equals(queries, ((Entity) obj).getQueries());
            }
        }
        return false;
    }
}
