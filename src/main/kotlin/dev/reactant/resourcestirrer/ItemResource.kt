package dev.reactant.resourcestirrer

import dev.reactant.resourcestirrer.resourceloader.ResourceLoader
import org.bukkit.Material
import java.io.InputStream

interface ItemResource {
    val modelResourcePath: String
    val baseItem: Material?
    val baseResource: ItemResource?
    val predicate: Map<String, Any>

    val resourceLoader: ResourceLoader
    val modelFileInputStream: InputStream
    val identifier: String

    val rootBaseItem: Material
        get() = when {
            baseItem == null && baseResource != null -> baseResource!!.rootBaseItem
            baseItem != null -> baseItem!!
            else -> throw IllegalStateException("A item resource must have either baseItem or baseResource")
        }
}
