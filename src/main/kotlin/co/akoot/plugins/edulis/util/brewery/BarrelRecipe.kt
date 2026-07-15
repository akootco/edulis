package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.bluefox.api.FoxConfig
import co.akoot.plugins.edulis.Edulis
import co.akoot.plugins.plushies.util.Recipes.getInput
import co.akoot.plugins.plushies.util.Recipes.getMaterial
import com.dre.brewery.BarrelWoodType
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice

val barrelRecipes = mutableListOf<BarrelRecipe>()

data class BarrelRecipe(
    val input: RecipeChoice,
    val barrelType: BarrelWoodType,
    val age: Float,
    val result: ItemStack
)

private val config: FoxConfig = Edulis.instance.registerConfig("aging_recipes", "recipes/aging.conf")

fun registerBarrelRecipes() {
    barrelRecipes.clear()
    for (r in config.getKeys()) {
        barrelRecipes.add(loadRecipe(r) ?: continue)
    }
}

private fun loadRecipe(path: String): BarrelRecipe? {
    val input = config.getString("$path.input")
        ?.let(::getInput)
        ?: return null

    val barrel = config.getString("$path.wood")
        ?.let(BarrelWoodType::parse)
        ?: BarrelWoodType.ANY

    val age = config.getDouble("$path.age")?.toFloat() ?: 1.0f

    val result = config.getString("$path.result")
        ?.let(::getMaterial)
        ?: return null

    return BarrelRecipe(input, barrel, age, result)
}