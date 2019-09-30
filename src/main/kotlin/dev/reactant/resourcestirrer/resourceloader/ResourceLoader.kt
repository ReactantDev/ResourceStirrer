package dev.reactant.resourcestirrer.resourceloader

import java.io.File
import java.io.InputStream

interface ResourceLoader {
    fun getResourceFile(resourcePath: String): InputStream?
    fun getResourceFiles(resourcePath: String): Set<String>
}
