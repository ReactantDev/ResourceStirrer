package dev.reactant.resourcestirrer.commands

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.reactant.extra.command.PicocliCommandService
import dev.reactant.reactant.service.spec.dsl.register
import dev.reactant.reactant.ui.ReactantUIService
import dev.reactant.resourcestirrer.collector.ItemResourceManagingService
import dev.reactant.resourcestirrer.stirring.ResourceStirringService
import dev.reactant.resourcestirrer.ui.StirredItemGalleryUI

@Component
internal class ResourceStirrerCommandRegister(
        private val commandService: PicocliCommandService,
        private val itemResourceManagingService: ItemResourceManagingService,
        private val uiService: ReactantUIService,
        private val stirredItemGalleryUI: StirredItemGalleryUI,
        private val resourceStirringService: ResourceStirringService
) : LifeCycleHook {
    override fun onEnable() {
        register(commandService) {
            command(::ResourceStirrerCommand) {
                command({ StirredItemGalleryCommand(itemResourceManagingService, stirredItemGalleryUI) })
                command({ ResourceStirrerPackUpdateCommand(resourceStirringService) })
            }
        }
    }
}
