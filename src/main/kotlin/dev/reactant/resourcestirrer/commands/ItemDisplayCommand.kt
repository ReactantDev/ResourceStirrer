package dev.reactant.resourcestirrer.commands


import dev.reactant.reactant.core.ReactantCore
import dev.reactant.reactant.extra.command.ReactantCommand
import dev.reactant.reactant.ui.ReactantUIService
import dev.reactant.reactant.ui.kits.item
import dev.reactant.reactant.utils.content.item.createItemStack
import dev.reactant.resourcestirrer.collector.ItemResourceManagingService
import dev.reactant.resourcestirrer.stirring.ResourceStirringService
import org.bukkit.entity.Player
import picocli.CommandLine

@CommandLine.Command(
        name = "ls",
        aliases = ["list"],
        mixinStandardHelpOptions = true,
        description = ["Listing all Component"]
)
internal class ItemDisplayCommand(
        private val itemResourceManagingService: ItemResourceManagingService,
        private val resourceStirringService: ResourceStirringService,
        private val uiService: ReactantUIService) : ReactantCommand() {
    override fun run() {
        requirePermission(ResourceStirrerPermission.DISPLAY_LIST.ITEM)
        if (sender !is Player) stderr.out("Only player can use this command");
        val player = sender as Player;
        uiService.createUI(player, "Items") {
            itemResourceManagingService.identifierResources.forEach { (identifier,resource)->
                val customMeta = resourceStirringService.lastStirringPlan?.stirrerMetaLock?.content?.itemResourceCustomMetaLock?.get(identifier);
                if (customMeta != null) {
                    item {
                        displayItem = createItemStack(resource.rootBaseItem) {
                            itemMeta {
                                setCustomModelData(customMeta.split("-").last().toInt())
                            }
                        }
                    }
                }
            }
        }.show(player)
    }

}
