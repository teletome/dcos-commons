package com.mesosphere.sdk.scheduler;

import java.util.Map;

import com.mesosphere.sdk.curator.CuratorLocker;
import com.mesosphere.sdk.curator.CuratorPersister;
import com.mesosphere.sdk.storage.Persister;
import com.mesosphere.sdk.storage.PersisterCache;
import com.mesosphere.sdk.storage.PersisterException;

/**
 * Sets up and executes a {@link FrameworkRunner} to which {@link ServiceScheduler}s may be added.
 */
public class QueueRunner implements Runnable {

    private final SchedulerConfig schedulerConfig;
    private final FrameworkConfig frameworkConfig;
    private final Persister persister;
    private final MesosEventClient client;

    /**
     * Returns a new {@link QueueRunner} instance which may be launched with {@code run()}.
     *
     * @param client the Mesos event client which receives offers/statuses from Mesos. Note that this may route events
     *               to multiple wrapped clients
     */
    public static QueueRunner build(MesosEventClient client) {
        Map<String, String> env = System.getenv();
        SchedulerConfig schedulerConfig = SchedulerConfig.fromMap(env);
        FrameworkConfig frameworkConfig = FrameworkConfig.fromMap(env);
        Persister persister;
        try {
            persister = schedulerConfig.isStateCacheEnabled()
                    ? new PersisterCache(CuratorPersister.newBuilder(
                            frameworkConfig.frameworkName, frameworkConfig.zookeeperConnection).build())
                    : CuratorPersister.newBuilder(
                            frameworkConfig.frameworkName, frameworkConfig.zookeeperConnection).build();
        } catch (PersisterException e) {
            throw new IllegalStateException(String.format(
                    "Failed to initialize default persister at %s for framework %s",
                    frameworkConfig.zookeeperConnection, frameworkConfig.frameworkName));
        }
        return new QueueRunner(schedulerConfig, frameworkConfig, persister, client);

    }

    private QueueRunner(
            SchedulerConfig schedulerConfig,
            FrameworkConfig frameworkConfig,
            Persister persister,
            MesosEventClient client) {
        this.schedulerConfig = schedulerConfig;
        this.frameworkConfig = frameworkConfig;
        this.persister = persister;
        this.client = client;
    }

    /**
     * Runs the queue. Don't forget to call this!
     * This should never exit, instead the entire process will be terminated internally.
     */
    @Override
    public void run() {
        CuratorLocker.lock(frameworkConfig.frameworkName, frameworkConfig.zookeeperConnection);
        Metrics.configureStatsd(schedulerConfig);

        new FrameworkRunner(schedulerConfig, frameworkConfig).registerAndRunFramework(persister, client);
    }
}