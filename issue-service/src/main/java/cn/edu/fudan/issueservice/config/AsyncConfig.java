package cn.edu.fudan.issueservice.config;

import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author fancying
 * @author beethoven
 * @author pjh
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    private static final int CORE_POOL_SIZE = 3;
    private static final int MAX_POOL_SIZE = 5;
    private static final int QUEUE_CAPACITY = 30;
    private static final int KEEP_ALIVE_SECONDS = 600;
    private static final String THREAD_NAME_PREFIX = "async-task-thread-pool-";
    private final RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();

    @Value("${parallelScanRepoSize}")
    private int parallelScanRepoSize;
    @Value("${repoQueueCapacity}")
    private int repoQueueCapacity;

    /**
     * 最多支持多少个库的并行扫描
     */
    @Bean("taskExecutor")
    public TaskExecutor repoScanTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(parallelScanRepoSize);
        executor.setMaxPoolSize(parallelScanRepoSize);
        executor.setQueueCapacity(repoQueueCapacity);
        executor.setKeepAliveSeconds(6000);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setRejectedExecutionHandler(rejectedExecutionHandler);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    /**
     * 最多支持多少个库的并行扫描
     * 这个线程池用来监管  库 准备commit的状态
     */
    @Bean("prepare-resource")
    public TaskExecutor prepareResourceTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(parallelScanRepoSize);
        executor.setMaxPoolSize(parallelScanRepoSize);
        executor.setQueueCapacity(repoQueueCapacity);
        executor.setKeepAliveSeconds(6000);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setRejectedExecutionHandler(rejectedExecutionHandler);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    /**
     * 最多支持多少个commit并行准备资源 每个repo是3个commit并行
     */
    @Bean("produce-resource")
    public TaskExecutor produceResourceTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3*parallelScanRepoSize);
        executor.setMaxPoolSize(3*parallelScanRepoSize);
        executor.setQueueCapacity(repoQueueCapacity);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setRejectedExecutionHandler(rejectedExecutionHandler);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    /**
     * 最多支持多少个库的同时删除
     */
    @Bean("delete-issue")
    public TaskExecutor deleteIssueTaskExecutor() {
        return createOne();
    }

    private ThreadPoolTaskExecutor createOne() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setRejectedExecutionHandler(rejectedExecutionHandler);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

}
