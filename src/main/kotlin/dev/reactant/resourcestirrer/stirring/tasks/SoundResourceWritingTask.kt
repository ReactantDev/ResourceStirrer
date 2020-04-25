package dev.reactant.resourcestirrer.stirring.tasks

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.resourcestirrer.collector.SoundResourceManagingService
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.Completable
import java.io.File
import java.io.FileReader
import java.io.FileWriter


@Component
class SoundResourceWritingTask(
        private val soundResourceService: SoundResourceManagingService,
        baseResourceCopyingTask: BaseResourceCopyingTask
) : ResourceStirringTask, LifeCycleHook {
    override val name: String = javaClass.canonicalName
    override val dependsOn: List<ResourceStirringTask> = listOf(baseResourceCopyingTask)

    private val gson = GsonBuilder().create();
    private val parser = JsonParser();

    override fun onEnable() {
    }

    override fun start(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        val assetsPath = "${workingDirectory.absolutePath}/assets/${ResourceStirringTask.ASSETS_NAME_SPACE}";
        soundResourceService.identifierResources.values.forEachIndexed { i, soundResource ->
            soundResource.writeFile("$assetsPath/sounds/${soundResource.identifier}")
        }
        rewriteMainSoundFile(stirringPlan).blockingAwait()
    }

    fun rewriteMainSoundFile(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        val mainSoundPath = "${workingDirectory.absolutePath}/assets/minecraft/sounds.json"
        val mainSoundFile = File(mainSoundPath)

        // Create a empty main sound file if it is not exist
        if (!mainSoundFile.exists()) {
            if (!mainSoundFile.parentFile.exists()) mainSoundFile.parentFile.mkdirs()
            FileWriter(mainSoundFile).use { gson.toJson(JsonObject(), it) }
        }

        parser.parse(FileReader(mainSoundFile)).asJsonObject.let { mainSoundJson ->
            soundResourceService.identifierResources.values.forEach {
                val soundEventObject = JsonObject()
                val soundEventSoundsArr = JsonArray()

                it.sounds.mapIndexed { index, sound -> index to sound }
                        .forEach { (index, sound) ->
                            if (!sound.preload && !sound.stream && sound.weight == 1) {
                                soundEventSoundsArr.add("stirred:" + it.identifier + "-" + index)
                            } else {
                                val soundObject = JsonObject()
                                soundObject.addProperty("name", "stirred:" + it.identifier + "-" + index)
                                if (sound.weight != 1) soundObject.addProperty("weight", sound.weight)
                                if (sound.preload) soundObject.addProperty("preload", sound.preload)
                                if (sound.stream) soundObject.addProperty("stream", sound.stream)
                                soundEventSoundsArr.add(soundObject)
                            }
                        }

                it.subtitle?.let { soundEventObject.addProperty("subtitle", it) }
                soundEventObject.add("sounds", soundEventSoundsArr)
                mainSoundJson.add(it.identifier, soundEventObject)
            }
            FileWriter(mainSoundFile).use { writer -> gson.toJson(mainSoundJson, writer) }
        }
    }

}

