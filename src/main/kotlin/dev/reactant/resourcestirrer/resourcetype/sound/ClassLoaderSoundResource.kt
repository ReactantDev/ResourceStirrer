package dev.reactant.resourcestirrer.resourcetype.sound

import dev.reactant.resourcestirrer.resourceloader.ClassLoaderResourceLoader
import dev.reactant.resourcestirrer.resourcetype.ClassLoaderResource
import dev.reactant.resourcestirrer.table.SoundResourcesTable

class ClassLoaderSoundResource(
        resourceLoader: ClassLoaderResourceLoader,
        override val identifier: String,
        override val subtitle: String?
) : ClassLoaderResource(resourceLoader), SoundResource {
    private val _sounds = ArrayList<Sound>()
    override val sounds: List<Sound> = _sounds

    override fun writeFile(path: String) {
        sounds.forEachIndexed { index, sound -> extractFileFromLoader("${sound.path}.ogg", "$path-$index.ogg") }
    }

    fun addSound(path: String, weight: Int = 1, stream: Boolean = false, preload: Boolean = false, attenuationDistance: Float? = null): Sound = Sound(path, weight, stream, preload, attenuationDistance).also { _sounds.add(it) }

    class Sound(override val path: String, override val weight: Int = 1,
                override val stream: Boolean = false, override val preload: Boolean = false,
                override val attenuationDistance: Float? = null) : SoundResource.Sound {
    }
}

fun SoundResourcesTable.byClassLoader(identifier: String, subtitle: String? = null, content: ClassLoaderSoundResource.() -> Unit): ClassLoaderSoundResource {
    return ClassLoaderSoundResource(this.resourceLoader, identifier, subtitle).apply(content)
}

