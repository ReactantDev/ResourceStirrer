package dev.reactant.resourcestirrer.resourcetype.item

import dev.reactant.resourcestirrer.model.AnimationMeta
import dev.reactant.resourcestirrer.model.ItemModel
import dev.reactant.resourcestirrer.model.ItemModel.DisplayTypes.gui

@Suppress("UNCHECKED_CAST")
interface GeneratedModelItemResource<T> {
    var itemModel: ItemModel
    val layers: Set<String>
    val animationMeta: HashMap<String, AnimationMeta>
    fun applyModifier(modifier: ItemResourceModifier): T = this.apply(modifier).let { this as T }
}


typealias ItemResourceModifier = (GeneratedModelItemResource<*>) -> Unit

object ItemModelModifiers {

    private fun positionModifier(types: List<ItemModel.DisplayType>, modify: ItemModel.DisplayPosition.() -> Unit): ItemResourceModifier = {
        it.itemModel.apply {
            display {
                types.map { it.displayPositionGetter(this) }.forEach { displayPosition -> displayPosition(modify) }
            }
        }
    }

    fun scale(x: Double?, y: Double?, z: Double?, types: List<ItemModel.DisplayType>): ItemResourceModifier =
            positionModifier(types) {
                scale(
                        x ?: scale?.let { it[0] } ?: 0.0,
                        y ?: scale?.let { it[1] } ?: 0.0,
                        z ?: scale?.let { it[2] } ?: 0.0
                )
            }

    fun scale(width: Int?, height: Int?, depth: Int? = null, scalingTypes: List<ItemModel.DisplayType>) =
            scale(width?.toDouble(), height?.toDouble(), depth?.toDouble(), scalingTypes)

    /**
     * Scale the texture based on slot size (included border width)
     * The relation between item normal scale and the full slot scale in gui is about 1:1.13
     * @param scalingTypes The display types that will apply the scaling
     * For example: scaleBySlotSize(2,3) mean the item will be scaled to 2x3 slot size
     */
    fun scaleAsGUI(width: Double, height: Double, scalingTypes: List<ItemModel.DisplayType> = listOf(gui)) =
            scale(width * 1.13, height * 1.13, null, scalingTypes)

    fun scaleAsGUI(width: Int, height: Int, scalingTypes: List<ItemModel.DisplayType> = listOf(gui)) =
            scaleAsGUI(width.toDouble(), height.toDouble(), scalingTypes)


    fun translation(x: Double?, y: Double?, z: Double? = null,
                    types: List<ItemModel.DisplayType>): ItemResourceModifier =
            positionModifier(types) {
                translation(
                        x ?: translation?.let { it[0] } ?: 0.0,
                        y ?: translation?.let { it[1] } ?: 0.0,
                        z ?: translation?.let { it[2] } ?: 0.0
                )
            }


    fun translation(x: Int?, y: Int?, z: Int? = null, types: List<ItemModel.DisplayType>) =
            translation(x?.toDouble(), y?.toDouble(), z?.toDouble(), types)

    /**
     * Move the texture based on the slot size, initial is at center
     * To move an item from original position to another slot, it need around 18 unit of translation
     */
    fun translationAsGUI(x: Double, y: Double, z: Double? = null, types: List<ItemModel.DisplayType> = listOf(gui)) =
            translation(x * 18, y * 18, z, types)

    fun translationAsGUI(x: Int, y: Int, z: Int? = null, types: List<ItemModel.DisplayType> = listOf(gui)) =
            translationAsGUI(x.toDouble(), y.toDouble(), z?.toDouble(), types)


    fun rotation(x: Double?, y: Double?, z: Double? = null, types: List<ItemModel.DisplayType>): ItemResourceModifier =
            positionModifier(types) {
                rotation(
                        x ?: rotation?.let { it[0] } ?: 0.0,
                        y ?: rotation?.let { it[1] } ?: 0.0,
                        z ?: rotation?.let { it[2] } ?: 0.0
                )
            }

    fun rotation(x: Int?, y: Int?, z: Int? = null, types: List<ItemModel.DisplayType>) =
            rotation(x?.toDouble(), y?.toDouble(), z?.toDouble(), types)

}

object AnimationModifiers {
    fun frametime(frametime: Int): ItemResourceModifier = { res ->
        res.layers.forEach {
            res.animationMeta.getOrPut(it, { AnimationMeta() }).apply {
                animation {
                    this.frametime = frametime
                }
            }
        }
    }

}
