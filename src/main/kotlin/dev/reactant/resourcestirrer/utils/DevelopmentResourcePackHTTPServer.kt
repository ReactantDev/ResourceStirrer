package dev.reactant.resourcestirrer.utils

import com.sun.net.httpserver.HttpServer
import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.reactant.core.dependency.injection.Inject
import dev.reactant.reactant.service.spec.config.Config
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.config.ResourceStirrerConfig
import java.io.File
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.file.Files
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Component
class DevelopmentResourcePackHTTPServer(
        @Inject("${ResourceStirrer.configFolder}/config.json")
        private val resourceStirrerConfig: Config<ResourceStirrerConfig>
) : LifeCycleHook {
    private var httpServer: HttpServer? = null
    private var httpThreadPool: ExecutorService? = null

    private val config get() = resourceStirrerConfig.content.development.developmentHTTPServer

    override fun onEnable() {
        if (config.enabled) {
            try {
                httpThreadPool = Executors.newFixedThreadPool(8)
                this.httpServer = HttpServer.create(InetSocketAddress(config.port), 0).apply {
                    createContext("/resources.zip") {
                        ResourceStirrer.logger.info("Resource pack request from : ${it.remoteAddress}")
                        val file = File(ResourceStirrer.resourcePackOutputPath)
                        if (!file.exists()) {
                            val msg = "Resource file not exist"
                            it.sendResponseHeaders(404, msg.length.toLong())
                            it.responseBody.use { out ->
                                out.write(msg.toByteArray())
                            }
                        } else {
                            it.sendResponseHeaders(200, file.length())
                            it.responseBody.use { out ->
                                Files.copy(file.toPath(), out)
                            }
                        }
                    }
                    start()
                    ResourceStirrer.logger.info("Development resource pack http server started at port ${config.port}")
                }
            } catch (e: BindException) {
                ResourceStirrer.logger.warn("Development cannot start because port ${config.port} is already listening")
                ResourceStirrer.logger.warn("Most of the time it is because /reload cannot shutdown the previous http server, you may ignore this message if resource pack server still working")
            }
        }
    }

    override fun onDisable() {
        ResourceStirrer.logger.info("Stopping development resource pack http server")
        httpServer?.stop(1)
        httpThreadPool?.shutdownNow()
    }

}
