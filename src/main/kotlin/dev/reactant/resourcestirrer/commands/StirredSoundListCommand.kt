package dev.reactant.resourcestirrer.commands


import dev.reactant.reactant.extra.command.ReactantCommand
import dev.reactant.reactant.utils.PatternMatchingUtils
import dev.reactant.resourcestirrer.collector.SoundResourceManagingService
import dev.reactant.resourcestirrer.ui.StirredSoundListUI
import org.bukkit.entity.Player
import picocli.CommandLine
import java.util.regex.Pattern

@CommandLine.Command(
        name = "sound",
        aliases = ["s"],
        mixinStandardHelpOptions = true,
        description = ["Show the stirred sounds in the list"]
)
internal class StirredSoundListCommand(
        private val soundResourceManagingService: SoundResourceManagingService,
        private val stirredSoundListUI: StirredSoundListUI
) : ReactantCommand() {

    @CommandLine.Option(names = ["-t", "--text-list"],
            description = ["Show in text list form instead of ui list"])
    var textOnlyList: Boolean = false


    @CommandLine.Option(names = ["-p", "--pattern"], paramLabel = "REG_EXP",
            description = ["Filtering sound resource identifier by RegExp"])
    var identifierPattern: Pattern? = null

    @CommandLine.Parameters(arity = "0..*", paramLabel = "IDENTIFIER",
            description = ["Filtering sound resource identifier, wildcard is available"])
    var identifierWildcards: ArrayList<String> = arrayListOf()


    override fun execute() {
        requirePermission(ResourceStirrerPermission.LISTING.SOUND)

        val result = soundResourceManagingService.identifierResources
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
            result.map { it.identifier }.forEach { stdout.out(it) }
        } else if (sender is Player) {
            result.toList().let {
                if (it.isEmpty()) {
                    sender.sendMessage("No sound found")
                } else {
                    stirredSoundListUI.showList(sender as Player, it)
                }
            }
        } else {
            stderr.out("Only player can use this command without --text-list");
        }
    }

}
