package co.akoot.plugins.edulis.commands

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.edulis.Edulis.Companion.cakeConfig
import co.akoot.plugins.edulis.Edulis.Companion.headConfig
import co.akoot.plugins.edulis.Edulis.Companion.craftingConfig
import co.akoot.plugins.edulis.Edulis.Companion.itemConfig
import co.akoot.plugins.edulis.Edulis.Companion.pluginEnabled
import co.akoot.plugins.edulis.Edulis.Companion.smokerConfig
import co.akoot.plugins.edulis.util.CreateItem.loadItems
import co.akoot.plugins.edulis.util.CreateItem.pendingRecipes
import co.akoot.plugins.edulis.util.CreateItem.resolvedResults
import co.akoot.plugins.edulis.util.CreateRecipes.craftingRecipes
import co.akoot.plugins.edulis.util.CreateRecipes.smeltingRecipes
import co.akoot.plugins.edulis.util.Schematics.registerSchematics
import co.akoot.plugins.edulis.util.brewery.BrewRecipes.loadBrewRecipes
import com.dre.brewery.P
import org.bukkit.Bukkit
import org.bukkit.Keyed
import org.bukkit.command.CommandSender

class ReloadCommand(plugin: FoxPlugin) : FoxCommand(plugin, "loadfood") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {
        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {

        resolvedResults.clear()
        pendingRecipes.clear()

        loadItems(itemConfig)
        loadItems(cakeConfig)
        loadItems(headConfig)

        // remove all flugin recipes
        val iterator = Bukkit.recipeIterator()
        while (iterator.hasNext()) {
            val recipe = iterator.next()

            if (recipe is Keyed) {
                val key = (recipe as Keyed).key // erm?
                if (key.namespace == "edulis") {
                    Bukkit.removeRecipe(key)
                }
            }
        }

        for (key in smokerConfig.getKeys()) {
            smeltingRecipes(key)
        }

        for (key in craftingConfig.getKeys()) {
            craftingRecipes(key)
        }

        // remove all flugin schematics
        val structureManager = Bukkit.getStructureManager()
        for (structure in structureManager.structures) {
            if (structure is Keyed) {
                val key = structure.key
                if (key.namespace == "edulis") {
                    structureManager.unregisterStructure(key)
                }
            }
        }

        registerSchematics(plugin)

        pluginEnabled("Brewery").let {
            P.p.reload(sender) // reload brew config
            loadBrewRecipes()
        }

        return sendMessage(sender, "Food configs reloaded")
    }
}