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

import com.codahale.metrics.Snapshot;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@JsonAutoDetect
@AutoValue
public abstract class Histogram {
    public static Histogram create(long count, long min, long max, double mean,
                                   double p50, double p75, double p95,
                                   double p98, double p99, double p999,
                                   double stddev) {
        return new AutoValue_Histogram(count, min, max, mean, p50, p75, p95, p98, p99, p999, stddev);
    }

    public static Histogram fromMetricsTimer(com.codahale.metrics.Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        return create(
                timer.getCount(),
                snapshot.getMin(),
                snapshot.getMax(),
                snapshot.getMean(),
                snapshot.getMedian(),
                snapshot.get75thPercentile(),
                snapshot.get95thPercentile(),
                snapshot.get98thPercentile(),
                snapshot.get99thPercentile(),
                snapshot.get999thPercentile(),
                snapshot.getStdDev()
        );
    }

    public static Histogram fromMetricsHistogram(com.codahale.metrics.Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();
        return create(
                histogram.getCount(),
                snapshot.getMin(),
                snapshot.getMax(),
                snapshot.getMean(),
                snapshot.getMedian(),
                snapshot.get75thPercentile(),
                snapshot.get95thPercentile(),
                snapshot.get98thPercentile(),
                snapshot.get99thPercentile(),
                snapshot.get999thPercentile(),
                snapshot.getStdDev()
        );
    }

    @JsonProperty
    public abstract long count();

    @JsonProperty
    public abstract long min();

    @JsonProperty
    public abstract long max();

    @JsonProperty
    public abstract double mean();

    @JsonProperty
    public abstract double p50();

    @JsonProperty
    public abstract double p75();

    @JsonProperty
    public abstract double p95();

    @JsonProperty
    public abstract double p98();

    @JsonProperty
    public abstract double p99();

    @JsonProperty
    public abstract double p999();

    @JsonProperty
    public abstract double stddev();
}
