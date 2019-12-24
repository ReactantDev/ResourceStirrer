package dev.reactant.resourcestirrer.collector

import dev.reactant.reactant.core.component.Component
import dev.reactant.resourcestirrer.annotation.ItemResourcesTable

/**
 * A service that use to explore the variables of object which extended ItemResourcesTable, and register the item resources
 */
@Component
class ItemResourcesTableService(
        private val itemResourceManagingService: ItemResourceManagingService
) {
    fun addTable(table: ItemResourcesTable) {
        table.let { itemResourceManagingService.addItem(*it.toSet().toTypedArray()) }
    }
}

