package dev.reactant.resourcestirrer.utils

import java.io.File
import java.io.InputStream

fun InputStream.outputTo(file: File) {
    if (!file.parentFile.exists()) file.parentFile.mkdirs()
    use { file.outputStream().use { out -> this.copyTo(out) } }
}

