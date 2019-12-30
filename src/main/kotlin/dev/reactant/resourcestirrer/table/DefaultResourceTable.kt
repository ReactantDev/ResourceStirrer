package dev.reactant.resourcestirrer.table

import dev.reactant.resourcestirrer.itemresource.byClassLoader
import org.bukkit.Material

@ResourcesTable
object DefaultResourceTable : ItemResourcesTable("default") {
    val BLANK = byClassLoader("textures/blank", "blank", Material.WOODEN_AXE)
}
