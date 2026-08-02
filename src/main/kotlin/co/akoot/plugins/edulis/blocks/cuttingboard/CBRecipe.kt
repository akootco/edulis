package co.akoot.plugins.edulis.blocks.cuttingboard

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.edulis.Edulis
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.plushies.util.Items.getItem
import co.akoot.plugins.plushies.util.Recipes.getInput
import co.akoot.plugins.plushies.util.Recipes.getMaterial
import co.akoot.plugins.plushies.util.builders.CraftRecipe
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

data class CBoardRecipes(val tool: RecipeChoice, val input: RecipeChoice, val results: MutableSet<ItemStack>)

private val config: FoxConfig = Edulis.instance.registerConfig("cutting_board", "recipes/cutting_board.conf")
val cuttingBoardRecipes = mutableListOf<CBoardRecipes>()

private fun erm(recipe: String, item: String) = log.warn("Invalid $item in `$recipe` cutting board recipe.")

fun cuttingBoardCraftRecipe() {
    for (material in Tag.PLANKS.values) {
        val woodType = material.name.removeSuffix("_PLANKS")
        val name = "${woodType.lowercase()}_cutting_board"
        val ingredientMaterial = Material.getMaterial("${woodType}_PRESSURE_PLATE") ?: continue

        CraftRecipe.builder(name, getItem(name) ?: continue)
            .ingredient(ingredientMaterial)
            .shapeless("edulis")
    }
}

fun createCBRecipes() {
    for (r in config.getKeys()) {
        val toolInput = getInput(config.getString("$r.tool") ?: continue) ?: run {
            erm(r, "tool")
            continue
        }
        val inputItem = getInput(config.getString("$r.input") ?: continue) ?: run {
            erm(r, "input")
            continue
        }

        val results = parseResults(r, config.getStringList("$r.result"))
        if (results.isEmpty()) continue

        cuttingBoardRecipes.add(CBoardRecipes(toolInput, inputItem, results))
    }
}

fun parseResults(recipe: String, stringList: List<String>): MutableSet<ItemStack> {
    val results = mutableSetOf<ItemStack>()
    for (string in stringList) {
        val split = string.split("/")
        val item = split[0]
        val amount = split.getOrNull(1)?.toIntOrNull() ?: 1

        val material = getMaterial(item, amount)
        if (material == null) {
            erm(recipe, "result")
            continue
        }
        results.add(material)
    }
    return results
}