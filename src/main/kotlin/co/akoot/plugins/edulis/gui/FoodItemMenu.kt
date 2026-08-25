package co.akoot.plugins.edulis.gui

import co.akoot.plugins.edulis.util.Util.foodid
import co.akoot.plugins.edulis.util.Util.isFood
import co.akoot.plugins.plushies.api.ChestMenu
import co.akoot.plugins.plushies.util.Items.customItems
import net.kyori.adventure.text.Component

class FoodItemMenu() : ChestMenu() {
    override val title = Component.text("Food")
    override val items = customItems.values.filter { it.isFood && !it.foodid.contains("bat_wing") }
}