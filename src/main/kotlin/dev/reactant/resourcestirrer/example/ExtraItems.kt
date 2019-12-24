package dev.reactant.resourcestirrer.example

import dev.reactant.resourcestirrer.annotation.ItemResourcesTable
import dev.reactant.resourcestirrer.itemresource.byClassLoader
import org.bukkit.Material


object ExtraItems : ItemResourcesTable("dev.reactant.resourcestirrer.example") {
    val FIRE_AXE = byClassLoader("items/fire_axe", "fire_axe", Material.WOODEN_AXE)
    val LIQUID = byClassLoader("items/liquid", "liquid", Material.NETHER_STAR)

}
