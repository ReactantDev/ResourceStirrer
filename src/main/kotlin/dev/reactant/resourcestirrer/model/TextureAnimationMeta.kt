package dev.reactant.resourcestirrer.model

class TextureAnimationMeta {
    var animation: Animation? = Animation()


    class Animation {
        var interpolate: Boolean? = null
        var frametime: Int? = null
        /**
         * Element should be either Int or Animation Frame
         */
        var frames: List<Any>? = null
    }

    class AnimationFrame {
        var index: Int? = null
        var time: Int? = null
    }
}
