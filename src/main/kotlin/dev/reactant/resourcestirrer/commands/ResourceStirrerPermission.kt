package dev.reactant.resourcestirrer.commands

import dev.reactant.reactant.extra.command.PermissionRoot

internal object ResourceStirrerPermission : PermissionRoot("ResourceStirrer") {
    object LISTING : S(prefix) {
        object ITEM : S(prefix)
        object SOUND : S(prefix)
    }

    object ADMIN : S(prefix) {
        object SOUND : S(prefix) {
            object PLAY : S(prefix)
        }

        object ITEM : S(prefix) {
            object GET : S(prefix)
        }

        object PACK : S(prefix) {
            object UPDATE : S(prefix)
        }

        object FIX : S(prefix)
    }
}
