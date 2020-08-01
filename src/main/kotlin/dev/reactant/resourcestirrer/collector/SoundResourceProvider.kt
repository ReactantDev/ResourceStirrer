package dev.reactant.resourcestirrer.collector

import dev.reactant.reactant.core.dependency.layers.SystemLevel
import dev.reactant.resourcestirrer.resourcetype.sound.SoundResource

interface SoundResourceProvider : SystemLevel {
    val soundResources: Iterable<SoundResource>
}
