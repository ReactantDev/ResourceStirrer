package dev.reactant.resourcestirrer

import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import org.bukkit.Material
import java.io.InputStream

abstract class ModelItemResource(
        override val modelResourcePath: String,
        val _identifier: String?,
        override val baseItem: Material?,
        override val baseResource: ItemResource?,
        override val predicate: Map<String, Any>
) : ItemResource {
    override val modelFileInputStream: InputStream
        get() = resourceLoader.getResourceFile("${modelResourcePath}/model.json")
                ?: throw IllegalStateException("An item resource is missing model.json file: $identifier");

    override val resourceLoader = ClassLoaderResourceLoader(this.javaClass.classLoader)

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

}
