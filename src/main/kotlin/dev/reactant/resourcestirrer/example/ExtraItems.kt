package dev.reactant.resourcestirrer.example

import dev.reactant.resourcestirrer.resourcetype.item.byClassLoader
import dev.reactant.resourcestirrer.table.ItemResourcesTable
import dev.reactant.resourcestirrer.table.ResourcesTable
import org.bukkit.Material

@ResourcesTable
object ExtraItems : ItemResourcesTable("dev.reactant.resourcestirrer.example") {
    val FIRE_AXE = byClassLoader("items/fire_axe", "fire_axe", Material.WOODEN_AXE)
    val LIQUID = byClassLoader("items/liquid", "liquid", Material.NETHER_STAR)
}
