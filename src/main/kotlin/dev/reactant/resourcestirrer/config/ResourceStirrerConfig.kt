package dev.reactant.resourcestirrer.config

class ResourceStirrerConfig {
    var customDataRange = ResourceStirrerCustomDataRange()
    var updateOnStart = false

    var uglify = true

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

    class ResourceStirrerCustomDataRange {
        var min = 100
    }
}
