package co.akoot.plugins.edulis.blocks.cuttingboard

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.edulis.Edulis
import co.akoot.plugins.plushies.util.Recipes.getInput
import co.akoot.plugins.plushies.util.Recipes.getMaterial
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

data class CBoardRecipes(val tool: RecipeChoice, val input: RecipeChoice, val results: MutableSet<ItemStack>)

private val config: FoxConfig = Edulis.instance.registerConfig("cutting_board", "recipes/cutting_board.conf")
val cuttingBoardRecipes = mutableListOf<CBoardRecipes>()

fun createCBRecipes() {
    for (r in config.getKeys()) {

        val tool = config.getString("$r.tool") ?: continue
        val input = config.getString("$r.input") ?: continue
        val result = config.getStringList("$r.result")

        val recipe = CBoardRecipes(
            getInput(tool) ?: continue,
            getInput(input) ?: continue,
            parseResults(result)
        )

        cuttingBoardRecipes.add(recipe)
    }
}

fun parseResults(stringList: List<String>): MutableSet<ItemStack> {
    val results = mutableSetOf<ItemStack>()
    for (string in stringList){
        val split = string.split("/")
        val item = split[0]
        val amount = split.getOrNull(1)?.toIntOrNull() ?: 1

        results.add(getMaterial(item, amount) ?: return mutableSetOf())
    }
    return results
}