/**
 * Copyright (C) 2015 Graylog, Inc. (hello@graylog.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.graylog.plugins.usagestatistics.dto.elasticsearch;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@JsonAutoDetect
@AutoValue
public abstract class IndicesStats {
    public static IndicesStats create(int indexCount,
                                      long storeSize,
                                      long fieldDataSize,
                                      long idCacheSize) {
        return new AutoValue_IndicesStats(indexCount, storeSize, fieldDataSize, idCacheSize);
    }

    public static IndicesStats fromIndicesStats(org.graylog2.system.stats.elasticsearch.IndicesStats indicesStats) {
        return create(
                indicesStats.indexCount(),
                indicesStats.storeSize(),
                indicesStats.fieldDataSize(),
                indicesStats.idCacheSize()
        );
    }

    @JsonProperty
    public abstract int indexCount();

    @JsonProperty
    public abstract long storeSize();

    @JsonProperty
    public abstract long fieldDataSize();

    @JsonProperty
    public abstract long idCacheSize();
}
