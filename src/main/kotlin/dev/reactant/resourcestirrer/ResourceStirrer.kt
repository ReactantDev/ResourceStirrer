package dev.reactant.resourcestirrer

import dev.reactant.reactant.core.ReactantPlugin
import org.apache.logging.log4j.LogManager
import org.bukkit.plugin.java.JavaPlugin


@ReactantPlugin(["dev.reactant.resourcestirrer"])
class ResourceStirrer : JavaPlugin() {

    companion object {
        const val configFolder = "plugins/Reactant/ResourceStirrer/";
        const val temporaryDirectoryPath = "$configFolder/output"
        const val resourcePackOutputPath = "$temporaryDirectoryPath/resources.zip"
        const val resourcePackHashOutputPath = "$temporaryDirectoryPath/resources.zip.sha1"
        val logger = LogManager.getLogger("ResourceStirrer")!!;
    }
}

