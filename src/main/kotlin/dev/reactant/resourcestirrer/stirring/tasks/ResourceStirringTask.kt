package dev.reactant.resourcestirrer.stirring.tasks

import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.Completable
import java.io.File
import java.io.InputStream

interface ResourceStirringTask {
    fun start(stirringPlan: StirringPlan): Completable;
    val cacheFolder get() = File("${ResourceStirrer.configFolder}/.cache/packing")

    fun InputStream.outputTo(file: File) {
        if (!file.parentFile.exists()) file.parentFile.mkdirs()
        use { file.outputStream().use { out -> this.copyTo(out) } }
    }

    companion object {
        const val ASSETS_NAME_SPACE = "stirred";
    }
}
