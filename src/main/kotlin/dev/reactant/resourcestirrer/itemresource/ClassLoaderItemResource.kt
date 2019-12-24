package dev.reactant.resourcestirrer.itemresource

import dev.reactant.resourcestirrer.annotation.ItemResourcesTable
import dev.reactant.resourcestirrer.model.ItemModel
import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import dev.reactant.resourcestirrer.stirring.tasks.ItemResourceWritingTask
import dev.reactant.resourcestirrer.utils.outputTo
import org.bukkit.Material
import java.io.File
import java.io.FileWriter
import java.io.InputStream

open class ClassLoaderItemResource(
        private val resourceLoader: ClassLoaderResourceLoader,
        private val searchAt: String,
        private val _identifier: String?,
        override val baseItem: Material?,
        override val baseResource: ItemResource?,
        override val predicate: Map<String, Any>
) : ItemResource {

    private val itemModel = defaultItemModel.copy().apply {
        textures {
            layer0 = "stirred:\${dir}/texture"
        }
    }

    override var allocatedCustomModelData: Int? = null

    override val identifier: String
        get() = _identifier ?: this.javaClass.canonicalName


    private val modelFileInputStream: InputStream?
        get() = resourceLoader.getResourceFile("${searchAt}.json")

    override fun writeModelFile(path: String) {
        if (this.modelFileInputStream != null) this.modelFileInputStream!!.use { it.outputTo(File(path)) }
        else FileWriter(File(path), false).use { it.write(itemModel.toJson()) }
    }

    override fun writeTextureFiles(path: String) {
        extraFileFromLoader("$searchAt.png", "$path/texture.png")
                ?: throw IllegalArgumentException("Texture file not found (identifier: ${_identifier}, missing file: ${searchAt})")

        extraFileFromLoader("$searchAt.png.mcmeta", "$path/texture.png.mcmeta")
    }

    private fun extraFileFromLoader(fromPath: String, toPath: String): Unit? = resourceLoader.getResourceFile(fromPath)
            ?.let { ItemResourceWritingTask.CopyingFile(it, toPath) }
            ?.let { it.inputStream.use { input -> input.outputTo(File(it.fileName)) } }

    companion object {
        val defaultItemModel = ItemModel()
    }
}

fun ItemResourcesTable.byClassLoader(searchAt: String, itemResourceIdentifier: String?,
                                     baseItem: Material?, predicate: Map<String, Any> = mapOf()) =
        ClassLoaderItemResource(this.resourceLoader, searchAt, "${this.identifierPrefix}-$itemResourceIdentifier", baseItem, null, predicate)
