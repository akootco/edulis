package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.bluefox.util.*
import co.akoot.plugins.edulis.util.Materials.getMaterial
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import com.dre.brewery.api.BreweryApi
import com.dre.brewery.recipe.BRecipe
import com.dre.brewery.recipe.PluginItem
import com.dre.brewery.recipe.RecipeItem
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object BrewBook {

    private val String.ccrgx get() = replace(Regex("&[0-9a-fk-orx]"), "")

    private val woodMap = mapOf(
        0 to "Any",
        1 to "Birch",
        2 to "Oak",
        3 to "Jungle",
        4 to "Spruce",
        5 to "Acacia",
        6 to "Dark Oak",
        7 to "Crimson",
        8 to "Warped",
        9 to "Mangrove",
        10 to "Cherry",
        11 to "Bamboo",
        12 to "Pale Oak"
    )

    fun brewBook(recipe: String): ItemStack? {
        val pages = mutableListOf<Component>()

        if (recipe == "all") {
            // create book with all recipes
            for (key in BRecipe.getAllRecipes()) {
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
        val brewData = BRecipe.get(recipe) ?: return null

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
                                getMaterial("$prefix${ingredient.name}", recipeName = recipe)
                                    ?.let { hover(ItemStack(it)) }
                                // amount
                            }).plus(Text(": ${ingredient.amount}\n"))
                        }
                    })

                    .plus(Text("\nCooking Time: ${brewData.cookingTime} mins"))

                    // age info
                    .plus(Text().apply {
                        if (brewData.needsToAge()) {

                            plus(Text("\nWood: ${brewData.woodString}"))
                                .plus("\nAge: ${brewData.age.toInt()} years")
                        }
                    })

                    // distill info
                    .plus(Text().apply {
                        if (brewData.needsDistilling()) {
                            plus(Text("\nDistill Runs: ${brewData.distillRuns}"))
                        }
                    })).component
    }

    private val RecipeItem.name: String
        get() = (this as? PluginItem)?.itemId?.ccrgx ?: configId ?: materials?.first().toString()

    private val BRecipe.woodString: String
        get() = woodMap[this.wood.toInt()] ?: "Unknown"
}
