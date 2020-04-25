package dev.reactant.resourcestirrer.stirring

import dev.reactant.reactant.service.spec.config.Config
import dev.reactant.resourcestirrer.config.ResourceStirrerConfig
import dev.reactant.resourcestirrer.config.StirrerCustomDataLock
import dev.reactant.resourcestirrer.resourcetype.item.ItemResource
import org.bukkit.Material
import java.io.File

class StirringPlan() {
    lateinit var stirrerCustomDataLock: Config<StirrerCustomDataLock>

    /**
     * The identifier which indicate a material with custom meta was used in the base material.
     */
    lateinit var usedIdentifiers: Set<String>
    lateinit var resourceStirrerConfig: Config<ResourceStirrerConfig>
    lateinit var baseResourcePack: File

    lateinit var resourcePackSha1: ByteArray;

    /**
     * Mapping between identifier and uglify name
     */
    var identifierPrefixMapping: HashMap<String, String> = HashMap()

    lateinit var lostReferenceCustomData: Map<String, String>

    /**
     * Allocate a new custom meta if not exist in MetaLock
     */
    public fun addItemResource(itemResource: ItemResource, itemResourceIdentifier: String) {
        var customData = stirrerCustomDataLock.content.itemResourceCustomDataLock[itemResourceIdentifier]?.split("-")?.last()?.toInt()
        if (customData == null) {
            customData = searchUsableCustomData(itemResource.rootBaseItem);
            stirrerCustomDataLock.content.itemResourceCustomDataLock[itemResourceIdentifier] = "${itemResource.rootBaseItem.name.toLowerCase()}-$customData";
        }
        itemResource.allocatedCustomModelData = customData;
    }

    private fun searchUsableCustomData(material: Material): Int {
        val materialName = material.name.toLowerCase();
        while (true) {
            val possibleCustomData = stirrerCustomDataLock.content.lastAllocatedCustomData
                    .getOrPut(materialName) { resourceStirrerConfig.content.customDataRange.min } + 1;
            stirrerCustomDataLock.content.lastAllocatedCustomData[materialName] = possibleCustomData
            val possibleIdentifier = "$materialName-$possibleCustomData";
            if (!usedIdentifiers.contains(possibleIdentifier)) {
                return possibleCustomData;
            }
        }
    }

}
