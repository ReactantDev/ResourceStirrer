package dev.reactant.resourcestirrer.commands

import dev.reactant.reactant.extra.command.ReactantCommand
import dev.reactant.resourcestirrer.stirring.ResourceStirringService
import picocli.CommandLine

@CommandLine.Command(
        name = "update",
        aliases = [],
        mixinStandardHelpOptions = true,
        description = ["Update the generated resource pack"]
)
internal class ResourceStirrerPackUpdateCommand(
        private val resourceStirringService: ResourceStirringService
) : ReactantCommand() {
    @CommandLine.Option(names = ["-a", "--async"],
            description = ["Run without blocking main thread"])
    var async: Boolean = false

    override fun run() {
        requirePermission(ResourceStirrerPermission.ADMIN.PACK.UPDATE)

        val startAt = System.currentTimeMillis()
        resourceStirringService.startStirring().let {
            it.doOnError { e ->
                e.printStackTrace()
                sender.sendMessage("Exception occurred when executing resource packing tasks")
            }.let { tasks ->
                val sendMessage = {
                    sender.sendMessage("Resources packing tasks executed successfully," +
                            " cost ${System.currentTimeMillis() - startAt} ms")
                }
                if (async) tasks.subscribe { sendMessage() }
                else tasks.blockingAwait().let { sendMessage() }
            }
        }
    }
}
