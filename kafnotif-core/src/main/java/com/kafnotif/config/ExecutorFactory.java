package com.kafnotif.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Factory for creating executors based on threading mode
 */
public class ExecutorFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecutorFactory.class);
    
    /**
     * Create executor service based on threading mode and pool size
     */
    public static ExecutorService create(ThreadingMode mode, int poolSize) {
        return switch (mode) {
            case VIRTUAL_THREADS -> createVirtualThreadExecutor(poolSize);
            case PLATFORM_THREADS -> createPlatformThreadExecutor(poolSize);
            case SINGLE_THREADED -> createSingleThreadExecutor();
        };
    }
    
    /**
     * Create virtual thread executor (Java 21+)
     */
    private static ExecutorService createVirtualThreadExecutor(int poolSize) {
        try {
            // Use virtual threads if available (Java 21+)
            ThreadFactory virtualThreadFactory = Thread.ofVirtual()
                    .name("kafnotif-virtual-", 0)
                    .factory();
            
            logger.info("🚀 Created virtual thread executor with factory");
            return Executors.newThreadPerTaskExecutor(virtualThreadFactory);
            
        } catch (Exception e) {
            logger.warn("Virtual threads not available, falling back to platform threads: {}", e.getMessage());
            return createPlatformThreadExecutor(poolSize);
        }
    }
    
    /**
     * Create platform thread executor
     */
    private static ExecutorService createPlatformThreadExecutor(int poolSize) {
        ThreadFactory threadFactory = Thread.ofPlatform()
                .name("kafnotif-platform-", 0)
                .daemon(true)
                .factory();
        
        logger.info("🔧 Created platform thread executor with {} threads", poolSize);
        return Executors.newFixedThreadPool(poolSize, threadFactory);
    }
    
    /**
     * Create single thread executor
     */
    private static ExecutorService createSingleThreadExecutor() {
        ThreadFactory threadFactory = Thread.ofPlatform()
                .name("kafnotif-single")
                .daemon(true)
                .factory();
        
        logger.info("🔧 Created single thread executor");
        return Executors.newSingleThreadExecutor(threadFactory);
    }
}
