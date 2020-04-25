package dev.reactant.resourcestirrer.collector

import com.google.common.collect.ImmutableMap
import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.container.ContainerManager
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.resourcestirrer.resourcetype.sound.SoundResource
import dev.reactant.resourcestirrer.table.ResourcesTable
import dev.reactant.resourcestirrer.table.SoundResourcesTable
import kotlin.reflect.full.isSubclassOf

/**
 * A service that used to collect custom resource, and provide to stirrer service
 */
@Component
class SoundResourceManagingService(
        private val containerManager: ContainerManager
) : LifeCycleHook {
    private val _identifierResources = HashMap<String, SoundResource>()
    val identifierResources get() = ImmutableMap.copyOf(_identifierResources)

    override fun onEnable() {
        containerManager.containers
                .flatMap { it.reflections.getTypesAnnotatedWith(ResourcesTable::class.java) }
                .asSequence()
                .map { it.kotlin }
                .filter { it.isSubclassOf(SoundResourcesTable::class) }
                .mapNotNull { it.objectInstance as? SoundResourcesTable }
                .toList()
                .forEach { addSound(it) }

    }

    fun addSound(vararg soundResource: SoundResource) {
        soundResource.forEach {
            if (_identifierResources.containsKey(it.identifier))
                throw IllegalStateException("Sound resource identifier repeated: ${it.identifier}")
            _identifierResources[it.identifier] = it
        }
    }

    fun addSound(resources: Iterable<SoundResource>) {
        resources.let { addSound(*it.toSet().toTypedArray()) }
    }

    fun getSound(identifier: String): SoundResource? = identifierResources[identifier]

}
