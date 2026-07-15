package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.plushies.util.Recipes.getMaterial
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import com.dre.brewery.api.BreweryApi
import com.dre.brewery.recipe.BRecipe
import com.dre.brewery.recipe.PluginItem
import com.dre.brewery.recipe.RecipeItem
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

// book
private val String.ccrgx get() = replace(Regex("&[0-9a-fk-orx]"), "")
private val RecipeItem.name: String
    get() = (this as? PluginItem)?.itemId?.ccrgx ?: configId ?: materials?.firstOrNull().toString()

fun brewBook(recipe: String): ItemStack? {
    val pages = mutableListOf<Component>()

    if (recipe == "all") {
        // create book with all recipes
        for (key in BRecipe.getRecipes()) {
            createPage(key.recipeName)?.let { pages.add(it) }
        }

        return ItemBuilder.builder(ItemStack(Material.WRITTEN_BOOK))
            .writtenBook(pages, "Brewery", generation = 3)
            .customModelData(48)
            .build()
    } else {
        // create book with single recipe
        return ItemBuilder.builder(ItemStack(Material.WRITTEN_BOOK))
            .writtenBook(createPage(recipe) ?: return null)
            .build()
    }
}

private fun createPage(recipe: String): Component? {
    val brewData = BRecipe.getMatching(recipe) ?: return null
    return (
            Text()
                // brew name
                .plus(
                    Text("${brewData.getName(10).ccrgx}\n\n")
                        .apply { // set hover event if the item exists
                            BreweryApi.createBrewItem(recipe, 10)
                                ?.let { hover(ItemStack(it)) }
                        }.underlined()
                )

                // ingredients
                .plus(Text().apply {
                    for (ingredient in brewData.ingredients) {
                        plus(Text(ingredient.name.replace(Regex("[_\\-]"), " ").lowercase()).apply {
                            // set a prefix only if the item is a brewery item, the brewery api is a bit odd innit!
                            val prefix = if (ingredient is PluginItem && ingredient.plugin == "brewery") "brewery:" else ""
                            // set hover event if the item exists
                            getMaterial("$prefix${ingredient.name}")
                                ?.let { hover(ItemStack(it)) }
                            // amount
                        }).plus(Text(": ${ingredient.amount}\n"))
                    }
                })

                .plus(Text("\nCooking Time: ${brewData.cookingTime} mins"))

                // age info
                .plus(Text().apply {
                    if (brewData.needsToAge()) {

                        plus(Text("\nWood: ${brewData.wood.name}"))
                            .plus("\nAge: ${brewData.age} years")
                    }
                })

                // distill info
                .plus(Text().apply {
                    if (brewData.needsDistilling()) {
                        plus(Text("\nDistill Runs: ${brewData.distillruns}"))
                    }
                })).component
}