package dev.reactant.resourcestirrer.commands


import dev.reactant.reactant.extra.command.ReactantCommand
import dev.reactant.reactant.utils.PatternMatchingUtils
import dev.reactant.reactant.utils.formatting.MultiColumns
import dev.reactant.resourcestirrer.collector.ItemResourceManagingService
import dev.reactant.resourcestirrer.ui.StirredItemGalleryUI
import org.bukkit.entity.Player
import picocli.CommandLine
import java.util.regex.Pattern

@CommandLine.Command(
        name = "item",
        aliases = ["i"],
        mixinStandardHelpOptions = true,
        description = ["Show the stirred items in the gallery"]
)
internal class StirredItemGalleryCommand(
        private val itemResourceManagingService: ItemResourceManagingService,
        private val stirredItemGalleryUI: StirredItemGalleryUI
) : ReactantCommand() {

    @CommandLine.Option(names = ["-t", "--text-list"],
            description = ["Show in text list form instead of gallery"])
    var textOnlyList: Boolean = false


    @CommandLine.Option(names = ["-p", "--pattern"], paramLabel = "REG_EXP",
            description = ["Filtering item resource identifier by RegExp"])
    var identifierPattern: Pattern? = null

    @CommandLine.Parameters(arity = "0..*", paramLabel = "IDENTIFIER",
            description = ["Filtering item resource identifier, wildcard is available"])
    var identifierWildcards: ArrayList<String> = arrayListOf()


    override fun execute() {
        requirePermission(ResourceStirrerPermission.LISTING.ITEM)

        val result = itemResourceManagingService.identifierResources
                .asSequence()
                .map { it.value }
                .filter { identifierPattern == null || identifierPattern!!.toRegex().matches(it.identifier) }
                .filter {
                    identifierWildcards.isEmpty() || identifierWildcards.any { wildcard ->
                        PatternMatchingUtils.matchWildcard(wildcard, it.identifier)
                    }
                }
                .sortedBy { it.identifier }

        if (textOnlyList) {
            val listTable = MultiColumns.create {
                column { maxLength = 60; overflowCutFromRight = false; }
                column { maxLength = 20 }
                column { maxLength = 20 }
            }
            result.forEach {
                listTable.rows.add(listOf(
                        it.identifier, it.rootBaseItem.name,
                        it.allocatedCustomModelData?.toString() ?: "/"
                ))
            }
            listTable.generate().forEach { stdout.out(it) }
        } else if (sender is Player) {
            result.filter { it.allocatedCustomModelData != null }.toList().let {
                if (it.isEmpty()) {
                    sender.sendMessage("No stirred item found")
                } else {
                    stirredItemGalleryUI.showGallery(sender as Player, it)
                }
            }
        } else {
            stderr.out("Only player can use this command without --text-list");
        }
    }

}
