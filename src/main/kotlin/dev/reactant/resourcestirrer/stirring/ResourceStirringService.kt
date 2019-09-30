package dev.reactant.resourcestirrer.stirring

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
import java.io.File

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
    override fun afterBulkActionComplete(action: LifeCycleControlAction) {
        if (action != LifeCycleControlAction.Initialize) return
        startStirring().blockingAwait()
    }

    var lastStirringPlan: StirringPlan? = null;

    fun startStirring(): Completable {
        return Single.fromCallable(::StirringPlan)
                .flatMap(::prepareStirringPlan)
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
                .doOnSuccess { lastStirringPlan = it }
                .flatMap { stirringPlan ->
                    Single.fromCallable {
                        // stirring tasks
                        arrayOf(
                                baseResourceCopyingTask,
                                itemResourceWritingTask,
                                defaultMetaGeneratingTask,
                                packingTask
                        ).map { task -> task.start(stirringPlan).blockingAwait() }
                    }
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
        val baseResourcePack = baseResourcePack;
        if (!baseResourcePack.exists()) return Single.just(setOf());
        if (!baseResourcePack.isDirectory) {
            ResourceStirrer.logger.warn("Base resource pack must be a folder, ignored.")
            return Single.just(setOf());
        }
        return Single.just(setOf())
        //todo: read all model file
    }

    private val baseResourcePack: File get() = File("${ResourceStirrer.configFolder}/BaseResource/")


    private val lockPath get() = "${ResourceStirrer.configFolder}/stirrer-meta-lock.json"
    private val configPath get() = "${ResourceStirrer.configFolder}/config.json"
}
