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
        private val searchAt: String,
        override val identifier: String,
        override val baseItem: Material?,
        override val baseResource: ItemResource?,
        override val predicate: Map<String, Any>
) : ItemResource, GeneratedItemModelItemResource<ClassLoaderItemResource> {

    override var itemModel = DEFAULT_ITEM_MODEL.copy().apply {
        textures {
            layer0 = "stirred:\${dir}/texture"
        }
    }


    val animationMeta: AnimationMeta? = null

    override var allocatedCustomModelData: Int? = null


    private val modelFileInputStream: InputStream?
        get() = resourceLoader.getResourceFile("${searchAt}.json")

    private val animationMetaInputStream: InputStream?
        get() = resourceLoader.getResourceFile("${searchAt}.png.mcmeta")

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
    val originalAnimationMeta: AnimationMeta?
        get() = animationMetaInputStream?.use { InputStreamReader(it).use { GSON.fromJson(it, AnimationMeta::class.java) } }

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
        extractFileFromLoader("$searchAt.png", "$path/texture.png")
                ?: throw IllegalArgumentException("Texture file not found (identifier: ${identifier}, missing file: ${searchAt})")


        val animationMetaFile = File("$path/texture.png.mcmeta")
        fun animationOutputFromObject() = animationMeta?.toJson()
                ?.let { FileWriter(animationMetaFile, false).use { writer -> writer.write(it) } }

        fun animationOutputFromFile() = animationMetaInputStream?.use { input -> input.outputTo(animationMetaFile) }
        when {
            ignoreOriginalAnimationMeta && animationMeta != null -> animationOutputFromObject()
            else -> animationOutputFromFile() ?: animationOutputFromObject()
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

fun ItemResourcesTable.byClassLoader(searchAt: String, itemResourceIdentifier: String?,
                                     baseItem: Material?, predicate: Map<String, Any> = mapOf()) =
        ClassLoaderItemResource(this.resourceLoader, searchAt, "${this.identifierPrefix}-$itemResourceIdentifier", baseItem, null, predicate)
