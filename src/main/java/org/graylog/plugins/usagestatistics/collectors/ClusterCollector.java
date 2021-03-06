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
package org.graylog.plugins.usagestatistics.collectors;

import com.github.joschi.jadconfig.util.Duration;
import com.google.common.collect.ImmutableMap;
import org.graylog.plugins.usagestatistics.UsageStatsMetaData;
import org.graylog.plugins.usagestatistics.dto.AlarmStats;
import org.graylog.plugins.usagestatistics.dto.ClusterDataSet;
import org.graylog.plugins.usagestatistics.dto.ClusterStats;
import org.graylog.plugins.usagestatistics.dto.LdapStats;
import org.graylog2.indexer.counts.Counts;
import org.graylog2.plugin.cluster.ClusterConfigService;
import org.graylog2.plugin.cluster.ClusterId;
import org.graylog2.plugin.inputs.Extractor;
import org.graylog2.system.stats.ClusterStatsService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClusterCollector {
    private final ClusterStatsService clusterStatsService;
    private final ElasticsearchCollector elasticsearchCollector;
    private final MongoCollector mongoCollector;
    private final CollectorCollector collectorCollector;
    private final Counts counts;
    private final ClusterConfigService clusterConfigService;
    private final long reportIntervalMs;

    @Inject
    public ClusterCollector(ClusterStatsService clusterStatsService,
                            ElasticsearchCollector elasticsearchCollector,
                            MongoCollector mongoCollector,
                            CollectorCollector collectorCollector,
                            Counts counts,
                            @Named("usage_statistics_report_interval") Duration reportInterval,
                            ClusterConfigService clusterConfigService) {
        this.clusterStatsService = checkNotNull(clusterStatsService);
        this.elasticsearchCollector = checkNotNull(elasticsearchCollector);
        this.mongoCollector = checkNotNull(mongoCollector);
        this.collectorCollector = checkNotNull(collectorCollector);
        this.counts = checkNotNull(counts);
        this.reportIntervalMs = checkNotNull(reportInterval).toMilliseconds();
        this.clusterConfigService = checkNotNull(clusterConfigService);
    }

    public ClusterDataSet getClusterDataSet() {
        final ClusterId clusterId = clusterConfigService.getOrDefault(ClusterId.class, ClusterId.create(""));

        return ClusterDataSet.create(
                String.valueOf(UsageStatsMetaData.VERSION),
                clusterId.clusterId(),
                System.currentTimeMillis(),
                reportIntervalMs,
                buildClusterStats()
        );
    }

    private ClusterStats buildClusterStats() {
        final org.graylog2.system.stats.ClusterStats clusterStats = clusterStatsService.clusterStats();

        return ClusterStats.create(
                elasticsearchCollector.getClusterStats(),
                elasticsearchCollector.getNodeInfos(),
                mongoCollector.getMongoStats(),
                collectorCollector.getCollectorInfos(),
                clusterStats.streamCount(),
                clusterStats.streamRuleCount(),
                clusterStats.streamRuleCountByStream(),
                clusterStats.userCount(),
                clusterStats.outputCount(),
                clusterStats.outputCountByType(),
                clusterStats.dashboardCount(),
                clusterStats.inputCount(),
                clusterStats.globalInputCount(),
                clusterStats.inputCountByType(),
                clusterStats.extractorCount(),
                buildExtractorCountByType(),
                clusterStats.contentPackCount(),
                counts.total(),
                buildStreamThroughput(),
                buildLdapStats(),
                buildAlarmStats()
        );
    }

    private Map<String, Long> buildStreamThroughput() {
        return Collections.emptyMap();
    }

    private Map<String, Long> buildExtractorCountByType() {
        final ImmutableMap.Builder<String, Long> builder = ImmutableMap.builder();
        for (Map.Entry<Extractor.Type, Long> entry : clusterStatsService.clusterStats().extractorCountByType().entrySet()) {
            builder.put(entry.getKey().name(), entry.getValue());
        }
        return builder.build();
    }

    private LdapStats buildLdapStats() {
        final org.graylog2.system.stats.LdapStats ldapStats = clusterStatsService.ldapStats();
        return LdapStats.create(ldapStats.enabled(),
                                ldapStats.activeDirectory(),
                                ldapStats.roleMappingCount(),
                                ldapStats.roleCount());
    }

    private AlarmStats buildAlarmStats() {
        final org.graylog2.system.stats.AlarmStats stats = clusterStatsService.alarmStats();
        return AlarmStats.create(stats.alertCount(), stats.alarmcallbackCountByType());
    }
}
