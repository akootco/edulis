package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.Edulis.Companion.pluginEnabled
import co.akoot.plugins.edulis.util.brewery.BrewRecipes.loadBrewRecipes
import co.akoot.plugins.edulis.util.loaders.ConfigLoader
import com.dre.brewery.P
import org.bukkit.command.CommandSender

class ReloadCommand(plugin: FoxPlugin) : FoxCommand(plugin, "loadfood") {

    override fun onTabComplete(sender: CommandSender, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {

        // reload config and brewery
        ConfigLoader.loadConfigs(plugin)
        pluginEnabled("Brewery").let {
            P.p.reload(sender)
            loadBrewRecipes()
        }

        sender.sendMessage("Reloaded food.")
        return true
    }
}