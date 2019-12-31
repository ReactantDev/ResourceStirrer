package dev.reactant.resourcestirrer.table

import dev.reactant.resourcestirrer.itemresource.ItemResource
import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

abstract class ItemResourcesTable(
        val identifierPrefix: String,
        private val _resourceLoader: ClassLoaderResourceLoader? = null
) : Iterable<ItemResource> {
    val resourceLoader get() = _resourceLoader ?: ClassLoaderResourceLoader(this.javaClass.classLoader)
    private val outputPrefix = identifierPrefix.let { if (it.isBlank()) "" else "$it-" }

    fun getIdentifier(suffix: String) = "$outputPrefix$suffix"

    abstract class ItemResourcesGroup(parent: ItemResourcesTable, _identifierPrefix: String = "")
        : ItemResourcesTable(parent.getIdentifier(_identifierPrefix), parent.resourceLoader)

    /**
     * Explore the fields of the table by reflection
     */
    override fun iterator(): Iterator<ItemResource> {
        val itemResourceType = ItemResource::class.createType()
        val multipleItemResourceType = Iterable::class.createType(listOf(KTypeProjection(KVariance.OUT, itemResourceType)))

        val objectInstances = this::class.nestedClasses
                .mapNotNull { it.objectInstance }

        val properties = this::class.declaredMemberProperties
                .mapNotNull { it.getter.call(this) }

        return walk(objectInstances.union(properties).iterator()).iterator()
    }

    private fun walk(iterator: Iterator<*>): Set<ItemResource> {
        val result = HashSet<ItemResource>()
        iterator.forEach { value ->
            when (value) {
                is ItemResource -> setOf(value)
                is Iterable<*> -> walk(value.iterator())
                is Map<*, *> -> walk(value.entries.iterator())
                is Array<*> -> walk(value.iterator())
                else -> throw UnsupportedOperationException(
                        "${value?.javaClass?.canonicalName} is not a supported type in ItemResourceTable")
            }.let { result.addAll(it) }
        }
        return result
    }
}
