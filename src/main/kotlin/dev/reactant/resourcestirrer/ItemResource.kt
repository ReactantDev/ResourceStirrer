package dev.reactant.resourcestirrer

import org.bukkit.Material

interface ItemResource {
    val baseItem: Material?
    val baseResource: ItemResource?
    val predicate: Map<String, Any>

    val identifier: String

    val rootBaseItem: Material
        get() = when {
            baseItem == null && baseResource != null -> baseResource!!.rootBaseItem
            baseItem != null -> baseItem!!
            else -> throw IllegalStateException("A item resource must have either baseItem or baseResource")
        }

    /**
     * Write the model file of item resource to the destination path
     */
    fun writeModelFile(path: String)

    /**
     * Write the textures files of item resource to the destination folder path
     */
    fun writeTextureFiles(path: String)
}
