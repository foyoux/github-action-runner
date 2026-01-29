package com.github.foyoux.githubactionrunner.core

import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.zip.GZIPOutputStream
import java.nio.charset.StandardCharsets

object GzipUtil {
    fun compressAndEncode(content: String): String {
        if (content.isEmpty()) return ""
        
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzip ->
            gzip.write(content.toByteArray(StandardCharsets.UTF_8))
        }
        val compressedBytes = bos.toByteArray()
        return Base64.getEncoder().encodeToString(compressedBytes)
    }
}