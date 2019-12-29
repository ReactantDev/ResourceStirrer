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

    /**
     * Scale the texture based on slot size (included border width)
     * @param scalingTypes The display types that will apply the scaling
     * For example: scaleBySlotSize(2,3) mean the item will be scaled to 2x3 slot size
     */
    fun scaleBySlotSize(width: Double, height: Double,
                        scalingTypes: List<ItemModel.DisplayType> = listOf(gui)): ItemModelModifier = {
        it.apply {
            display {
                scalingTypes.map { it.displayPositionGetter(this) }.forEach { displayPosition ->
                    displayPosition {
                        scale(1.13 * width, 1.13 * height, scale?.let { it[2] } ?: 0.0)
                    }
                }
            }
        }
    }

    fun scaleBySlotSize(width: Int, height: Int, scalingTypes: List<ItemModel.DisplayType> = listOf(gui)) =
            scaleBySlotSize(width.toDouble(), height.toDouble(), scalingTypes)

    /**
     * Move the texture based on the slot size, initial is at center
     */
    fun translateBySlotSize(x: Double, y: Double,
                            scalingTypes: List<ItemModel.DisplayType> = listOf(gui)): ItemModelModifier = {
        it.apply {
            display {
                scalingTypes.map { it.displayPositionGetter(this) }.forEach { displayPosition ->
                    displayPosition {
                        translation(x * 18, y * 18, translation?.let { it[2] } ?: 0.0)
                    }
                }
            }
        }
    }

    fun translateBySlotSize(x: Int, y: Int, scalingTypes: List<ItemModel.DisplayType> =
            listOf(gui)): (ItemModel).() -> Unit = translateBySlotSize(x.toDouble(), y.toDouble(), scalingTypes)
}
