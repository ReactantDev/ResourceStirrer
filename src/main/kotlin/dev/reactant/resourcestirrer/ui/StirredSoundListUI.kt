package dev.reactant.resourcestirrer.ui

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.ui.ReactantUIService
import dev.reactant.reactant.ui.element.style.actual
import dev.reactant.reactant.ui.element.style.auto
import dev.reactant.reactant.ui.element.style.fillParent
import dev.reactant.reactant.ui.kits.ReactantUIItemElement
import dev.reactant.reactant.ui.kits.container.ReactantUIContainerElement
import dev.reactant.reactant.ui.kits.div
import dev.reactant.reactant.ui.kits.item
import dev.reactant.reactant.ui.kits.span
import dev.reactant.reactant.ui.query.getElementById
import dev.reactant.reactant.utils.content.item.itemStackOf
import dev.reactant.resourcestirrer.resourcetype.sound.SoundResource
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.ceil

@Component
class StirredSoundListUI(
        private val uiService: ReactantUIService
) {
    fun showList(player: Player, soundResources: List<SoundResource>) {
        uiService.createUI(player, "Stirred Sound List: ${soundResources.size} sounds", 6) {
            view.setCancelModificationEvents(true)

            var currentIndex = 0
            val totalPage = ceil(soundResources.size / 45.0).toInt()

            fun update() {
                val displaying = soundResources.drop(currentIndex * 45).take(45)
                view.getElementById<ReactantUIItemElement>("prev")!!.edit().apply {
                    slotItem = if (currentIndex - 1 >= 0) itemStackOf(Material.RED_WOOL) { itemMeta { setDisplayName("Previous") } } else itemStackOf(Material.AIR)
                }
                view.getElementById<ReactantUIItemElement>("next")!!.edit().apply {
                    slotItem = if (currentIndex + 1 < totalPage) itemStackOf(Material.GREEN_WOOL) { itemMeta { setDisplayName("Next") } } else itemStackOf(Material.AIR)
                }

                view.getElementById<ReactantUIContainerElement>("sounds")!!.let {
                    it.children.clear()
                    it.edit().apply {
                        displaying.forEach { soundResource ->
                            item {
                                slotItem = itemStackOf(Material.NOTE_BLOCK) {
                                    itemMeta {
                                        setDisplayName(soundResource.identifier)
                                        lore = soundResource.toDisplayInfo()
                                    }
                                }

                                onClick.subscribe { soundResource.playSound(it.player) }
                            }
                        }
                    }
                }

                view.getElementById<ReactantUIItemElement>("info")!!.edit().apply {
                    slotItem = itemStackOf(Material.PAPER) { itemMeta { setDisplayName("Pages: ${currentIndex + 1}/$totalPage") } }
                }
            }

            div {
                id = "sounds"
                size(fillParent, actual(5))
            }
            div {
                size(fillParent, actual(1))

                span {
                    margin(auto)
                    size(3, 1)
                    item {
                        id = "prev"
                        onClick.subscribe {
                            if (currentIndex - 1 >= 0) {
                                currentIndex--
                                update()
                            }
                        }
                    }
                    item { id = "info" }
                    item {
                        id = "next"
                        onClick.subscribe {
                            if (currentIndex + 1 < totalPage) {
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
