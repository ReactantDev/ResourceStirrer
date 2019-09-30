package dev.reactant.resourcestirrer.example

import dev.reactant.resourcestirrer.ItemResource
import dev.reactant.resourcestirrer.ModelItemResource
import org.bukkit.Material

internal enum class ExtraItems(val itemResource: ModelItemResource) : ItemResource by itemResource {

    LIQUID(object : ModelItemResource(
            "liquid",
            "dev.reactant.resourcestirrer.example:liquid",
            Material.NETHER_STAR) {}),
}

