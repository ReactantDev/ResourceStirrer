package dev.reactant.resourcestirrer.collector

import com.google.common.collect.ImmutableMap
import dev.reactant.reactant.core.component.Component
import dev.reactant.resourcestirrer.itemresource.ItemResource

/**
 * A service that used to collect custom resource, and provide to stirrer service
 */
@Component
class ItemResourceManagingService() {
    private val _identifierResources = HashMap<String, ItemResource>()
    public val identifierResources: Map<String, ItemResource> get() = ImmutableMap.copyOf(_identifierResources)

    fun addItem(vararg itemResource: ItemResource) {
        itemResource.forEach {
            if (_identifierResources.containsKey(it.identifier))
                throw IllegalStateException("Item resource identifier repeated: ${it.identifier}")
            _identifierResources[it.identifier] = it
        }
    }

    fun getItem(identifier: String): ItemResource? = identifierResources[identifier]

}
