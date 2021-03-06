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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.admin.cluster.node.stats.NodeStats;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.monitor.jvm.JvmStats;
import org.graylog.plugins.usagestatistics.dto.HostInfo;
import org.graylog.plugins.usagestatistics.dto.JvmInfo;
import org.graylog.plugins.usagestatistics.dto.MacAddress;
import org.graylog.plugins.usagestatistics.dto.elasticsearch.ClusterHealth;
import org.graylog.plugins.usagestatistics.dto.elasticsearch.ElasticsearchClusterStats;
import org.graylog.plugins.usagestatistics.dto.elasticsearch.ElasticsearchNodeInfo;
import org.graylog.plugins.usagestatistics.dto.elasticsearch.IndicesStats;
import org.graylog.plugins.usagestatistics.dto.elasticsearch.NodesStats;
import org.graylog2.system.stats.ClusterStatsService;
import org.graylog2.system.stats.elasticsearch.ElasticsearchStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ElasticsearchCollector {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchCollector.class);

    private final Client client;
    private final ClusterStatsService clusterStatsService;

    @Inject
    public ElasticsearchCollector(Client client, ClusterStatsService clusterStatsService) {
        this.client = client;
        this.clusterStatsService = clusterStatsService;
    }

    public Set<ElasticsearchNodeInfo> getNodeInfos() {
        final Map<String, NodeInfo> nodeInfos = fetchNodeInfos();
        final Map<String, NodeStats> nodeStats = fetchNodeStats();

        final Set<ElasticsearchNodeInfo> elasticsearchNodeInfos = Sets.newHashSetWithExpectedSize(nodeInfos.size());
        for (String node : nodeInfos.keySet()) {
            final NodeInfo info = nodeInfos.get(node);
            final NodeStats stats = nodeStats.get(node);

            if (info == null || stats == null) {
                LOG.warn("Couldn't retrieve all required information from Elasticsearch node {}, skipping.", node);
                continue;
            }

            // TODO remove these as soon as the backend service treats HostInfo as optional
            // the host info details aren't available in Elasticsearch 2.x anymore, but we still report the empty
            // bean because the backend service still expects some data (even if it is empty)
            final MacAddress macAddress = MacAddress.EMPTY;
            final HostInfo.Cpu cpu = null;
            final HostInfo.Memory memory = null;
            final HostInfo.Memory swap =  null;
            final HostInfo hostInfo = HostInfo.create(macAddress, cpu, memory, swap);

            final List<String> garbageCollectors;
            if (stats.getJvm() != null) {
                garbageCollectors = Lists.newArrayList();
                for (JvmStats.GarbageCollector gc : stats.getJvm().getGc()) {
                    garbageCollectors.add(gc.getName());
                }
            } else {
                garbageCollectors = Collections.emptyList();
            }

            final JvmInfo jvmInfo;
            if (info.getJvm() != null) {
                final JvmInfo.Memory jvmMemory = JvmInfo.Memory.create(
                        info.getJvm().getMem().getHeapInit().bytes(),
                        info.getJvm().getMem().getHeapMax().bytes(),
                        info.getJvm().getMem().getNonHeapInit().bytes(),
                        info.getJvm().getMem().getNonHeapMax().bytes(),
                        info.getJvm().getMem().getDirectMemoryMax().bytes()
                );
                final JvmInfo.Os jvmOs = JvmInfo.Os.create(
                        info.getJvm().getSystemProperties().get("os.name"),
                        info.getJvm().getSystemProperties().get("os.version"),
                        info.getJvm().getSystemProperties().get("os.arch")
                );
                jvmInfo = JvmInfo.create(
                        info.getJvm().version(),
                        info.getJvm().getVmName(),
                        info.getJvm().getVmVersion(),
                        info.getJvm().getVmVendor(),
                        jvmOs,
                        jvmMemory,
                        garbageCollectors
                );
            } else {
                jvmInfo = null;
            }

            final ElasticsearchNodeInfo elasticsearchNodeInfo = ElasticsearchNodeInfo.create(
                    info.getVersion().toString(),
                    hostInfo,
                    jvmInfo
            );

            elasticsearchNodeInfos.add(elasticsearchNodeInfo);
        }

        return elasticsearchNodeInfos;
    }

    public ElasticsearchClusterStats getClusterStats() {
        final ElasticsearchStats stats = clusterStatsService.elasticsearchStats();
        return ElasticsearchClusterStats.create(
                ClusterHealth.fromClusterHealth(stats.clusterHealth()),
                NodesStats.fromNodesStats(stats.nodesStats()),
                IndicesStats.fromIndicesStats(stats.indicesStats())
        );
    }

    private Map<String, NodeInfo> fetchNodeInfos() {
        ClusterAdminClient adminClient = this.client.admin().cluster();
        NodesInfoResponse nodesInfoResponse = adminClient.nodesInfo(adminClient.prepareNodesInfo().request()).actionGet();
        return nodesInfoResponse.getNodesMap();
    }

    private Map<String, NodeStats> fetchNodeStats() {
        ClusterAdminClient adminClient = this.client.admin().cluster();
        NodesStatsResponse nodesStatsResponse = adminClient.nodesStats(adminClient.prepareNodesStats().request()).actionGet();
        return nodesStatsResponse.getNodesMap();
    }
}
