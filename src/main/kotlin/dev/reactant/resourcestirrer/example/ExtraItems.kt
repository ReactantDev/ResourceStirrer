package dev.reactant.resourcestirrer.example

import dev.reactant.resourcestirrer.ItemResource
import dev.reactant.resourcestirrer.ModelItemResource
import org.bukkit.Material

internal enum class ExtraItems(val itemResource: ModelItemResource) : ItemResource by itemResource {

    LIQUID(object : ModelItemResource(
            "liquid",
            "f70a4208-e450-4bf0-a3a5-f786e1df5650",
            Material.NETHER_STAR) {}),
}

