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

@JsonAutoDetect
@AutoValue
public abstract class ClusterDataSet {
    public static ClusterDataSet create(String version,
                                        String clusterId,
                                        long timestamp,
                                        long reportIntervalMs,
                                        ClusterStats clusterStats) {
        return new AutoValue_ClusterDataSet(version, clusterId, timestamp, reportIntervalMs, clusterStats);
    }

    @JsonProperty
    public abstract String version();

    @JsonProperty
    public abstract String clusterId();

    @JsonProperty
    public abstract long timestamp();

    @JsonProperty
    public abstract long reportIntervalMs();

    @JsonProperty
    public abstract ClusterStats clusterStats();
}
