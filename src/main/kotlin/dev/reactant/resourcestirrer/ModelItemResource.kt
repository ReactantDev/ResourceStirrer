package dev.reactant.resourcestirrer

import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import dev.reactant.resourcestirrer.stirring.tasks.ItemResourceWritingTask
import dev.reactant.resourcestirrer.utils.outputTo
import org.bukkit.Material
import java.io.File
import java.io.InputStream

abstract class ModelItemResource(
        val modelResourcePath: String,
        val _identifier: String?,
        override val baseItem: Material?,
        override val baseResource: ItemResource?,
        override val predicate: Map<String, Any>
) : ItemResource {
    val modelFileInputStream: InputStream
        get() = resourceLoader.getResourceFile("${modelResourcePath}/model.json")
                ?: throw IllegalStateException("An item resource is missing model.json file: $identifier");

    val resourceLoader = ClassLoaderResourceLoader(this.javaClass.classLoader)

    override var allocatedCustomModelData: Int? = null

    override val identifier: String
        get() = _identifier ?: this.javaClass.canonicalName

    constructor(modelResourcePath: String, itemResourceIdentifier: String?, baseItem: Material?, predicate: Map<String, Any>)
            : this(modelResourcePath, itemResourceIdentifier, baseItem, null, predicate)

    constructor(modelResourcePath: String, itemResourceIdentifier: String?, baseItem: Material?)
            : this(modelResourcePath, itemResourceIdentifier, baseItem, null, mapOf())

    constructor(modelResourcePath: String, itemResourceIdentifier: String?, baseResource: ItemResource?, predicate: Map<String, Any>)
            : this(modelResourcePath, itemResourceIdentifier, null, baseResource, predicate)

    constructor(modelResourcePath: String, itemResourceIdentifier: String?, baseResource: ItemResource?)
            : this(modelResourcePath, itemResourceIdentifier, null, baseResource, mapOf())

    override fun writeModelFile(path: String) {
        this.modelFileInputStream.use { it.outputTo(File(path)) }
    }

    override fun writeTextureFiles(path: String) {
        resourceLoader.getResourceFiles(modelResourcePath).forEach { filePath ->
            resourceLoader.getResourceFile(filePath)
                    ?.let { ItemResourceWritingTask.CopyingFile(it, "$path/${filePath.removePrefix(modelResourcePath)}") }
                    ?.let { it.inputStream.use { input -> input.outputTo(File(it.fileName)) } }
        }
    }
}
