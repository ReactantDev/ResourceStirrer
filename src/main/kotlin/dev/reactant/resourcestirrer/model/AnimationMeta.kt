package dev.reactant.resourcestirrer.model

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.annotations.JsonAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

data class AnimationMeta(
        var animation: Animation? = Animation()
) {

    fun toJson(): String {
        return GSON.toJson(this, AnimationMeta::class.java)
    }


    data class Animation(
            var interpolate: Boolean? = null,
            var frametime: Int? = null,
            @JsonAdapter(AnimationFrameListAdapterFactory::class)
            var frames: List<AnimationFrame>? = null
    )

    data class AnimationFrame(
            var index: Int? = null,
            var time: Int? = null
    )

    class AnimationFrameListAdapterFactory : TypeAdapterFactory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>): TypeAdapter<T> = AnimationFrameListAdapter() as TypeAdapter<T>

        private class AnimationFrameListAdapter : TypeAdapter<List<AnimationFrame>>() {
            override fun write(out: JsonWriter, value: List<AnimationFrame>) {
                when {
                    value.isEmpty() -> Unit
                    else -> {
                        out.beginArray()
                        value.forEach { frame ->
                            when {
                                frame.index == null -> Unit
                                frame.time == null -> out.value(frame.index)
                                else -> {
                                    out.beginObject()
                                    out.name("index")
                                    out.value(frame.index)
                                    out.name("time")
                                    out.value(frame.time)
                                    out.endObject()
                                }
                            }
                        }
                        out.endArray()
                    }
                }
            }

            override fun read(reader: JsonReader): List<AnimationFrame> {
                return when (reader.peek()) {
                    JsonToken.NULL -> listOf()
                    JsonToken.BEGIN_ARRAY -> {
                        val frames = listOf<AnimationFrame>()
                        reader.beginArray()
                        loopArray@ while (reader.hasNext()) {
                            when (reader.peek()) {
                                JsonToken.NUMBER -> AnimationFrame().apply { index = reader.nextInt() }
                                JsonToken.END_ARRAY -> break@loopArray
                                JsonToken.BEGIN_OBJECT -> {
                                    reader.beginObject()
                                    val frame = AnimationFrame()
                                    var valueSetter: ((Any) -> Unit)? = {}
                                    loopFrameObject@ while (reader.hasNext()) {
                                        when (reader.peek()) {
                                            JsonToken.NAME -> when (reader.nextName()) {
                                                "index" -> valueSetter = { frame.index = it as Int }
                                                "time" -> valueSetter = { frame.time = it as Int }
                                                else -> valueSetter = {}
                                            }
                                            JsonToken.NUMBER -> valueSetter?.let { it(reader.nextInt()) }
                                            JsonToken.END_OBJECT -> break@loopFrameObject
                                        }
                                    }
                                    reader.endObject()
                                }
                            }
                        }
                        reader.endArray()
                        return frames
                    }
                    else -> listOf()
                }
            }

        }
    }

    companion object {
        val GSON = Gson()
    }
}


