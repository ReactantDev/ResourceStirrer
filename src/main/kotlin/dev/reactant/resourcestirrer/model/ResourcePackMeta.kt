package dev.reactant.resourcestirrer.model

/**
 * Model class of resource pack meta file
 */
class ResourcePackMeta {
    var pack: PackInfo = PackInfo();

    class PackInfo {
        var pack_format: Int = 8;
        var description: String = "Resource stirrer generated resource pack";
    }
}
