package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.gui.FoodItemMenu
import co.akoot.plugins.edulis.gui.FoodItemMenu.Companion.foodItemMenu
import co.akoot.plugins.edulis.util.Materials.pendingRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.craftingRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.smeltingRecipes
import co.akoot.plugins.edulis.util.brewery.BrewRecipes.loadBrewRecipes
import org.bukkit.Material
import org.bukkit.entity.ItemDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.event.world.ChunkLoadEvent

class PluginEvent: Listener {

    @EventHandler
    fun onBreweryEnable(event: PluginEnableEvent) {
        // we cant load the recipes until brewery is enabled smh
        if (event.plugin.name == "Brewery") {
            loadBrewRecipes()
            for (recipe in pendingRecipes) {
                smeltingRecipes(recipe)
                craftingRecipes(recipe)
            }
            log.info("Loaded Brewery Recipes!")
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun InventoryPickupItemEvent.pukeItems() {
        isCancelled = item.hasMetadata("brewery_puke")
    }

    @EventHandler
    fun InventoryClickEvent.onInvClick() {
        when (val holder = clickedInventory?.holder) {
            is FoodItemMenu -> {
                foodItemMenu(currentItem ?: return, whoClicked, holder)
                isCancelled = true
            }
        }
    }

    @EventHandler
    fun onChunkLoad(e: ChunkLoadEvent) {
        for (display in e.chunk.entities) {
            if (display is ItemDisplay && display.itemStack.type == Material.BARRIER) {
                val item = display.itemStack.clone()
                display.setItemStack(item.withType(Material.OAK_PRESSURE_PLATE))
            }
        }
    }
}