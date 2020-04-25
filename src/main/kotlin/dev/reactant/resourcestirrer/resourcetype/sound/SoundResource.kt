package dev.reactant.resourcestirrer.resourcetype.sound

import dev.reactant.resourcestirrer.resourcetype.ResourceType
import org.bukkit.Location
import org.bukkit.entity.Player

interface SoundResource : ResourceType {
    val subtitle: String?
    val sounds: List<Sound>
    /**
     * Write the sound file to specified path
     */
    fun writeFile(path: String)

    interface Sound {
        val path: String
        val weight: Int
        val stream: Boolean
        val preload: Boolean
        val attenuationDistance: Float?
    }

    fun playSound(player: Player, location: Location, volume: Float = 1F, pitch: Float = 1F) =
            player.playSound(location, identifier, 1F, 1F)

    fun playSound(player: Player, volume: Float = 1F, pitch: Float = 1F) =
            playSound(player, player.location, volume, pitch)

    fun playSound(location: Location, volume: Float = 1F, pitch: Float = 1F) =
            location.world!!.playSound(location, identifier, volume, pitch)

    fun toDisplayInfo(): List<String> {
        return arrayListOf(
                "subtitle: $subtitle",
                "sounds:"
        ).also {
            it.addAll(sounds.map { "  - path: ${it.path}, weight: ${it.weight} ${if (it.stream) "[stream] " else ""}${if (it.preload) "[preload] " else ""}" })
        }
    }
}
