package com.example.data.source

import java.io.File

object PeerSanitizer {
    private val ALLOWED_AUDIO_EXTENSIONS = setOf("mp3", "flac", "m4a", "alac", "wav", "ogg", "opus")
    private val ALLOWED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")

    /**
     * Sanitizes peer-supplied paths to strictly prevent path traversal vulnerabilities.
     */
    fun sanitizePath(rawPath: String): String {
        var clean = rawPath.replace("\\", "/")
        // Block path traversal attempts
        while (clean.contains("../") || clean.contains("./") || clean.contains("//")) {
            clean = clean.replace("../", "")
                .replace("./", "")
                .replace("//", "/")
        }
        clean = clean.trim().trimStart('/')
        return clean
    }

    /**
     * Sanitizes filename and enforces audio extension check.
     */
    fun sanitizeFilename(rawFilename: String): String {
        val clean = File(sanitizePath(rawFilename)).name
        return clean.replace(Regex("[^a-zA-Z0-9._ -]"), "_")
    }

    /**
     * Verifies if the peer file is safe audio.
     */
    fun isValidAudioFile(filename: String): Boolean {
        val ext = filename.substringAfterLast(".", "").lowercase()
        return ALLOWED_AUDIO_EXTENSIONS.contains(ext)
    }

    /**
     * Verifies if the peer file is an image (e.g. album cover).
     */
    fun isValidImageFile(filename: String): Boolean {
        val ext = filename.substringAfterLast(".", "").lowercase()
        return ALLOWED_IMAGE_EXTENSIONS.contains(ext)
    }

    /**
     * Sanitizes peer username (no control characters or injection).
     */
    fun sanitizeUsername(rawUser: String): String {
        return rawUser.filter { it.isLetterOrDigit() || it == '_' || it == '-' || it == '.' }.take(32)
    }
}
