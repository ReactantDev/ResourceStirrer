package dev.reactant.resourcestirrer.table

import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import dev.reactant.resourcestirrer.resourcetype.ResourceType
import dev.reactant.resourcestirrer.resourcetype.item.ItemResource
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

abstract class ClassLoaderResourceTable<T : ResourceType>(
        val resourceTypeClass: KClass<T>,
        val identifierPrefix: String,
        private val _resourceLoader: ClassLoaderResourceLoader? = null
) : Iterable<T> {

    val resourceLoader get() = _resourceLoader ?: ClassLoaderResourceLoader(this.javaClass.classLoader)
    private val outputPrefix = identifierPrefix.let { if (it.isBlank()) "" else "$it-" }

    fun getIdentifier(suffix: String) = "$outputPrefix$suffix"


    override fun iterator(): Iterator<T> {
        val itemResourceType = ItemResource::class.createType()
        val multipleItemResourceType = Iterable::class.createType(listOf(KTypeProjection(KVariance.OUT, itemResourceType)))

        val objectInstances = this::class.nestedClasses
                .mapNotNull { it.objectInstance }

        val properties = this::class.declaredMemberProperties
                .filter { it.visibility == KVisibility.PUBLIC }
                .mapNotNull { it.getter.call(this) }

        return walk(objectInstances.union(properties).iterator()).iterator()
    }

    @Suppress("UNCHECKED_CAST")
    private fun walk(iterator: Iterator<*>): Set<T> {
        val result = HashSet<T>()

        iterator.forEach { value ->
            if (value != null) {
                when {
                    resourceTypeClass.isInstance(value) -> setOf(value as T)
                    value is Iterable<*> -> walk(value.iterator())
                    value is Map<*, *> -> walk(value.entries.iterator())
                    value is Array<*> -> walk(value.iterator())
                    else -> throw UnsupportedOperationException(
                            "${value.javaClass.canonicalName} is not a ${resourceTypeClass.simpleName}")
                }.let { result.addAll(it) }
            }
        }
        return result
    }

}
