package dev.reactant.resourcestirrer.resourcetype

import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import dev.reactant.resourcestirrer.utils.outputTo
import java.io.File

abstract class ClassLoaderResource(
        protected val resourceLoader: ClassLoaderResourceLoader
) {

    protected fun extractFileFromLoader(fromPath: String, toPath: String): Unit? = resourceLoader.getResourceFile(fromPath)
            ?.let { it.use { input -> input.outputTo(File(toPath)) } }

    protected fun getResourceFile(path: String) = resourceLoader.getResourceFile(path)

}
