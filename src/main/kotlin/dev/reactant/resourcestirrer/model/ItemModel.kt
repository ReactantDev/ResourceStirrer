package dev.reactant.resourcestirrer.model

import com.google.gson.Gson

data class ItemModel(
        var parent: String = "item/generated",
        var textures: Textures? = null,
        var display: Display? = null,
        var elements: Array<Element>? = null
) {
    fun textures(content: Textures.() -> Unit) {
        this.textures = Textures().apply(content)
    }

    fun display(content: Display.() -> Unit) {
        this.display = Display().apply(content)
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
        fun thirdperson_righthand(content: DisplayPosition.() -> Unit) {
            this.thirdperson_righthand = DisplayPosition().apply(content)
        }

        fun thirdperson_lefthand(content: DisplayPosition.() -> Unit) {
            this.thirdperson_lefthand = DisplayPosition().apply(content)
        }

        fun firstperson_righthand(content: DisplayPosition.() -> Unit) {
            this.firstperson_righthand = DisplayPosition().apply(content)
        }

        fun firstperson_lefthand(content: DisplayPosition.() -> Unit) {
            this.firstperson_lefthand = DisplayPosition().apply(content)
        }

        fun gui(content: DisplayPosition.() -> Unit) {
            this.gui = DisplayPosition().apply(content)
        }

        fun head(content: DisplayPosition.() -> Unit) {
            this.head = DisplayPosition().apply(content)
        }

        fun ground(content: DisplayPosition.() -> Unit) {
            this.ground = DisplayPosition().apply(content)
        }

        fun fixed(content: DisplayPosition.() -> Unit) {
            this.fixed = DisplayPosition().apply(content)
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
            var particle: String? = null
    )

    companion object {
        private val GSON = Gson()
    }

    fun toJson(): String {
        return GSON.toJson(this, this::class.java)
    }
}
