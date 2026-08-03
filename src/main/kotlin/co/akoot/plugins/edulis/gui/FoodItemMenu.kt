package co.akoot.plugins.edulis.gui

import co.akoot.plugins.bluefox.util.text
import co.akoot.plugins.edulis.util.Util.foodid
import co.akoot.plugins.edulis.util.Util.isFood
import co.akoot.plugins.plushies.api.ChestMenu
import co.akoot.plugins.plushies.util.Items.customItems

class FoodItemMenu(page: Int = 1) : ChestMenu(page) {

    override val title = text("Custom Items")

    override val items = customItems.values.filter { it.isFood && !it.foodid.contains("bat_wing") }

    override fun nextPage() = FoodItemMenu(page + 1)

    override fun prevPage() = FoodItemMenu(page - 1)
}