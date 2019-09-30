package dev.reactant.resourcestirrer.example

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.collector.ItemResourceManagingService

@Component
private class ItemRegister(
        private val itemResource: ItemResourceManagingService
) : LifeCycleHook {
    override fun onEnable() {
        itemResource.addItem(ExtraItems.LIQUID);
    }
}
