package dev.reactant.resourcestirrer.model

import com.google.gson.Gson

data class ItemModel(
        var parent: String = "item/generated",
        var textures: Textures? = null,
        var display: Display? = null,
        var elements: Array<Element>? = null
) {
    fun textures(content: Textures.() -> Unit): Textures {
        return (textures ?: Textures().also { textures = it }).apply(content)
    }

    fun display(content: Display.() -> Unit): Display {
        return (display ?: Display().also { display = it }).apply(content)
    }

    class DisplayType internal constructor(
            val displayPositionGetter: (Display) -> (DisplayPosition.() -> Unit) -> DisplayPosition
    )

    object DisplayTypes {
        val thirdperson_righthand = DisplayType { display: Display -> { display.thirdperson_righthand(it) } }
        val thirdperson_lefthand = DisplayType { display: Display -> { display.thirdperson_lefthand(it) } }
        val firstperson_righthand = DisplayType { display: Display -> { display.firstperson_righthand(it) } }
        val firstperson_lefthand = DisplayType { display: Display -> { display.firstperson_lefthand(it) } }
        val gui = DisplayType { display: Display -> { display.gui(it) } }
        val head = DisplayType { display: Display -> { display.head(it) } }
        val ground = DisplayType { display: Display -> { display.ground(it) } }
        val fixed = DisplayType { display: Display -> { display.fixed(it) } }
    }

    data class Display(
            var thirdperson_righthand: DisplayPosition? = null,
            var thirdperson_lefthand: DisplayPosition? = null,
            var firstperson_righthand: DisplayPosition? = null,
            var firstperson_lefthand: DisplayPosition? = null,
            var gui: DisplayPosition? = null,
            var head: DisplayPosition? = null,
            var ground: DisplayPosition? = null,
            var fixed: DisplayPosition? = null
    ) {
        fun thirdperson_righthand(content: DisplayPosition.() -> Unit): DisplayPosition {
            return (this.thirdperson_righthand ?: DisplayPosition().also { thirdperson_righthand = it }).apply(content)
        }

        fun thirdperson_lefthand(content: DisplayPosition.() -> Unit): DisplayPosition {
            return (this.thirdperson_lefthand ?: DisplayPosition().also { thirdperson_lefthand = it }).apply(content)
        }

        fun firstperson_righthand(content: DisplayPosition.() -> Unit): DisplayPosition {
            return (this.firstperson_righthand ?: DisplayPosition().also { firstperson_righthand = it }).apply(content)
        }

        fun firstperson_lefthand(content: DisplayPosition.() -> Unit): DisplayPosition {
            return (this.firstperson_lefthand ?: DisplayPosition().also { firstperson_lefthand = it }).apply(content)
        }

        fun gui(content: DisplayPosition.() -> Unit): DisplayPosition {
            return (this.gui ?: DisplayPosition().also { gui = it }).apply(content)
        }

        fun head(content: DisplayPosition.() -> Unit): DisplayPosition {
            return (this.head ?: DisplayPosition().also { head = it }).apply(content)
        }

        fun ground(content: DisplayPosition.() -> Unit): DisplayPosition {
            return (this.ground ?: DisplayPosition().also { ground = it }).apply(content)
        }

        fun fixed(content: DisplayPosition.() -> Unit): DisplayPosition {
            return (this.fixed ?: DisplayPosition().also { fixed = it }).apply(content)
        }
    }

    data class Element(
            var from: Array<Double>? = null,
            var to: Array<Double>? = null,
            var rotation: ElementRotation? = null,
            var faces: ElementFaces? = null
    )

    class ElementFaces(
            var up: ElementFace? = null,
            var down: ElementFace? = null,
            var north: ElementFace? = null,
            var south: ElementFace? = null,
            var west: ElementFace? = null,
            var east: ElementFace? = null
    )

    class ElementFace(
            var uv: Array<Double>? = null,
            var texture: String? = null,
            var cullface: String? = null,
            var rotation: Double? = null,
            var tintindex: Int? = null
    )

    class ElementRotation(
            var origin: Array<Double>? = null,
            var axis: String? = null,
            var angle: Double? = null
    )

    class DisplayPosition(
            var rotation: Array<Double>? = null,
            var translation: Array<Double>? = null,
            var scale: Array<Double>? = null
    ) {
        fun rotation(x: Double, y: Double, z: Double) {
            this.rotation = arrayOf(x, y, z)
        }

        fun translation(x: Double, y: Double, z: Double) {
            this.translation = arrayOf(x, y, z)
        }

        fun scale(x: Double, y: Double, z: Double) {
            this.scale = arrayOf(x, y, z)
        }
    }


    class Textures(
            var layer0: String? = null,
            var layer1: String? = null,
            var layer2: String? = null,
            var layer3: String? = null,
            var layer4: String? = null,
            var layer5: String? = null,
            var particle: String? = null
    )

    companion object {
        private val GSON = Gson()
    }

    fun toJson(): String {
        return GSON.toJson(this, this::class.java)
    }
}
