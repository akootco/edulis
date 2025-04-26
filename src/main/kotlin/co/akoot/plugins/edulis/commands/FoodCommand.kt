package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.Edulis.Companion.cakeConfig
import co.akoot.plugins.edulis.Edulis.Companion.itemConfig
import co.akoot.plugins.edulis.gui.FoodItemMenu
import co.akoot.plugins.edulis.util.Materials.loadItems
import co.akoot.plugins.edulis.util.Util.isFood
import co.akoot.plugins.plushies.util.Items.customItems
import org.bukkit.command.CommandSender

class FoodCommand(plugin: FoxPlugin) : FoxCommand(plugin, "food") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        if (args.size == 1) {
            return customItems.filter { it.value.isFood }.keys.toMutableList()
        }
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        val p = playerCheck(sender) ?: return false

        when (args.getOrNull(0)) {
            "reload" -> {
                customItems.entries.removeIf { it.value.isFood }
                loadItems(itemConfig)
                loadItems(cakeConfig)
                return sendMessage(sender, "Food reloaded")
            }

            else -> {
                if (args.isEmpty()) {
                    p.openInventory(FoodItemMenu().inventory)
                    return true
                }

                val outputItem = customItems.keys.find { it.equals(args[0], ignoreCase = true) }
                    ?.let { customItems[it] } ?: run { return sendError(sender, "Invalid item.") }
                val count = args.getOrNull(1)?.toIntOrNull() ?: 1

                p.inventory.addItem(outputItem.clone().apply { amount = count })
                return true
            }
        }
    }
}