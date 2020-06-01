package dev.reactant.resourcestirrer.stirring.tasks

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.reactant.service.spec.config.ConfigService
import dev.reactant.reactant.service.spec.parser.JsonParserService
import dev.reactant.resourcestirrer.model.ResourcePackMeta
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.rxjava3.core.Completable
import java.io.File

@Component
class ResourcePackDefaultMetaGeneratingTask(
        private val configService: ConfigService,
        private val jsonParserService: JsonParserService,
        itemResourceWritingTask: ItemResourceWritingTask
) : ResourceStirringTask, LifeCycleHook {
    override val name: String = javaClass.canonicalName
    override val dependsOn: List<ResourceStirringTask> = listOf(itemResourceWritingTask)

    override fun onEnable() {
    }

    override fun start(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        val mcmetaFile = File("${workingDirectory.absolutePath}/pack.mcmeta")
        if (!mcmetaFile.exists()) {
            configService.getOrDefault(jsonParserService, ResourcePackMeta::class, mcmetaFile.absolutePath, ::ResourcePackMeta).blockingGet().save().blockingAwait()
        }
    }
}

