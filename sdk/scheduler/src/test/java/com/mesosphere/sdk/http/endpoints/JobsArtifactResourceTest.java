package com.mesosphere.sdk.http.endpoints;

import org.junit.Test;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class JobsArtifactResourceTest {

    @Test
    public void testGetTemplateUrl() {
        UUID uuid = UUID.randomUUID();
        assertEquals(
                "http://api.svc-name.marathon.l4lb.thisdcos.directory/v1/jobs/job-name/artifacts/template/"
                        + uuid.toString() + "/some-pod/some-task/some-config",
                JobsArtifactResource.getJobTemplateUrl("svc-name", "job-name", uuid, "some-pod", "some-task", "some-config"));
        assertEquals(
                // TODO(nickbp): figure something out for slashes in job names, or just disallow them...
                "http://api.pathtosvc-name.marathon.l4lb.thisdcos.directory/v1/jobs//path/to/job-name/artifacts/template/"
                        + uuid.toString() + "/some-pod/some-task/some-config",
                JobsArtifactResource.getJobTemplateUrl("/path/to/svc-name", "/path/to/job-name", uuid, "some-pod", "some-task", "some-config"));
    }
}