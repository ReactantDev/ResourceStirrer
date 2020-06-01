package dev.reactant.resourcestirrer.stirring.tasks

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.rxjava3.core.Completable

@Component
class BaseResourceCopyingTask(
) : ResourceStirringTask, LifeCycleHook {
    override val name: String = javaClass.canonicalName
    override val dependsOn: List<ResourceStirringTask> = listOf()

    override fun onEnable() {
    }

    override fun start(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        workingDirectory.deleteRecursively()
        workingDirectory.mkdirs()
        val baseResourcePack = stirringPlan.baseResourcePack
        if (baseResourcePack.exists() && baseResourcePack.isDirectory) {
            baseResourcePack.copyRecursively(workingDirectory)
        }
    }

}
