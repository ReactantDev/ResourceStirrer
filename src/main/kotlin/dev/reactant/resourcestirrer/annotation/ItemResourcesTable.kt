package dev.reactant.resourcestirrer.annotation

import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader

abstract class ItemResourcesTable(val identifierPrefix: String) {
    val resourceLoader = ClassLoaderResourceLoader(this.javaClass.classLoader)
}
