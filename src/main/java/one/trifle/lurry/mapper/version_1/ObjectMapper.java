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
package one.trifle.lurry.mapper.version_1;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author Aleksey Dobrynin
 */
public interface ObjectMapper<T> {
    /**
     * Call all rows for mapping and group results
     *
     * @param rs     SQL result set
     * @param rowNum row number
     */
    void mapRow(ResultSet rs, int rowNum);

    /**
     * Get final result
     *
     * @return list main labels
     */
    List<T> result();
}
