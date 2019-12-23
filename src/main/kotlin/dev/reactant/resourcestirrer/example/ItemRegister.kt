package dev.reactant.resourcestirrer.example

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.resourcestirrer.collector.ItemResourcesTableService

@Component
private class ItemRegister(
        private val itemResourcesTableService: ItemResourcesTableService
) : LifeCycleHook {
    override fun onEnable() {
        itemResourcesTableService.addTable(ExtraItems)
    }
}
