package dev.reactant.resourcestirrer.itemresource

import com.google.gson.Gson
import dev.reactant.resourcestirrer.model.AnimationMeta
import dev.reactant.resourcestirrer.model.ItemModel
import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import dev.reactant.resourcestirrer.stirring.tasks.ItemResourceWritingTask
import dev.reactant.resourcestirrer.table.ItemResourcesTable
import dev.reactant.resourcestirrer.utils.outputTo
import org.bukkit.Material
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.io.InputStreamReader

open class ClassLoaderItemResource(
        private val resourceLoader: ClassLoaderResourceLoader,
        private val modelPath: String?,
        private val textureLayersPath: Map<Int, String>,
        override val identifier: String,
        override val baseItem: Material?,
        override val baseResource: ItemResource?,
        override val predicate: Map<String, Any>
) : ItemResource, GeneratedItemModelItemResource<ClassLoaderItemResource> {

    override var itemModel = DEFAULT_ITEM_MODEL.copy().apply {
        textures {
            textureLayersPath.keys.forEach { layer ->
                "layer$layer"("stirred:{{prefix}}/layer$layer")
            }
        }
    }


    val animationMeta: HashMap<Int, AnimationMeta> = hashMapOf()

    override var allocatedCustomModelData: Int? = null


    private val modelFileInputStream: InputStream?
        get() = resourceLoader.getResourceFile("$modelPath.json")

    /**
     * Aniatiom meta by layer
     */
    private val animationMetaInputStream: Map<Int, InputStream?>
        get() = textureLayersPath.mapNotNull { (layer, texturePath) ->
            resourceLoader.getResourceFile("$texturePath.png.mcmeta")?.let { input -> layer to input }
        }.toMap()

    /**
     * Read only, changes will not affect anything
     * @return null if a item model file cannot be find
     */
    val originalItemModel: ItemModel?
        get() = modelFileInputStream?.use { InputStreamReader(it).use { GSON.fromJson(it, ItemModel::class.java) } }

    /**
     * The original item model will be not used in resource pack outputting if true and itemModel is not null
     */
    var ignoreOriginalItemModel = false

    /**
     * Read only, changes will not affect anything
     * @return null if a item model file cannot be find
     */
    val originalAnimationMeta: Map<Int, AnimationMeta?>
        get() = animationMetaInputStream.mapValues { (_, inputStream) ->
            inputStream
                    ?.use { InputStreamReader(it).use { GSON.fromJson(it, AnimationMeta::class.java) } }
        }

    /**
     * The original item model will be not used in resource pack outputting if true and animationMeta is not null
     */
    var ignoreOriginalAnimationMeta = false

    override fun writeModelFile(path: String) {
        fun modelOutputFromObject() = FileWriter(File(path), false).use { it.write(itemModel.toJson()) }
        fun modelOutputFromFile() = this.modelFileInputStream?.use { it.outputTo(File(path)) }
        when {
            ignoreOriginalItemModel -> modelOutputFromObject()
            else -> modelOutputFromFile() ?: modelOutputFromObject()
        }
    }

    override fun writeTextureFiles(path: String) {
        textureLayersPath.forEach { (layer, texturePath) ->
            extractFileFromLoader("$texturePath.png", "$path/layer$layer.png")
                    ?: throw IllegalArgumentException("Texture file not found (identifier: ${identifier}, missing file: $texturePath)")


            val outputAnimationMetaFile = File("$path/layer$layer.png.mcmeta")
            fun animationOutputFromObject() = animationMeta[layer]?.toJson()
                    ?.let { FileWriter(outputAnimationMetaFile, false).use { writer -> writer.write(it) } }

            fun animationOutputFromFile() = animationMetaInputStream[layer]?.use { input -> input.outputTo(outputAnimationMetaFile) }
            when {
                ignoreOriginalAnimationMeta && animationMeta[layer] != null -> animationOutputFromObject()
                else -> animationOutputFromFile() ?: animationOutputFromObject()
            }
        }
    }

    private fun extractFileFromLoader(fromPath: String, toPath: String): Unit? = resourceLoader.getResourceFile(fromPath)
            ?.let { ItemResourceWritingTask.CopyingFile(it, toPath) }
            ?.let { it.inputStream.use { input -> input.outputTo(File(it.fileName)) } }

    companion object {
        private val DEFAULT_ITEM_MODEL = ItemModel()
        private val GSON = Gson()
    }
}

/**
 * Use for single layer resource, assume item model and animation meta having same prefix like texture path
 * For example:
 *      When: texturePath = "text"
 *      Then: texture = "text.png", itemModel = "text.json", animationMeta = "text.png.mcmeta"
 */
fun ItemResourcesTable.byClassLoader(texturePath: String, itemResourceIdentifier: String?,
                                     baseItem: Material?, predicate: Map<String, Any> = mapOf()) =
        ClassLoaderItemResource(this.resourceLoader, texturePath, mapOf(0 to texturePath),
                "${this.identifierPrefix}-$itemResourceIdentifier", baseItem, null, predicate)

/**
 * Use for multiple layer resource
 */

fun ItemResourcesTable.byClassLoader(itemModelPath: String?, layerTexturePath: Map<Int, String>, itemResourceIdentifier: String?,
                                     baseItem: Material?, predicate: Map<String, Any> = mapOf()) =
        ClassLoaderItemResource(this.resourceLoader, itemModelPath, layerTexturePath,
                "${this.identifierPrefix}-$itemResourceIdentifier", baseItem, null, predicate)

/**
 * Use for multiple layer resource without providing itmModel
 */
fun ItemResourcesTable.byClassLoader(layerTexturePath: Map<Int, String>, itemResourceIdentifier: String?,
                                     baseItem: Material?, predicate: Map<String, Any> = mapOf()) =
        byClassLoader(null, layerTexturePath, itemResourceIdentifier, baseItem, predicate)
