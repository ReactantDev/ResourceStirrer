package dev.reactant.resourcestirrer.collector

import com.google.common.collect.ImmutableMap
import dev.reactant.reactant.core.component.Component
import dev.reactant.resourcestirrer.ItemResource

@Component
class ItemResourceManagingService() {
    private val _identifierResources = HashMap<String, ItemResource>()
    public val identifierResources: Map<String, ItemResource> get() = ImmutableMap.copyOf(_identifierResources)

    /**
     * @param resourceLoader the loader of resources
     * @param identifier the identifier which represent the resource and should not be changed
     */
    fun addItem(itemResource: ItemResource) {
        if (_identifierResources.containsKey(itemResource.identifier))
            throw IllegalStateException("Item resource identifier repeated: ${itemResource.identifier}")
        _identifierResources[itemResource.identifier] = itemResource
    }

    /**
     * Bulk add item
     */
    fun addItem(itemResource: Iterable<ItemResource>) {
        itemResource.forEach(::addItem);
    }

    fun getItem(identifier: String): ItemResource? = identifierResources[identifier]

}
