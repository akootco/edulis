package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.bluefox.api.FoxCommand
import co.akoot.plugins.bluefox.api.FoxPlugin
import com.dre.brewery.recipe.BRecipe

import org.bukkit.command.CommandSender

class DrinksCommand(plugin: FoxPlugin) : FoxCommand(plugin, "drinks") {

    override fun onTabComplete(sender: CommandSender, alias: String, args: Array<out String>): MutableList<String> {

        if (args.size == 1) {
            val pages = mutableListOf<String>()

            BRecipe.getRecipes().forEach { pages.add(it.id) }
            return pages
        }

        return mutableListOf()
    }

    override fun onCommand(sender: CommandSender, alias: String, args: Array<out String>): Boolean {
        val p = playerCheck(sender) ?: return false

        val recipe = if (args.isNotEmpty()) args[0] else "all"

        if (recipe == "book" && permissionCheck(p, "book") == true) {
            brewBook("all")?.let { p.give(it) }
        }
        else { p.openBook(brewBook(recipe) ?: return sendError(p, "Recipe not found!")) }

        return true
    }
}