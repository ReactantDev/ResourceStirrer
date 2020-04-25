package dev.reactant.resourcestirrer.ui

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.ui.ReactantUIService
import dev.reactant.reactant.ui.element.style.actual
import dev.reactant.reactant.ui.element.style.auto
import dev.reactant.reactant.ui.element.style.fillParent
import dev.reactant.reactant.ui.kits.ReactantUIItemElement
import dev.reactant.reactant.ui.kits.div
import dev.reactant.reactant.ui.kits.item
import dev.reactant.reactant.ui.kits.span
import dev.reactant.reactant.ui.query.getElementById
import dev.reactant.reactant.utils.content.item.itemStackOf
import dev.reactant.resourcestirrer.commands.ResourceStirrerPermission
import dev.reactant.resourcestirrer.resourcetype.item.ItemResource
import org.bukkit.Material
import org.bukkit.entity.Player

@Component
class StirredItemGalleryUI(
        private val uiService: ReactantUIService
) {
    fun showGallery(player: Player, itemResources: List<ItemResource>) {
        uiService.createUI(player, "Stirred Item Gallery: ${itemResources.size} items", 6) {
            view.setCancelModificationEvents(true)

            var currentIndex = 0

            fun update() {
                val itemResource = itemResources[currentIndex]
                view.getElementById<ReactantUIItemElement>("prev")!!.edit().apply {
                    displayItem = if (currentIndex - 1 >= 0) itemStackOf(Material.RED_WOOL) { itemMeta { setDisplayName("Previous") } } else itemStackOf(Material.AIR)
                }
                view.getElementById<ReactantUIItemElement>("next")!!.edit().apply {
                    displayItem = if (currentIndex + 1 <= itemResources.size - 1) itemStackOf(Material.GREEN_WOOL) { itemMeta { setDisplayName("Next") } } else itemStackOf(Material.AIR)
                }
                view.getElementById<ReactantUIItemElement>("item")!!.edit().apply {
                    displayItem = itemResource.similarItemStack.apply {
                        itemMeta = itemMeta!!.apply {
                            setDisplayName(itemResource.identifier)
                            lore = listOf(
                                    "Base Item:${itemResource.baseItem?.name}",
                                    "Allocated custom model data:${itemResource.allocatedCustomModelData}"
                            )
                        }
                    }
                }
                view.getElementById<ReactantUIItemElement>("info")!!.edit().apply {
                    displayItem = itemStackOf(Material.PAPER) { itemMeta { setDisplayName("Items: ${currentIndex + 1}/${itemResources.size}") } }
                }
            }

            div {
                size(fillParent, actual(5))
                padding(2, 4)
                item {
                    id = "item"
                    click.subscribe {
                        if (player.hasPermission(ResourceStirrerPermission.ADMIN.ITEM.GET.toString())
                                && !displayItem.type.isAir) {
                            player.inventory.addItem(displayItem.clone())
                        }
                    }
                }
            }
            div {
                size(fillParent, actual(1))

                span {
                    margin(auto)
                    size(3, 1)
                    item {
                        id = "prev"
                        click.subscribe {
                            if (currentIndex - 1 >= 0) {
                                currentIndex--
                                update()
                            }
                        }
                    }
                    item {
                        id = "info"
                    }
                    item {
                        id = "next"
                        click.subscribe {
                            if (currentIndex + 1 <= itemResources.size - 1) {
                                currentIndex++
                                update()
                            }
                        }
                    }
                }
            }

            update()
        }
    }
}
