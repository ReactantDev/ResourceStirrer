package dev.reactant.resourcestirrer.commands

import dev.reactant.reactant.extra.command.PermissionNode

internal object ResourceStirrerPermission : PermissionNode("ResourceStirrer") {
    object DISPLAY_LIST : PermissionNode(child("display.list")) {
        object ITEM : PermissionNode(child("item"))
    }
}
