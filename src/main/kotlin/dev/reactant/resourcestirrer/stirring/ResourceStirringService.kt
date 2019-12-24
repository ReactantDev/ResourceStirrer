package dev.reactant.resourcestirrer.stirring

import com.google.gson.JsonParser
import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.reactant.core.component.lifecycle.LifeCycleInspector
import dev.reactant.reactant.core.dependency.injection.Inject
import dev.reactant.reactant.service.spec.config.Config
import dev.reactant.reactant.service.spec.config.ConfigService
import dev.reactant.reactant.service.spec.config.loadOrDefault
import dev.reactant.reactant.service.spec.parser.JsonParserService
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.collector.ItemResourceManagingService
import dev.reactant.resourcestirrer.config.ResourceStirrerConfig
import dev.reactant.resourcestirrer.config.StirrerMetaLock
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
        private val resourceStirrerConfig: Config<ResourceStirrerConfig>
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

    private val stirringTasks: ArrayList<ResourceStirringTask> = arrayListOf()

    fun registerStirringTask(stirringTask: ResourceStirringTask) {
        stirringTask.dependsOn.map { dependsOn -> dependsOn to stirringTasks.indexOf(dependsOn) }
                .let { dependenciesIndexes: List<Pair<ResourceStirringTask, Int>> ->
                    dependenciesIndexes.filter { it.second == -1 }.let { notFulFilledDependencies ->
                        if (notFulFilledDependencies.isNotEmpty()) {
                            throw IllegalStateException(
                                    "Stirring task ${stirringTask.name} should be register after " +
                                            "[${notFulFilledDependencies.map { it.first.name }.joinToString(",")}] registered")
                        }
                    }
                    stirringTasks.add(stirringTask)
                }
    }

    /**
     * Completable to stir the resources and update the resource pack
     */
    fun startStirring(skipOutput: Boolean = false): Completable {
        return Completable.fromAction { ResourceStirrer.logger.info("Start resource mapping") }
                .toSingle(::StirringPlan) // Create new stirring plan
                .flatMap(::prepareStirringPlan) // Fill in configuration
                .doOnSuccess { stirringPlan ->
                    // Remove previous allocated identifiers if conflict with first priority identifier
                    stirringPlan.stirrerMetaLock.content.itemResourceCustomMetaLock
                            .filter { stirringPlan.usedIdentifiers.contains(it.value) }
                            .forEach { stirringPlan.stirrerMetaLock.content.itemResourceCustomMetaLock.remove(it.key) }

                    // Add all registered item resource into plan
                    itemResourceService.identifierResources
                            .forEach { (identifier, resource) -> stirringPlan.addItemResource(resource, identifier) }

                    // Save changes on lock
                    stirringPlan.stirrerMetaLock.save().blockingAwait()

                }
                .doOnSuccess { _latestStirringPlan = it }
                .let {
                    if (!skipOutput) it.flatMap { stirringPlan ->
                        ResourceStirrer.logger.info("Start resource stirring...")
                        Single.fromCallable {
                            // stirring tasks
                            stirringTasks.map { task -> task.start(stirringPlan).blockingAwait() }
                        }
                    } else it
                }
                .ignoreElement();
    }

    /**
     * Load configs and history into stirring plan
     */
    private fun prepareStirringPlan(stirringPlan: StirringPlan) = configService
            .loadOrDefault(jsonParserService, configPath, ::ResourceStirrerConfig)
            .doOnSuccess { it.save().blockingAwait(); stirringPlan.resourceStirrerConfig = it }
            .doOnSuccess { stirringPlan.baseResourcePack = baseResourcePack }
            .flatMap { readBaseResourcePackUsedIdentifiers() }
            .doOnSuccess { stirringPlan.usedIdentifiers = it }
            .flatMap { configService.loadOrDefault(jsonParserService, lockPath, ::StirrerMetaLock) }
            .doOnSuccess { stirringPlan.stirrerMetaLock = it }
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


    private val lockPath get() = "${ResourceStirrer.configFolder}/stirrer-meta-lock.json"
    private val configPath get() = "${ResourceStirrer.configFolder}/config.json"
}
