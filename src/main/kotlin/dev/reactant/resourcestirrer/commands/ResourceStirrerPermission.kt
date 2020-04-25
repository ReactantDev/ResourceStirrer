package dev.reactant.resourcestirrer.commands

import dev.reactant.reactant.extra.command.PermissionNode

internal object ResourceStirrerPermission : PermissionNode("ResourceStirrer") {
    object LISTING : PermissionNode(child("listing")) {
        object ITEM : PermissionNode(child("item"))
        object SOUND : PermissionNode(child("sound"))
    }

    object ADMIN : PermissionNode(child("admin")) {
        object SOUND : PermissionNode(child("sound")) {
            object PLAY : PermissionNode(child("play"))
        }

        object ITEM : PermissionNode(child("item")) {
            object GET : PermissionNode(child("get"))
        }

        object PACK : PermissionNode(child("pack")) {
            object UPDATE : PermissionNode(child("update"))
        }

        object FIX : PermissionNode(child("fix"))
    }
}
