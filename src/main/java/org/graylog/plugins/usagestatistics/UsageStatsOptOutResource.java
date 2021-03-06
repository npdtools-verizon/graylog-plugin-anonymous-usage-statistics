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
package org.graylog.plugins.usagestatistics;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.graylog.plugins.usagestatistics.audit.UsageStatsAuditEventTypes;
import org.graylog2.audit.jersey.AuditEvent;
import org.graylog2.plugin.rest.PluginRestResource;
import org.graylog2.shared.rest.resources.RestResource;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.graylog2.shared.security.RestPermissions.CLUSTER_CONFIG_ENTRY_CREATE;
import static org.graylog2.shared.security.RestPermissions.CLUSTER_CONFIG_ENTRY_READ;

@RequiresAuthentication
@Api(value = "UsageStatistics/Opt-Out", description = "Anonymous usage statistics opt-out state of this Graylog setup")
@Path("/opt-out")
public class UsageStatsOptOutResource extends RestResource implements PluginRestResource {
    private static final String CLUSTER_CONFIG_INSTANCE = UsageStatsOptOutState.class.getCanonicalName();

    private final UsageStatsOptOutService usageStatsOptOutService;

    @Inject
    public UsageStatsOptOutResource(UsageStatsOptOutService usageStatsOptOutService) {
        this.usageStatsOptOutService = usageStatsOptOutService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @ApiOperation(value = "Get opt-out status")
    @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Opt-out status does not exist"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public UsageStatsOptOutState getOptOutState() {
        checkPermission(CLUSTER_CONFIG_ENTRY_READ, CLUSTER_CONFIG_INSTANCE);

        final UsageStatsOptOutState optOutState = usageStatsOptOutService.getOptOutState();

        if (optOutState == null) {
            throw new NotFoundException();
        }

        return optOutState;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Timed
    @ApiOperation(value = "Disable sending anonymous usage stats")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Missing or invalid opt-out state"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    @AuditEvent(type = UsageStatsAuditEventTypes.OPT_OUT_UPDATE)
    public void setOptOutState(@Valid @NotNull UsageStatsOptOutState optOutState) {
        checkPermission(CLUSTER_CONFIG_ENTRY_CREATE, CLUSTER_CONFIG_INSTANCE);

        usageStatsOptOutService.setOptOutState(optOutState);
    }
}
