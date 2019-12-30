package dev.reactant.resourcestirrer.stirring.tasks

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.reactant.reactant.core.component.Component
import dev.reactant.reactant.core.component.lifecycle.LifeCycleHook
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.collector.ItemResourceManagingService
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.Completable
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStream


@Component
class ItemResourceWritingTask(
        private val itemResourceService: ItemResourceManagingService,
        baseResourceCopyingTask: BaseResourceCopyingTask
) : ResourceStirringTask, LifeCycleHook {
    override val name: String = javaClass.canonicalName
    override val dependsOn: List<ResourceStirringTask> = listOf(baseResourceCopyingTask)

    private val gson = GsonBuilder().create();
    private val parser = JsonParser();

    override fun onEnable() {
    }

    override fun start(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        stirringPlan.stirrerCustomDataLock.content.itemResourceCustomDataLock
                .filter { itemResourceService.getItem(it.key) == null }
                .toMap()
                .also { stirringPlan.lostReferenceCustomData = it }
                .also { if (it.isNotEmpty()) ResourceStirrer.logger.warn("Found ${it.size} resource custom data locks lost their reference, it won't cause any problem normally, use \"/resstir fixtool\" to check.") }
        stirringPlan.stirrerCustomDataLock.content.itemResourceCustomDataLock
                .filter { itemResourceService.getItem(it.key)?.baseItem != null }
                .entries
                .sortedBy { it.value }
                .forEach { (itemResourceIdentifier, _) ->
                    val itemResource = itemResourceService.getItem(itemResourceIdentifier)!!
                    val assetsPath = "${workingDirectory.absolutePath}/assets/${ResourceStirringTask.ASSETS_NAME_SPACE}";
                    val textureFolderPath = "$assetsPath/textures/$itemResourceIdentifier"
                    val modelFilePath = "$assetsPath/models/$itemResourceIdentifier.json"

                    // Copy model file
                    val copiedModelFile = File(modelFilePath);
                    if (!copiedModelFile.parentFile.exists()) copiedModelFile.parentFile.mkdirs()
                    itemResource.writeModelFile(copiedModelFile.absolutePath)

                    // Replace model file var
                    copiedModelFile.readText()
                            .replace("{{prefix}}", itemResourceIdentifier)
                            .let { copiedModelFile.writeText(it) }

                    // Copy textures file
                    itemResource.writeTextureFiles(textureFolderPath)
                }
        rewriteMainModelFile(stirringPlan).blockingAwait()
    }

    fun rewriteMainModelFile(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        stirringPlan.stirrerCustomDataLock.content.itemResourceCustomDataLock.entries
                .filter { itemResourceService.getItem(it.key) != null }
                .sortedBy { it.value }
                .map { itemResourceService.getItem(it.key)!! to it.value }
                .forEach { (itemResource, customData) ->
                    val material = customData.split('-')[0];
                    val customData = customData.split('-')[1].toInt();

                    val materialModelPath = "${workingDirectory.absolutePath}/assets/minecraft/models/item/$material.json";
                    val materialModelFile = File(materialModelPath)

                    // if there have no model file of that material yet
                    if (!materialModelFile.exists()) {

                        // create folder if not yet created
                        if (!materialModelFile.parentFile.exists()) materialModelFile.parentFile.mkdirs();
                        val defaultModelFile = File("${ResourceStirrer.configFolder}/models/item/$material.json");

                        // if default model (minecraft's model) isn't provided
                        if (!defaultModelFile.exists()) {
                            ResourceStirrer.logger.warn("Default model is not existing: \"${defaultModelFile.absoluteFile}\", using empty json object.")
                            FileWriter(materialModelFile).use { gson.toJson(JsonObject(), it) }
                        } else {
                            // copy the default model
                            val defaultModelJsonObject = parser.parse(FileReader(defaultModelFile)).asJsonObject
                            FileWriter(materialModelFile).use { gson.toJson(defaultModelJsonObject, it) }
                        }
                    }

                    // edit the original model file
                    parser.parse(FileReader(materialModelFile)).asJsonObject.let {
                        if (!it.has("overrides")) it.add("overrides", JsonArray())

                        val overrides = it.getAsJsonArray("overrides");
                        val overrideObj = JsonObject();
                        val predicates = HashMap(itemResource.predicate)
                        predicates["custom_model_data"] = customData;
                        overrideObj.add("predicate", gson.toJsonTree(predicates).asJsonObject)
                        overrideObj.addProperty("model", "${ResourceStirringTask.ASSETS_NAME_SPACE}:${itemResource.identifier}")
                        overrides.add(overrideObj);

                        FileWriter(materialModelFile).use { writer -> gson.toJson(it, writer) }
                    }
                }
    }

    class CopyingFile(val inputStream: InputStream, val fileName: String)

}

