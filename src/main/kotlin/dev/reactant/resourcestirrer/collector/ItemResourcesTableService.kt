package dev.reactant.resourcestirrer.collector

import dev.reactant.reactant.core.component.Component
import dev.reactant.resourcestirrer.annotation.ItemResourcesTable
import dev.reactant.resourcestirrer.itemresource.ItemResource
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
        table::class.declaredMemberProperties
                .filter { it.returnType.isSubtypeOf(ItemResource::class.createType()) }
                .map { it.getter.call(table) as ItemResource }
                .let { itemResourceManagingService.addItem(*it.toTypedArray()) }
    }
}

