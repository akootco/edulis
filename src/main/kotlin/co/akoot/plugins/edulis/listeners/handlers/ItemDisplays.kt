package co.akoot.plugins.edulis.listeners.handlers

import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.edulis.Edulis.Companion.overlayConfig
import co.akoot.plugins.plushies.util.Util.getBlockPDC
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack

object ItemDisplays {
    fun createDisplay(location: Location, age: Int, id: String) {
        val loc = location.clone().add(0.5, 0.5, 0.5)

        var existingDisplay: ItemDisplay? = null

        // check for existing display
        for (entity in location.world.entities) {
            if (entity is ItemDisplay && entity.location.distance(loc) < 0.5) {
                existingDisplay = entity
                break
            }
        }

        val cmd = overlayConfig.getInt("$id.age$age") ?: run {
            removeDisplay(location)
            return
        }

        val overlay = ItemBuilder.builder(ItemStack(Material.OAK_PRESSURE_PLATE))
            .customModelData(cmd)
            .build()

        if (existingDisplay != null) {
            // take over the existing display
            existingDisplay.setItemStack(overlay)
        } else {
            // spawn a new one
            val itemDisplay = location.world.spawnEntity(loc, EntityType.ITEM_DISPLAY) as ItemDisplay
            itemDisplay.setItemStack(overlay)
        }
    }

    fun removeDisplay(location: Location, removePDC: Boolean = false) {
        val loc = location.clone().add(0.5, 0.5, 0.5) // this is so dumb!

        for (entity in location.world.entities) {
            if (entity is ItemDisplay && entity.location.distance(loc) < 0.5) {
                if (removePDC) loc.chunk.removePDC(getBlockPDC(loc, "edulis"))
                entity.remove()
            }
        }
    }
}