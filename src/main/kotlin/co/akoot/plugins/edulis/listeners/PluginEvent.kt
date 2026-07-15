package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.gui.FoodItemMenu
import co.akoot.plugins.edulis.gui.FoodItemMenu.Companion.foodItemMenu
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
}