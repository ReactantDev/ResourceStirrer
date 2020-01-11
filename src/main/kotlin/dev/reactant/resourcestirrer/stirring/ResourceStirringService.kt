package dev.reactant.resourcestirrer.stirring

import com.google.gson.JsonParser
import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.reactant.core.component.lifecycle.LifeCycleInspector
import dev.reactant.reactant.core.dependency.injection.Inject
import dev.reactant.reactant.core.dependency.injection.components.Components
import dev.reactant.reactant.service.spec.config.Config
import dev.reactant.reactant.service.spec.config.ConfigService
import dev.reactant.reactant.service.spec.config.getOrDefault
import dev.reactant.reactant.service.spec.parser.JsonParserService
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.collector.ItemResourceManagingService
import dev.reactant.resourcestirrer.config.ResourceStirrerConfig
import dev.reactant.resourcestirrer.config.StirrerCustomDataLock
import dev.reactant.resourcestirrer.stirring.tasks.ResourceStirringTask
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.FileReader

@Component
class ResourceStirringService private constructor(
        private val itemResourceService: ItemResourceManagingService,
        private val configService: ConfigService,
        private val jsonParserService: JsonParserService,
        @Inject("${ResourceStirrer.configFolder}/config.json")
        private val resourceStirrerConfig: Config<ResourceStirrerConfig>,
        private val unsortedTasks: Components<ResourceStirringTask>
) : LifeCycleHook, LifeCycleInspector {


    private val parser = JsonParser();

    override fun onEnable() {
        val startAt = System.currentTimeMillis()
        if (resourceStirrerConfig.content.updateOnStart) {
            startStirring().blockingAwait()
            ResourceStirrer.logger.info("Update resource pack cost ${System.currentTimeMillis() - startAt} ms")
        } else {
            startStirring(true).blockingAwait()
            ResourceStirrer.logger.info("Update resource mapping cost ${System.currentTimeMillis() - startAt} ms")
        }
    }

    val latestStirringPlan get() = _latestStirringPlan;
    private var _latestStirringPlan: StirringPlan? = null;


    /**
     * Completable to stir the resources and update the resource pack
     */
    fun startStirring(skipOutput: Boolean = false): Completable {
        return Completable.fromAction { ResourceStirrer.logger.info("Start resource mapping") }
                .toSingle(::StirringPlan) // Create new stirring plan
                .flatMap(::prepareStirringPlan) // Fill in configuration
                .doOnSuccess { stirringPlan ->
                    // Remove previous allocated identifiers if conflict with first priority identifier
                    stirringPlan.stirrerCustomDataLock.content.itemResourceCustomDataLock
                            .filter { stirringPlan.usedIdentifiers.contains(it.value) }
                            .forEach { stirringPlan.stirrerCustomDataLock.content.itemResourceCustomDataLock.remove(it.key) }

                    // Add all registered item resource into plan
                    itemResourceService.identifierResources
                            .forEach { (identifier, resource) -> stirringPlan.addItemResource(resource, identifier) }

                    // Save changes on lock
                    stirringPlan.stirrerCustomDataLock.save().blockingAwait()

                }
                .doOnSuccess { _latestStirringPlan = it }
                .let {
                    return@let (if (!skipOutput) it.flatMap { stirringPlan ->
                        val stirringTaskDepth = hashMapOf<ResourceStirringTask, Int>()
                        fun getStirringTaskDepth(task: ResourceStirringTask): Int {
                            stirringTaskDepth[task] = stirringTaskDepth[task]
                                    ?: (task.dependsOn.map { getStirringTaskDepth(it) }.max() ?: -1) + 1
                            return stirringTaskDepth[task]!!
                        }

                        val stirringTasks = unsortedTasks.forEach { task -> getStirringTaskDepth(task) }
                        Single.fromCallable {
                            // stirring tasks
                            stirringTaskDepth.entries.sortedBy { it.value }.map { it.key }.map { task ->
                                ResourceStirrer.logger.info("Stirring task: ${task.name}")
                                task.start(stirringPlan).blockingAwait()
                            }
                        }
                    } else it)
                }
                .ignoreElement();
    }

    /**
     * Load configs and history into stirring plan
     */
    private fun prepareStirringPlan(stirringPlan: StirringPlan) = configService
            .getOrDefault(jsonParserService, configPath, ::ResourceStirrerConfig)
            .doOnSuccess { it.save().blockingAwait(); stirringPlan.resourceStirrerConfig = it }
            .doOnSuccess { stirringPlan.baseResourcePack = baseResourcePack }
            .flatMap { readBaseResourcePackUsedIdentifiers() }
            .doOnSuccess { stirringPlan.usedIdentifiers = it }
            .flatMap { configService.getOrDefault(jsonParserService, lockPath, ::StirrerCustomDataLock) }
            .doOnSuccess { stirringPlan.stirrerCustomDataLock = it }
            .map { stirringPlan }

    private fun readBaseResourcePackUsedIdentifiers(): Single<Set<String>> {
        return Single.fromCallable {
            val baseResourcePack = baseResourcePack;
            val resourcePackItemFolder = File("${baseResourcePack.absolutePath}/assets/minecraft/models/item");

            if (!resourcePackItemFolder.exists()) setOf()
            else (resourcePackItemFolder.listFiles() ?: arrayOf()).map { file ->
                FileReader(file).use { reader ->
                    val resourcePackItemModel = parser.parse(reader).asJsonObject;
                    resourcePackItemModel.getAsJsonArray("overrides")
                            .map { it.asJsonObject }
                            .filter { it.has("custom_model_data") }
                            .map { it.get("custom_model_data") }
                            .map { "${file.nameWithoutExtension}-$it" }
                }
            }.flatten().toSet()
        }
    }

    private val baseResourcePack: File get() = File("${ResourceStirrer.configFolder}/base/").also { if (!it.exists()) it.mkdir() }


    private val lockPath get() = "${ResourceStirrer.configFolder}/stirrer-model-data-lock.json"
    private val configPath get() = "${ResourceStirrer.configFolder}/config.json"
}
