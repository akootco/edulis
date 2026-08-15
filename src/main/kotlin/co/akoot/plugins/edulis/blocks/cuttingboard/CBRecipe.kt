package co.akoot.plugins.edulis.blocks.cuttingboard

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.edulis.Edulis
import co.akoot.plugins.plushies.api.loadSimpleRecipes
import co.akoot.plugins.plushies.util.Items.getItem
import co.akoot.plugins.plushies.util.builders.CraftRecipe
import org.bukkit.Tag

private val config: FoxConfig =
    Edulis.instance.registerConfig(
        "cutting_board",
        "recipes/cutting_board.conf"
    )

val cuttingBoardRecipes = loadSimpleRecipes(config)

fun cuttingBoardCraftRecipe() {
    for (material in Tag.PRESSURE_PLATES.values) {
        val woodType = material.name.removeSuffix("_PRESSURE_PLATE")
        val name = "${woodType.lowercase()}_cutting_board"

        CraftRecipe.builder(name, getItem(name) ?: continue)
            .ingredient(material)
            .shapeless("edulis")
    }
}