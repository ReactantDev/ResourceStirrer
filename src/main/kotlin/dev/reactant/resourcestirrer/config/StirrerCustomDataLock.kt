package dev.reactant.resourcestirrer.config

class StirrerCustomDataLock {
    // ItemResource identifier   to   MaterialCustomData identifier (format: [MATERIAL_NAME]-CUSTOM_DATA)
    var itemResourceCustomDataLock: HashMap<String, String> = hashMapOf()
    var lastAllocatedCustomData: HashMap<String, Int> = hashMapOf()
}
