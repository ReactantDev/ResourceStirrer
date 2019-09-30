package dev.reactant.resourcestirrer.config

class StirrerMetaLock {
    // ItemResource identifier   to   MaterialCustomMeta identifier (format: [MATERIAL_NAME]-CUSTOM_META)
    // Custom meta is custom model data of item meta
    var itemResourceCustomMetaLock: HashMap<String, String> = hashMapOf()
    var lastAllocatedCustomMeta: HashMap<String, Int> = hashMapOf()
}
