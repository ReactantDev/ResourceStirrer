package dev.reactant.resourcestirrer

import dev.reactant.reactant.core.ReactantPlugin
import org.apache.logging.log4j.LogManager
import org.bukkit.plugin.java.JavaPlugin


@ReactantPlugin(["dev.reactant.resourcestirrer"])
class ResourceStirrer : JavaPlugin() {
    companion object {
        const val configFolder = "plugins/ResourceStirrer/";
        val logger = LogManager.getLogger("ResourceStirrer")!!;
    }
}
