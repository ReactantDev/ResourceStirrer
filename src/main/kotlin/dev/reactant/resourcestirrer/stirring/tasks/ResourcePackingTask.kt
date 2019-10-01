package dev.reactant.resourcestirrer.stirring.tasks

import dev.reactant.reactant.core.component.Component
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.Completable
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream

@Component
internal class ResourcePackingTask : ResourceStirringTask {
    override fun start(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        File(stirringPlan.resourceStirrerConfig.content.outputPath).let { if (it.exists()) it.delete() }
        val zip = ZipFile(stirringPlan.resourceStirrerConfig.content.outputPath);

        zip.addFolder(cacheFolder,ZipParameters().also { it.isIncludeRootFolder = false })
//        cacheFolder.listFiles()?.forEach { file ->
//            if (file.isFile) zip.addFile(file, ZipParameters().also { it.fileNameInZip = file.name })
//            else if (file.isDirectory) zip.addFolder(file, ZipParameters().also { it.defaultFolderPath = "test" })
//        }
        ResourceStirrer.logger.info("Generating resource pack sha1...")
        val sha1 = DigestUtils.sha1Hex(FileInputStream(zip.file))
        ResourceStirrer.logger.info("Resource pack sha1 is $sha1");
    }
}
