package dev.reactant.resourcestirrer.commands

import dev.reactant.reactant.extra.command.ReactantCommand
import dev.reactant.reactant.utils.formatting.MultiColumns
import dev.reactant.resourcestirrer.stirring.ResourceStirringService
import picocli.CommandLine

@CommandLine.Command(
        name = "fixtool",
        aliases = ["fix"],
        mixinStandardHelpOptions = true,
        description = ["Fix tools that used for fixing reference lost problem"]
)
internal class ResourceStirrerFixToolCommand(
        private val resourceStirringService: ResourceStirringService
) : ReactantCommand() {
    override fun execute() {
        requirePermission(ResourceStirrerPermission.ADMIN.FIX)

        val lostReferenceCustomData = resourceStirringService.latestStirringPlan?.lostReferenceCustomData
        if (lostReferenceCustomData == null) {
            stdout.out("There have no recent stirring plan generated, is there any failed stirring task?")
        } else if (lostReferenceCustomData.isEmpty()) {
            stdout.out("There have nothing to fix")
        } else {
            MultiColumns.create {
                column { maxLength = 50; overflowCutFromRight = false }
                column { maxLength = 50 }
            }.also {
                lostReferenceCustomData.forEach { identifier, customData ->
                    it.rows.add(listOf(identifier, customData))
                }
            }.generate().forEach { stdout.out(it) }
        }
    }
}
