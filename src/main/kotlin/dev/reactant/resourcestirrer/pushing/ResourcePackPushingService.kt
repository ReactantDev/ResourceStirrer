package dev.reactant.resourcestirrer.pushing

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.reactant.core.dependency.injection.Inject
import dev.reactant.reactant.service.spec.config.Config
import dev.reactant.reactant.service.spec.dsl.register
import dev.reactant.reactant.service.spec.server.EventService
import dev.reactant.reactant.service.spec.server.SchedulerService
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.config.ResourceStirrerConfig
import dev.reactant.resourcestirrer.stirring.StirringPlan
import dev.reactant.resourcestirrer.stirring.tasks.ResourcePackingTask
import dev.reactant.resourcestirrer.stirring.tasks.ResourceStirringTask
import io.reactivex.rxjava3.core.Completable
import org.apache.commons.codec.binary.Hex
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import java.io.File
import java.util.*

@Component
class ResourcePackPushingService(
        private val event: EventService,
        @Inject("${ResourceStirrer.configFolder}/config.json")
        private val resourceStirrerConfig: Config<ResourceStirrerConfig>,
        private val schedulerService: SchedulerService,
        packingTask: ResourcePackingTask
) : ResourceStirringTask, LifeCycleHook {
    private val config get() = resourceStirrerConfig.content.development.autoPushing
    private var currentSha1: ByteArray? = File(ResourceStirrer.resourcePackHashOutputPath).let {
        if (it.exists()) Scanner(it).use { it.nextLine() }.let { Hex.decodeHex(it) }
        else null
    }

    override fun onEnable() {
        if (config.pushWhenPlayerJoin) {
            register(event) {
                PlayerJoinEvent::class.observable().subscribe {
                    if (currentSha1 == null) {
                        ResourceStirrer.logger.warn("Resource pack sha1 not found, " +
                                "resource pack will not be pushed to player until updated, " +
                                "set autoPushing.pushWhenPlayerJoin to false in ReesourceStirrer config " +
                                "if you are not willing to push resource pack when player join")
                    } else {
                        schedulerService.next().subscribe {
                            it.player.setResourcePack(config.resourcePackUrl, currentSha1!!)
                        }
                    }
                }
            }
        }
    }


    override fun start(stirringPlan: StirringPlan): Completable = Completable.fromAction {
        this.currentSha1 = stirringPlan.resourcePackSha1
        if (config.pushAfterUpdate) pushToAllPlayer()
    }

    private fun pushToAllPlayer() {
        Bukkit.getOnlinePlayers().forEach {
            it.setResourcePack(config.resourcePackUrl, currentSha1!!)
            schedulerService.timer(20).subscribe {
                it.setResourcePack(config.resourcePackUrl, currentSha1!!)
            }
        }
    }

    override val name: String = javaClass.canonicalName
    override val dependsOn: List<ResourceStirringTask> = listOf(packingTask)


}
