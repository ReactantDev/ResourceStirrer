package dev.reactant.resourcestirrer.config

class ResourceStirrerConfig {
    var customMetaRange = ResourceStirrerCustomMetaRange()

    var outputPath = "resources.zip";

    /**
     * false to run in other thread instead of main thread
     */
    var blockingThread = false;

    class ResourceStirrerCustomMetaRange {
        var min = 100
    }
}
