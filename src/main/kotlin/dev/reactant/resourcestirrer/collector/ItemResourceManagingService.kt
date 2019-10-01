package dev.reactant.resourcestirrer.collector

import dev.reactant.reactant.core.component.Component
import dev.reactant.resourcestirrer.ItemResource
import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import dev.reactant.resourcestirrer.resourceloader.ResourceLoader

@Component
class ItemResourceManagingService() {
    private val _identifierResources = HashMap<String, ItemResource>()
    public val identifierResources: Map<String, ItemResource> get() = _identifierResources

    /**
     * @param resourceLoader the loader of resources
     * @param identifier the identifier which represent the resource and should not be changed
     */
    fun addItem( itemResource: ItemResource) {
        if (_identifierResources.containsKey(itemResource.identifier)) throw IllegalStateException()
        _identifierResources[itemResource.identifier] = itemResource

    }

    fun getItem(identifier: String): ItemResource? = identifierResources[identifier]

}
