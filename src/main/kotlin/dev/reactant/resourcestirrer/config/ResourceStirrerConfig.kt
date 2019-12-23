package dev.reactant.resourcestirrer.config

class ResourceStirrerConfig {
    var customMetaRange = ResourceStirrerCustomMetaRange()
    var updateOnAllServicesEnabled = false

    var development = DevelopmentConfig()

    class DevelopmentConfig {
        var developmentHTTPServer = DevelopmentHTTPServerConfig()
        var autoPushing = AutoPushingResourcePackConfig()
    }

    class AutoPushingResourcePackConfig {
        var pushAfterUpdate = false
        var pushWhenPlayerJoin = false
        var resourcePackUrl = "http://localhost:8499/resources.zip"
    }

    class DevelopmentHTTPServerConfig {
        var enabled = false
        var port: Int = 8499
        var threads = 8
    }

    class ResourceStirrerCustomMetaRange {
        var min = 100
    }
}
