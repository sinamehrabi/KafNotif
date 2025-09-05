package com.kafnotif.config;

/**
 * Threading modes for notification processing
 */
public enum ThreadingMode {
    /**
     * Use platform threads (traditional OS threads)
     */
    PLATFORM_THREADS,
    
    /**
     * Use virtual threads (Project Loom - Java 21+)
     */
    VIRTUAL_THREADS,
    
    /**
     * Use single-threaded processing
     */
    SINGLE_THREADED
}
