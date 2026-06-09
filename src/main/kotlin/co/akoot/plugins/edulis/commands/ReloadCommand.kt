package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.util.async
import co.akoot.plugins.edulis.Edulis.Companion.cakeConfig
import co.akoot.plugins.edulis.Edulis.Companion.itemConfig
import co.akoot.plugins.edulis.Edulis.Companion.pluginEnabled
import co.akoot.plugins.edulis.blocks.cuttingboard.createCBRecipes
import co.akoot.plugins.edulis.blocks.cuttingboard.createCuttingBoards
import co.akoot.plugins.edulis.util.Materials.loadItems
import co.akoot.plugins.edulis.util.Materials.pendingRecipes
import co.akoot.plugins.edulis.util.Util.isFood
import co.akoot.plugins.edulis.util.brewery.loadBrewRecipes
import co.akoot.plugins.plushies.util.Items.customItems
import org.bukkit.command.CommandSender

class ReloadCommand(plugin: FoxPlugin) : FoxCommand(plugin, "loadfood") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        // remove from map only if food
        async {
            customItems.entries.removeIf { it.value.isFood }
            pendingRecipes.clear()

            loadItems(itemConfig)
            loadItems(cakeConfig)

            createCuttingBoards()
            createCBRecipes()

            pluginEnabled("BreweryX").let { loadBrewRecipes() }

            sendMessage(sender, "Food configs reloaded")
        }

        return true
    }
}