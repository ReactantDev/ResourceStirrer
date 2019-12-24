package dev.reactant.resourcestirrer.table

import dev.reactant.resourcestirrer.itemresource.ItemResource
import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.jvmErasure

abstract class ItemResourcesTable(
        val identifierPrefix: String,
        private val _resourceLoader: ClassLoaderResourceLoader? = null
) : Iterable<ItemResource> {
    val resourceLoader get() = _resourceLoader ?: ClassLoaderResourceLoader(this.javaClass.classLoader)

    class TableHeader(val identifierPrefix: String, val resourceLoader: ClassLoaderResourceLoader?)

    open val tableHeader: TableHeader get() = TableHeader(identifierPrefix, resourceLoader)

    abstract class ItemResourcesGroup(_tableHeader: TableHeader, _identifierPrefix: String? = "")
        : ItemResourcesTable(_tableHeader.identifierPrefix + _identifierPrefix, _tableHeader.resourceLoader) {
        override val tableHeader: TableHeader get() = TableHeader(identifierPrefix, resourceLoader)
    }


    /**
     * Explore the fields of the table by reflection
     */
    override fun iterator(): Iterator<ItemResource> {
        val itemResourceType = ItemResource::class.createType()
        val multipleItemResourceType = Iterable::class.createType(listOf(KTypeProjection(KVariance.OUT, itemResourceType)))

        val multipleItemResource = this::class.declaredMemberProperties
                .filter { it.returnType.isSubtypeOf(multipleItemResourceType) }
                .map { it.getter.call(this) as Iterable<ItemResource> }
                .flatten()

        val singleItemResource = this::class.declaredMemberProperties
                .filter { it.returnType.isSubtypeOf(itemResourceType) }
                .map { it.getter.call(this) as ItemResource }


        val objectMultipleItemResource = this::class.nestedClasses
                .filter { it.isSubclassOf(multipleItemResourceType.jvmErasure) }
                .map { it.objectInstance as Iterable<ItemResource> }
                .flatten()

        val objectSingleItemResource = this::class.nestedClasses
                .filter { it.isSubclassOf(itemResourceType.jvmErasure) }
                .map { it.objectInstance as ItemResource }

        return multipleItemResource.union(singleItemResource).union(objectMultipleItemResource).union(objectSingleItemResource).iterator()
    }
}
