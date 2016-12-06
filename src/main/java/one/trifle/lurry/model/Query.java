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

import java.util.Objects;

/**
 * Larry query information format
 *
 * @author Aleksey Dobrynin
 */
public class Query {
    /**
     * query name
     */
    private final String name;
    /**
     * sql template
     */
    private String sql;

    public Query(String name) {
        this.name = name;
    }

    public Query(String name, String sql) {
        this.name = name;
        this.sql = sql;
    }

    public String getName() {
        return name;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
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
        if (obj instanceof Query) {
            return Objects.equals(name, ((Query) obj).getName());
        }
        return false;
    }
}
