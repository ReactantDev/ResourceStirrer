package dev.reactant.resourcestirrer.commands

import dev.reactant.reactant.extra.command.ReactantCommand
import picocli.CommandLine

@CommandLine.Command(
        name = "resourcestirrer",
        aliases = ["resstir", "ress"],
        mixinStandardHelpOptions = true,
        description = []
)
internal class ResourceStirrerCommand : ReactantCommand() {
    override fun execute() {
        requirePermission(ResourceStirrerPermission)
        showUsage()
    }
}
