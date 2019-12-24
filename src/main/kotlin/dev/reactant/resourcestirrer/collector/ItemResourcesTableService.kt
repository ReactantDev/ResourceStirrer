package dev.reactant.resourcestirrer.collector

import dev.reactant.reactant.core.component.Component
import dev.reactant.resourcestirrer.annotation.ItemResourcesTable
import dev.reactant.resourcestirrer.itemresource.ItemResource
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf

/**
 * A service that use to explore the variables of object which extended ItemResourcesTable, and register the item resources
 */
@Component
class ItemResourcesTableService(
        val itemResourceManagingService: ItemResourceManagingService
) {

    fun addTable(table: ItemResourcesTable) {
        val itemResourceType = ItemResource::class.createType()
        val multipleItemResourceType = Iterable::class.createType(listOf(KTypeProjection(KVariance.OUT, itemResourceType)))
        val multipleItemResource = table::class.declaredMemberProperties
                .filter { it.returnType.isSubtypeOf(multipleItemResourceType) }
                .map { it.getter.call(table) as Iterable<ItemResource> }
                .flatten()
        val singleItemResource = table::class.declaredMemberProperties
                .filter { it.returnType.isSubtypeOf(itemResourceType) }
                .map { it.getter.call(table) as ItemResource }

        multipleItemResource.union(singleItemResource)
                .let { itemResourceManagingService.addItem(*it.toTypedArray()) }
    }
}

