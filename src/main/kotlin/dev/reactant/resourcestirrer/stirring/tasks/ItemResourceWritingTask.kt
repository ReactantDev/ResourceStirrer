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
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable.fromCallable
import io.reactivex.rxjava3.kotlin.toObservable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.collections.HashMap


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
                .entries.sortedBy { it.value }.toObservable()
                .flatMap { (itemResourceIdentifier, _) ->
                    fromCallable {
                        val itemResource = itemResourceService.getItem(itemResourceIdentifier)!!
                        val assetsPath = "${workingDirectory.absolutePath}/assets/${ResourceStirringTask.ASSETS_NAME_SPACE}";

                        val outputPrefix = when {
                            stirringPlan.resourceStirrerConfig.content.uglify && !itemResourceIdentifier.startsWith("default-") -> UUID.randomUUID().toString()
                            else -> itemResourceIdentifier
                        }
                        stirringPlan.identifierPrefixMapping[itemResourceIdentifier] = outputPrefix

                        val texturePrefix = "$assetsPath/textures/$outputPrefix"
                        val modelFilePath = "$assetsPath/models/$outputPrefix.json"

                        // Copy model file
                        val copiedModelFile = File(modelFilePath);
                        if (!copiedModelFile.parentFile.exists()) copiedModelFile.parentFile.mkdirs()
                        itemResource.writeModelFile(copiedModelFile.absolutePath)

                        // Replace model file var
                        copiedModelFile.readText()
                                .replace("{{prefix}}", "stirred:" + outputPrefix)
                                .let { copiedModelFile.writeText(it) }

                        // Copy textures file
                        itemResource.writeTextureFiles(texturePrefix)
                    }.subscribeOn(Schedulers.computation())
                }
                .ignoreElements()
                .blockingAwait()
        ResourceStirrer.logger.info("End")
        rewriteMainModelFile(stirringPlan).blockingAwait()
    }

    fun rewriteMainModelFile(stirringPlan: StirringPlan): Completable = Completable.fromCallable {
        val materialModelChanges = stirringPlan.stirrerCustomDataLock.content.itemResourceCustomDataLock.entries
                .filter { itemResourceService.getItem(it.key) != null }
                .sortedBy { it.value }
                .map { itemResourceService.getItem(it.key)!! to it.value }
                .groupBy { (_, customData) ->
                    customData.split('-')[0] // material
                }

        materialModelChanges.entries.toObservable()
                .doOnNext { (material, changes) ->
                    val materialModelPath = "${workingDirectory.absolutePath}/assets/minecraft/models/item/$material.json";
                    val materialModelFile = File(materialModelPath)

                    // if there have no model file of that material yet
                    if (!materialModelFile.exists()) {

                        // create folder if not yet created
                        if (!materialModelFile.parentFile.exists()) materialModelFile.parentFile.mkdirs();
                        File("${ResourceStirrer.configFolder}/models/item/").let { if (!it.exists()) it.mkdirs() }
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

                    parser.parse(FileReader(materialModelFile)).asJsonObject.let {
                        if (!it.has("overrides")) it.add("overrides", JsonArray())

                        val overrides = it.getAsJsonArray("overrides");

                        changes.map { (itemResource, customData) ->
                            val modelPath = stirringPlan.identifierPrefixMapping[itemResource.identifier]
                            val predicates = HashMap(itemResource.predicate)
                            predicates["custom_model_data"] = customData.split('-')[1].toInt();
                            JsonObject().apply {
                                add("predicate", gson.toJsonTree(predicates).asJsonObject)
                                addProperty("model", "${ResourceStirringTask.ASSETS_NAME_SPACE}:$modelPath")
                            }
                        }.forEach {
                            overrides.add(it)
                        }
                        FileWriter(materialModelFile).use { writer -> gson.toJson(it, writer) }
                    }
                }

    }


}

