package dev.reactant.resourcestirrer.stirring.tasks

import dev.reactant.reactant.core.component.Component
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.Completable

@Component
internal class BaseResourceCopyingTask : ResourceStirringTask {
    override fun start(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        ResourceStirrer.logger.info("Packing resource pack...")
        cacheFolder.deleteRecursively()

        cacheFolder.mkdirs()

        val baseResourcePack = stirringPlan.baseResourcePack
        if (baseResourcePack.exists() && baseResourcePack.isDirectory) {
            baseResourcePack.copyRecursively(cacheFolder)
        }
    }
}
