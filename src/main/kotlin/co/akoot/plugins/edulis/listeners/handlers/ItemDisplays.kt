package co.akoot.plugins.edulis.listeners.handlers

import co.akoot.plugins.edulis.util.loaders.ConfigLoader.cropsConfig
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack

object ItemDisplays {
    fun createDisplay(location: Location, age: Int, id: String) {
        val world = location.world ?: return
        var itemDisplay: ItemDisplay? = null

        for (entity in world.entities) {
            if (entity is ItemDisplay && entity.location.distanceSquared(location) < 0.5) {
                itemDisplay = entity
                break
            }
        }
        if (itemDisplay == null) {
            itemDisplay = world.spawnEntity(location.clone().add(0.5, 0.5, 0.5), EntityType.ITEM_DISPLAY) as ItemDisplay
        }

        val cmd = cropsConfig.getInt("overlays.$id.$age", 0)

        itemDisplay.setItemStack(ItemBuilder.builder(ItemStack(Material.BARRIER)).customModelData(cmd).build())
    }
}