package dev.reactant.resourcestirrer.itemresource

import dev.reactant.reactant.utils.content.item.itemStackOf
import org.bukkit.Material

interface ItemResource {
    val baseItem: Material?
    val baseResource: ItemResource?
    val predicate: Map<String, Any>

    val identifier: String

    var allocatedCustomModelData: Int?


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

    /**
     * Create a similar itemstack which have the allocated custom model data, but do not included the extra predicates
     */
    @JvmDefault
    val similarItemStack
        get() = itemStackOf(rootBaseItem) {
            itemMeta {
                setCustomModelData(allocatedCustomModelData
                        ?: throw IllegalStateException("The custom model data of $identifier is not allocated yet"))
            }
        }
}
