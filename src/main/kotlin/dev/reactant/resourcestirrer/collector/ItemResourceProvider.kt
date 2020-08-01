package dev.reactant.resourcestirrer.collector

import dev.reactant.reactant.core.dependency.layers.SystemLevel
import dev.reactant.resourcestirrer.resourcetype.item.ItemResource

interface ItemResourceProvider : SystemLevel {
    val itemResources: Iterable<ItemResource>
}
