package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.util.CreateItem.resolvedResults
import org.bukkit.command.CommandSender

class FoodCommand(plugin: FoxPlugin) : FoxCommand(plugin, "food") {

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): MutableList<String> {
        if (args.size == 1) {
            return resolvedResults.keys.toMutableList()
        }
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        val p = playerCheck(sender) ?: return false

        if (args.isEmpty()) return sendError(sender, "Please specify the food item.")

        val outputItem = resolvedResults[args[0]] ?: run {
            sender.sendMessage("Invalid food item.")
            return false
        }

        val item = outputItem.clone().apply { amount = args.getOrNull(1)?.toIntOrNull() ?: 1 }

        p.inventory.addItem(item)
        return true
    }
}