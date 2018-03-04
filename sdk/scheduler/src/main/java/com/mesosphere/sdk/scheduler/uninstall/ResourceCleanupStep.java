package com.mesosphere.sdk.scheduler.uninstall;

import com.mesosphere.sdk.scheduler.plan.PodInstanceRequirement;
import com.mesosphere.sdk.scheduler.plan.Status;

import java.util.Optional;
import java.util.Set;

/**
 * Step which implements the uninstalling of a particular reserved resource. For instance, persistent volumes and cpu.
 */
public class ResourceCleanupStep extends UninstallStep {

    private final String resourceId;

    /**
     * Creates a new instance with the provided {@code resourceId} and initial {@code status}.
     */
    public ResourceCleanupStep(String resourceId, Status status) {
        // Avoid having the step name be a pure UUID. Otherwise PlansResource will confuse this UUID with the step UUID:
        super("unreserve-" + resourceId, status);
        this.resourceId = resourceId;
    }

    @Override
    public Optional<PodInstanceRequirement> start() {
        if (isPending()) {
            logger.info("Setting state to Prepared for resource {}", resourceId);
            setStatus(Status.PREPARED);
        }

        return getPodInstanceRequirement();
    }

    /**
     * Notifies this step that some resource ids are about to be unreserved. If any of the resource ids are relevante to
     * this step, it will update its status to {@code COMPLETE}.
     */
    public void updateResourceStatus(Set<String> uninstalledResourceIds) {
        if (uninstalledResourceIds.contains(resourceId)) {
            logger.info("Completed dereservation of resource {}", resourceId);
            setStatus(Status.COMPLETE);
        }
    }
}
