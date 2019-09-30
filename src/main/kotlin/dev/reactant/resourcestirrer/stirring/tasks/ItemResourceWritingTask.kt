package dev.reactant.resourcestirrer.stirring.tasks

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.reactant.reactant.core.component.Component
import dev.reactant.resourcestirrer.ResourceStirrer
import dev.reactant.resourcestirrer.collector.ItemResourceManagingService
import dev.reactant.resourcestirrer.stirring.StirringPlan
import io.reactivex.Completable
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStream


@Component
internal class ItemResourceWritingTask(
        private val itemResourceService: ItemResourceManagingService
) : ResourceStirringTask {
    private val gson = GsonBuilder().create();
    private val parser = JsonParser();

    override fun start(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        stirringPlan.stirrerMetaLock.content.itemResourceCustomMetaLock
                .filter { itemResourceService.getItem(it.key) == null }
                .forEach { ResourceStirrer.logger.warn("A resource custom data lock lost their reference: ${it.key}") }
        stirringPlan.stirrerMetaLock.content.itemResourceCustomMetaLock
                .filter { itemResourceService.getItem(it.key)?.baseItem != null }
                .forEach { (itemResourceIdentifier, _) ->
                    val itemResource = itemResourceService.getItem(itemResourceIdentifier)!!
                    val assetsPath = "${cacheFolder.absolutePath}/assets/${ResourceStirringTask.ASSETS_NAME_SPACE}";
                    val textureFolderPath = "$assetsPath/textures/$itemResourceIdentifier"
                    val modelFilePath = "$assetsPath/models/$itemResourceIdentifier.json"

                    // Copy model file
                    val copiedModelFile = File(modelFilePath);
                    itemResource.writeModelFile(copiedModelFile.absolutePath)

                    // Replace model file var
                    copiedModelFile.readText()
                            .replace("\${dir}", itemResourceIdentifier)
                            .let { copiedModelFile.writeText(it) }

                    // Copy textures file
                    itemResource.writeModelFile(textureFolderPath)
                }
        rewriteMainModelFile(stirringPlan).blockingAwait()
    }

    fun rewriteMainModelFile(stirringPlan: StirringPlan): Completable = Completable.fromCallable {

        itemResourceService.identifierResources.entries.forEach { (resourceIdentifier, itemResource) ->
            val materialCustomMeta = stirringPlan.stirrerMetaLock.content.itemResourceCustomMetaLock[resourceIdentifier]!!;
            val material = materialCustomMeta.split('-')[0];
            val customMeta = materialCustomMeta.split('-')[1].toInt();

            val materialModelPath = "${cacheFolder.absolutePath}/assets/minecraft/models/item/$material.json";
            val materialModelFile = File(materialModelPath)

            // if there have no model file of that material yet
            if (!materialModelFile.exists()) {

                // create folder if not yet created
                if (!materialModelFile.parentFile.exists()) materialModelFile.parentFile.mkdirs();
                val defaultModelFile = File("${ResourceStirrer.configFolder}/defaultModels/item/$material.json");

                // if default model (minecraft's model) isn't provided
                if (!defaultModelFile.exists()) {
                    ResourceStirrer.logger.warn("Default model is not existing: \"${defaultModelFile.absoluteFile}\", using empty json object.")
                    gson.toJson(JsonObject(), FileWriter(materialModelFile))
                } else {
                    // copy the default model
                    val defaultModelJsonObject = parser.parse(FileReader(defaultModelFile)).asJsonObject
                    gson.toJson(defaultModelJsonObject, FileWriter(materialModelFile))
                }
            }

            // edit the original model file
            parser.parse(FileReader(materialModelFile)).asJsonObject.let {
                if (!it.has("overrides")) it.add("overrides", JsonArray())

                val overrides = it.getAsJsonArray("overrides");
                val overrideObj = JsonObject();
                val predicates = HashMap(itemResource.predicate)
                predicates["custom_model_data"] = customMeta;
                overrideObj.add("predicate", gson.toJsonTree(predicates).asJsonObject)
                overrideObj.addProperty("model", "${ResourceStirringTask.ASSETS_NAME_SPACE}:$resourceIdentifier")
                overrides.add(overrideObj);

                FileWriter(materialModelFile).use { writer -> gson.toJson(it, writer) }
            }

        }

    }

    class CopyingFile(val inputStream: InputStream, val fileName: String)

}

