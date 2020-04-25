package dev.reactant.resourcestirrer.table

import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import dev.reactant.resourcestirrer.resourcetype.sound.SoundResource

abstract class SoundResourcesTable(
        identifierPrefix: String,
        _resourceLoader: ClassLoaderResourceLoader? = null
) : ClassLoaderResourceTable<SoundResource>(SoundResource::class, identifierPrefix, _resourceLoader) {

    abstract class SoundResourceGroup(parent: SoundResourcesTable, _identifierPrefix: String = "")
        : SoundResourcesTable(parent.getIdentifier(_identifierPrefix), parent.resourceLoader)
}
