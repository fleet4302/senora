package com.example.data.catalog

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe rate limiter enforcing MusicBrainz's mandatory 1 request per second rule.
 */
class RateLimiter(private val intervalMs: Long = 1000L) {
    private val mutex = Mutex()
    private var lastRequestTime = 0L

    suspend fun acquire() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            val timeSinceLast = now - lastRequestTime
            if (timeSinceLast < intervalMs) {
                delay(intervalMs - timeSinceLast)
            }
            lastRequestTime = System.currentTimeMillis()
        }
    }
}
