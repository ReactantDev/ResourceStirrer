package dev.reactant.resourcestirrer.stirring.tasks

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.service.spec.config.ConfigService
import dev.reactant.reactant.service.spec.parser.JsonParserService
import dev.reactant.resourcestirrer.config.ResourcePackMeta
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.Completable
import java.io.File

@Component
internal class ResourcePackDefaultMetaGeneratingTask(
        private val configService: ConfigService,
        private val jsonParserService: JsonParserService
) : ResourceStirringTask {
    override fun start(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        val mcmetaFile = File("${cacheFolder.absolutePath}/pack.mcmeta")
        if (!mcmetaFile.exists()) {
            configService.loadOrDefault(jsonParserService, ResourcePackMeta::class, mcmetaFile.absolutePath, ::ResourcePackMeta).blockingGet().save().blockingAwait()
        }
    }
}

