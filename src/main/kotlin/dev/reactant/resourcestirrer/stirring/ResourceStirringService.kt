package dev.reactant.resourcestirrer.stirring

import com.google.gson.JsonParser
import dev.reactant.reactant.core.ReactantCore
import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleControlAction
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.reactant.core.component.lifecycle.LifeCycleInspector
import dev.reactant.reactant.service.spec.config.ConfigService
import dev.reactant.reactant.service.spec.config.loadOrDefault
import dev.reactant.reactant.service.spec.parser.JsonParserService
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.collector.ItemResourceManagingService
import dev.reactant.resourcestirrer.config.ResourceStirrerConfig
import dev.reactant.resourcestirrer.config.StirrerMetaLock
import dev.reactant.resourcestirrer.stirring.tasks.BaseResourceCopyingTask
import dev.reactant.resourcestirrer.stirring.tasks.ItemResourceWritingTask
import dev.reactant.resourcestirrer.stirring.tasks.ResourcePackDefaultMetaGeneratingTask
import dev.reactant.resourcestirrer.stirring.tasks.ResourcePackingTask
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.io.FileReader

@Component
internal class ResourceStirringService(
        private val itemResourceService: ItemResourceManagingService,
        private val configService: ConfigService,
        private val jsonParserService: JsonParserService,
        private val baseResourceCopyingTask: BaseResourceCopyingTask,
        private val itemResourceWritingTask: ItemResourceWritingTask,
        private val defaultMetaGeneratingTask: ResourcePackDefaultMetaGeneratingTask,
        private val packingTask: ResourcePackingTask
) : LifeCycleHook, LifeCycleInspector {
    private val parser = JsonParser();
    override fun afterBulkActionComplete(action: LifeCycleControlAction) {
        if (action != LifeCycleControlAction.Initialize) return
        startStirring().blockingAwait()
    }


    fun test() {
         configService.loadOrDefault(jsonParserService, "path.json", ::ResourceStirrerConfig)
                 .subscribeOn(Schedulers.io())
                 .observeOn(ReactantCore.mainThreadScheduler)
                 .subscribe { config->
                     config.content
                     // do what u wanna do
                 }
    }

    val latestStirringPlan get() = _latestStirringPlan;
    private var _latestStirringPlan: StirringPlan? = null;

    val stirringCompleteHook = PublishSubject.create<StirringPlan>()

    private val stirringTasks = arrayListOf(
            baseResourceCopyingTask,
            itemResourceWritingTask,
            defaultMetaGeneratingTask,
            packingTask
    );

    fun startStirring(): Completable {
        return Completable.fromAction { ResourceStirrer.logger.info("Start resource stirring") }
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
                .flatMap { stirringPlan ->
                    Single.fromCallable {
                        // stirring tasks
                        stirringTasks.map { task -> task.start(stirringPlan).blockingAwait() }
                    }
                }
                .doFinally { stirringCompleteHook.onNext(_latestStirringPlan!!) }
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

            if (resourcePackItemFolder.exists()) setOf()
            else resourcePackItemFolder.listFiles()!!.map { file ->
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

    private val baseResourcePack: File get() = File("${ResourceStirrer.configFolder}/BaseResource/")


    private val lockPath get() = "${ResourceStirrer.configFolder}/stirrer-meta-lock.json"
    private val configPath get() = "${ResourceStirrer.configFolder}/config.json"
}
