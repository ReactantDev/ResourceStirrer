package dev.reactant.resourcestirrer.itemresource

import dev.reactant.resourcestirrer.model.ItemModel
import dev.reactant.resourcestirrer.model.ItemModel.DisplayTypes.gui

@Suppress("UNCHECKED_CAST")
interface GeneratedItemModelItemResource<T> {
    var itemModel: ItemModel
    fun applyModifier(modifier: ItemModelModifier): T = this.itemModel.apply(modifier).let { this as T }
}


typealias ItemModelModifier = (ItemModel) -> Unit

object ItemModelModifiers {

    fun scale(width: Double, height: Double, depth: Double? = null, types: List<ItemModel.DisplayType>): ItemModelModifier = {
        it.apply {
            display {
                types.map { it.displayPositionGetter(this) }.forEach { displayPosition ->
                    displayPosition {
                        scale(width, height, depth ?: scale?.let { it[2] } ?: 0.0)
                    }
                }
            }
        }
    }

    fun scale(width: Int, height: Int, depth: Int? = null, scalingTypes: List<ItemModel.DisplayType>) = scale(width.toDouble(), height.toDouble(), depth?.toDouble(), scalingTypes)

    /**
     * Scale the texture based on slot size (included border width)
     * The relation between item normal scale and the full slot scale in gui is about 1:1.13
     * @param scalingTypes The display types that will apply the scaling
     * For example: scaleBySlotSize(2,3) mean the item will be scaled to 2x3 slot size
     */
    fun scaleAsGUI(width: Double, height: Double,
                   scalingTypes: List<ItemModel.DisplayType> = listOf(gui)): ItemModelModifier = scale(width * 1.13, height * 1.13, null, scalingTypes)

    fun scaleAsGUI(width: Int, height: Int, scalingTypes: List<ItemModel.DisplayType> = listOf(gui)) =
            scaleAsGUI(width.toDouble(), height.toDouble(), scalingTypes)


    fun translation(x: Double, y: Double, z: Double? = null,
                    types: List<ItemModel.DisplayType>): ItemModelModifier = {
        it.apply {
            display {
                types.map { it.displayPositionGetter(this) }.forEach { displayPosition ->
                    displayPosition {
                        translation(x * 18, y * 18, z ?: translation?.let { it[2] } ?: 0.0)
                    }
                }
            }
        }
    }

    fun translation(x: Int, y: Int, z: Int? = null, types: List<ItemModel.DisplayType>) =
            translation(x.toDouble(), y.toDouble(), z?.toDouble(), types)

    /**
     * Move the texture based on the slot size, initial is at center
     * To move an item from original position to another slot, it need around 18 unit of translation
     */
    fun translationAsGUI(x: Double, y: Double, z: Double? = null, types: List<ItemModel.DisplayType> = listOf(gui))
            : ItemModelModifier = translation(x * 18, y * 18, z, types)

    fun translationAsGUI(x: Int, y: Int, z: Int? = null, types: List<ItemModel.DisplayType> = listOf(gui))
            : (ItemModel).() -> Unit = translationAsGUI(x.toDouble(), y.toDouble(), z?.toDouble(), types)


    fun rotation(x: Double, y: Double, z: Double? = null, types: List<ItemModel.DisplayType>): ItemModelModifier = {
        it.apply {
            display {
                types.map { it.displayPositionGetter(this) }.forEach { displayPosition ->
                    displayPosition {
                        rotation(x, y, z ?: translation?.let { it[2] } ?: 0.0)
                    }
                }
            }
        }
    }

    fun rotation(x: Int, y: Int, z: Int? = null, types: List<ItemModel.DisplayType>) = rotation(x.toDouble(), y.toDouble(), z?.toDouble(), types)

}
