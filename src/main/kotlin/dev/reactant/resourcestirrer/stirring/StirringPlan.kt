package dev.reactant.resourcestirrer.stirring

import dev.reactant.reactant.service.spec.config.Config
import dev.reactant.resourcestirrer.ItemResource
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

    /**
     * Allocate a new custom meta if not exist in MetaLock
     */
    public fun addItemResource(itemResource: ItemResource, itemResourceIdentifier: String) {
        if (!stirrerMetaLock.content.itemResourceCustomMetaLock.containsKey(itemResourceIdentifier)) {
            stirrerMetaLock.content.itemResourceCustomMetaLock[itemResourceIdentifier] = searchUsableIdentifier(itemResource.rootBaseItem);
        }
    }

    private fun searchUsableIdentifier(material: Material): String {
        val materialName = material.name.toLowerCase();
        while (true) {
            val possibleCustomMeta = stirrerMetaLock.content.lastAllocatedCustomMeta
                    .getOrPut(materialName) { resourceStirrerConfig.content.customMetaRange.min } + 1;
            stirrerMetaLock.content.lastAllocatedCustomMeta.put(materialName, possibleCustomMeta)
            val possibleIndentifier = "$materialName-$possibleCustomMeta";
            if (!usedIdentifiers.contains(possibleIndentifier)) {
                return possibleIndentifier;
            }
        }
    }

}
