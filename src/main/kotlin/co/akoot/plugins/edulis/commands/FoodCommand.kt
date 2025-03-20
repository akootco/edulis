package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.util.Materials.resolvedResults
import org.bukkit.command.CommandSender

class FoodCommand(plugin: FoxPlugin) : FoxCommand(plugin, "food") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        if (args.size == 1) {
            return resolvedResults.keys.toMutableList()
        }
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        val p = playerCheck(sender) ?: return false

        if (args.isEmpty()) return sendError(sender, "Please specify an item.")

        val outputItem = resolvedResults.keys.find { it.equals(args[0], ignoreCase = true) }
            ?.let { resolvedResults[it] }
            ?: run {
                return sendError(sender, "Invalid food item.")
            }

        val count = args.getOrNull(1)?.toIntOrNull() ?: 1
        val item = outputItem.clone().apply { amount = count }

        p.inventory.addItem(item)
        return true
    }
}