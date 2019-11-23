package dev.reactant.resourcestirrer.stirring

import dev.reactant.reactant.service.spec.config.Config
import dev.reactant.resourcestirrer.ItemResource
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.config.ResourceStirrerConfig
import dev.reactant.resourcestirrer.config.StirrerMetaLock
import org.bukkit.Material
import java.io.File

class StirringPlan() {
    lateinit var stirrerMetaLock: Config<StirrerMetaLock>

    /**
     * The identifier which indicate a material with custom meta was used in the base material.
     */
    lateinit var usedIdentifiers: Set<String>
    lateinit var resourceStirrerConfig: Config<ResourceStirrerConfig>
    lateinit var baseResourcePack: File

    lateinit var resourcePackSha1: ByteArray;

    /**
     * Allocate a new custom meta if not exist in MetaLock
     */
    public fun addItemResource(itemResource: ItemResource, itemResourceIdentifier: String) {
        var customMeta = stirrerMetaLock.content.itemResourceCustomMetaLock[itemResourceIdentifier]?.split("-")?.last()?.toInt()
        if (customMeta==null) {
            customMeta = searchUsableCustomMeta(itemResource.rootBaseItem);
            stirrerMetaLock.content.itemResourceCustomMetaLock[itemResourceIdentifier] = "${itemResource.rootBaseItem.name.toLowerCase()}-$customMeta";
        }
        itemResource.allocatedCustomModelData = customMeta;
    }

    private fun searchUsableCustomMeta(material: Material): Int {
        val materialName = material.name.toLowerCase();
        while (true) {
            val possibleCustomMeta = stirrerMetaLock.content.lastAllocatedCustomMeta
                    .getOrPut(materialName) { resourceStirrerConfig.content.customMetaRange.min } + 1;
            stirrerMetaLock.content.lastAllocatedCustomMeta[materialName] = possibleCustomMeta
            val possibleIdentifier = "$materialName-$possibleCustomMeta";
            if (!usedIdentifiers.contains(possibleIdentifier)) {
                return possibleCustomMeta;
            }
        }
    }

}
