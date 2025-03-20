package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.Edulis.Companion.pluginEnabled
import co.akoot.plugins.edulis.util.Materials.pendingRecipes
import co.akoot.plugins.edulis.util.Materials.resolvedResults
import co.akoot.plugins.edulis.util.Util.loadEverything
import co.akoot.plugins.edulis.util.brewery.BrewRecipes.loadBrewRecipes
import com.dre.brewery.P
import org.bukkit.command.CommandSender

class ReloadCommand(plugin: FoxPlugin) : FoxCommand(plugin, "loadfood") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        resolvedResults.clear()
        pendingRecipes.clear()

        loadEverything(plugin)

        pluginEnabled("Brewery").let {
            P.p.reload(sender) // reload brew config
            loadBrewRecipes()
        }

        return sendMessage(sender, "Food configs reloaded")
    }
}