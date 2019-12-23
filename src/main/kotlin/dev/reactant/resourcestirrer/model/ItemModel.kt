package dev.reactant.ui.model

import com.google.gson.Gson

data class ItemModel(
        var parent: String = "item/generated",
        var textures: Textures? = null,
        var display: Display? = null,
        var elements: Array<Element>? = null
) {

    data class Display(
            var thirdperson_righthand: DisplayPosition? = null,
            var thirdperson_lefthand: DisplayPosition? = null,
            var firstperson_righthand: DisplayPosition? = null,
            var firstperson_lefthand: DisplayPosition? = null,
            var gui: DisplayPosition? = null,
            var head: DisplayPosition? = null,
            var ground: DisplayPosition? = null,
            var fixed: DisplayPosition? = null
    )

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
    )


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
