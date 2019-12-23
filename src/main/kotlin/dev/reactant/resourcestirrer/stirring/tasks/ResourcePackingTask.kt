package dev.reactant.resourcestirrer.stirring.tasks

import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.reactant.extra.file.ReactantTextFileWriterService
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.stirring.ResourceStirringService
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.Completable
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream

@Component
open class ResourcePackingTask(
        private val stirringService: ResourceStirringService,
        defaultMetaGeneratingTask: ResourcePackDefaultMetaGeneratingTask,
        private val fileWriterService: ReactantTextFileWriterService
) : ResourceStirringTask, LifeCycleHook {

    override val name: String = javaClass.canonicalName
    override val dependsOn: List<ResourceStirringTask> = listOf(defaultMetaGeneratingTask)
    override fun onEnable() {
        stirringService.registerStirringTask(this)
    }

    override fun start(stirringPlan: StirringPlan): Completable = Completable.fromAction {

        File(resourcePackOutputPath).let { if (it.exists()) it.delete() }
        val zip = ZipFile(resourcePackOutputPath)

        zip.addFolder(workingDirectory, ZipParameters().also { it.isIncludeRootFolder = false })

        ResourceStirrer.logger.info("Generating resource pack sha1...")
        val sha1 = DigestUtils.sha1(FileInputStream(zip.file))
        Hex.encodeHexString(sha1).let {
            ResourceStirrer.logger.info("Resource pack sha1 is $it")
            fileWriterService.write(File(ResourceStirrer.resourcePackHashOutputPath), it).blockingAwait()
        }
        stirringPlan.resourcePackSha1 = sha1;
    }
}
