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
package org.graylog.plugins.usagestatistics.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@JsonAutoDetect
@AutoValue
public abstract class ThroughputStats {
    public static ThroughputStats create(Throughput input, Throughput output, Map<String, Throughput> perInputType) {
        return new AutoValue_ThroughputStats(input, output, perInputType);
    }

    @JsonProperty
    public abstract Throughput input();

    @JsonProperty
    public abstract Throughput output();

    @JsonProperty
    public abstract Map<String, Throughput> perInputType();

    @JsonAutoDetect
    @AutoValue
    public static abstract class Throughput {
        public static Throughput create(long count, double lastSecond, long size) {
            return new AutoValue_ThroughputStats_Throughput(count, lastSecond, size);
        }

        @JsonProperty
        public abstract long count();

        @JsonProperty
        public abstract double lastSecond();

        @JsonProperty
        public abstract long size();
    }
}
