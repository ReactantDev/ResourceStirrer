package dev.reactant.resourcestirrer.stirring.tasks

import dev.reactant.reactant.core.dependency.layers.SystemLevel
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.rxjava3.core.Completable
import java.io.File

interface ResourceStirringTask : SystemLevel {
    fun start(stirringPlan: StirringPlan): Completable;

    val resourcePackOutputPath get() = ResourceStirrer.resourcePackOutputPath
    val temporaryDirectory get() = File(ResourceStirrer.temporaryDirectoryPath)
    val workingDirectory get() = File("${ResourceStirrer.temporaryDirectoryPath}/packing")

    val name: String
    val dependsOn: List<ResourceStirringTask>

    companion object {
        const val ASSETS_NAME_SPACE = "stirred"
    }
}
