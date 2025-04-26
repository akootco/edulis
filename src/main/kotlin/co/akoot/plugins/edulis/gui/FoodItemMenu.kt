package co.akoot.plugins.edulis.gui

import co.akoot.plugins.bluefox.util.ColorUtil.randomColor
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.edulis.util.Util.isFood
import co.akoot.plugins.plushies.gui.MenuItems.filler
import co.akoot.plugins.plushies.gui.MenuItems.nextPage
import co.akoot.plugins.plushies.gui.MenuItems.prevPage
import co.akoot.plugins.plushies.util.Items.customItems
import co.akoot.plugins.plushies.util.builders.ChestGUI
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class FoodItemMenu(private val page: Int = 1) : InventoryHolder {

    companion object {
        fun foodItemMenu(item: ItemStack, p: HumanEntity, holder: InventoryHolder) {
            when (item) {
                filler -> return
                nextPage -> {
                    p.openInventory((holder as FoodItemMenu).nextPage().inventory)
                    return
                }
                prevPage -> {
                    p.openInventory((holder as FoodItemMenu).prevPage().inventory)
                    return
                }
            }
            // if not item above, give it to player
            p.inventory.addItem(item)
        }
    }

    val items = customItems.values.filter { it.isFood }

    private val itemMenu: Inventory = ChestGUI.builder(54, this, true).apply {
        title(Text("Flugin Items").color(randomColor(brightness = 0.6f)).component)
        if (page > 1) setItem(45, prevPage)
        if (items.size > page * 45) setItem(53, nextPage)
        setItems(0..44, getItems(page))
    }.build()

    // create list of food items
    private fun getItems(pageNumber: Int): List<ItemStack> {
        val itemList = mutableListOf<ItemStack>()

        val start = (pageNumber - 1) * 45
        val end = min(start + 45, items.size)

        // only get what fits on the page
        for (index in start until end) {
            itemList.add(items.toList()[index])
        }

        return itemList
    }

    fun nextPage(): FoodItemMenu {
        return FoodItemMenu(page + 1)
    }

    fun prevPage(): FoodItemMenu {
        return FoodItemMenu(page - 1)
    }

    override fun getInventory(): Inventory {
        return this.itemMenu
    }
}