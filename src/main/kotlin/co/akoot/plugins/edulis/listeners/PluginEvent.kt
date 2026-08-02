package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.gui.FoodItemMenu
import co.akoot.plugins.edulis.gui.FoodItemMenu.Companion.foodItemMenu
import co.akoot.plugins.edulis.util.Util.registerEdulisRecipes
import io.papermc.paper.event.server.ServerResourcesReloadedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class PluginEvent: Listener {
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
    fun ServerResourcesReloadedEvent.orReload() {
        registerEdulisRecipes()
    }
}