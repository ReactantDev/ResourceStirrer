package dev.reactant.resourcestirrer.collector

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.container.ContainerManager
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.reactant.core.dependency.injection.components.Components
import dev.reactant.reactant.core.dependency.layers.SystemLevel
import dev.reactant.resourcestirrer.resourcetype.item.ItemResource
import dev.reactant.resourcestirrer.table.ItemResourcesTable
import dev.reactant.resourcestirrer.table.ResourcesTable
import kotlin.reflect.full.isSubclassOf

/**
 * A service that used to collect custom resource, and provide to stirrer service
 */
@Component
class ItemResourceManagingService(
        private val containerManager: ContainerManager,
        private val itemResourceProviders: Components<ItemResourceProvider>
) : LifeCycleHook, SystemLevel {
    private val _identifierResources = HashMap<String, ItemResource>()
    public val identifierResources: Map<String, ItemResource> get() = _identifierResources

    override fun onEnable() {
        containerManager.containers
                .flatMap { it.reflections.getTypesAnnotatedWith(ResourcesTable::class.java) }
                .asSequence()
                .map { it.kotlin }
                .filter { it.isSubclassOf(ItemResourcesTable::class) }
                .mapNotNull { it.objectInstance as? ItemResourcesTable }
                .toList().union(itemResourceProviders.map { it.itemResources })
                .forEach { addItem(it) }
    }

    fun addItem(vararg itemResource: ItemResource) {
        itemResource.forEach {
            if (_identifierResources.containsKey(it.identifier))
                throw IllegalStateException("Item resource identifier repeated: ${it.identifier}")
            _identifierResources[it.identifier] = it
        }
    }

    fun addItem(resources: Iterable<ItemResource>) {
        resources.let { addItem(*it.toSet().toTypedArray()) }
    }

    fun getItem(identifier: String): ItemResource? = identifierResources[identifier]

}
