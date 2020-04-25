package dev.reactant.resourcestirrer.table

import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import dev.reactant.resourcestirrer.resourcetype.item.ItemResource

abstract class ItemResourcesTable(
        identifierPrefix: String,
        _resourceLoader: ClassLoaderResourceLoader? = null
) : ClassLoaderResourceTable<ItemResource>(ItemResource::class, identifierPrefix, _resourceLoader) {

    abstract class ItemResourcesGroup(parent: ItemResourcesTable, _identifierPrefix: String = "")
        : ItemResourcesTable(parent.getIdentifier(_identifierPrefix), parent.resourceLoader)
}
