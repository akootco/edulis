package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.edulis.Edulis.Companion.brewConfig
import co.akoot.plugins.edulis.Edulis.Companion.foodKey
import co.akoot.plugins.edulis.util.Materials.getMaterial
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import com.dre.brewery.BarrelWoodType
import com.dre.brewery.api.BreweryApi
import com.dre.brewery.configuration.sector.capsule.ConfigRecipe
import com.dre.brewery.recipe.BRecipe
import com.dre.brewery.recipe.PluginItem
import com.dre.brewery.recipe.RecipeItem
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class BrewItems : PluginItem() {
    override fun matches(item: ItemStack): Boolean {
        return item.itemMeta.getPDC<String>(foodKey) == itemId // lol
    }
}

fun loadBrewRecipes() {
    for (recipe in brewConfig.getKeys()) {
        val name = brewConfig.getString("$recipe.name") ?: continue
        val cookTime = brewConfig.getInt("$recipe.cookTime") ?: 2
        val ingredients = brewConfig.getStringList("$recipe.ingredients").takeIf { it.isNotEmpty() } ?: continue

        val builder = ConfigRecipe.builder()
            .name(name)
            .cookingTime(cookTime)
            .ingredients(ingredients)

        brewConfig.getInt("$recipe.age")?.let {
            builder.age(it)
            builder.wood(BarrelWoodType.fromName(brewConfig.getString("$recipe.wood") ?: "any"))
        }

        if (brewConfig.getInt("$recipe.distillRuns") != null || brewConfig.getInt("$recipe.distillTime") != null) {
            val runs = brewConfig.getInt("$recipe.distillRuns") ?: 1
            val time = brewConfig.getInt("$recipe.distillTime") ?: 10
            builder.distillRuns(runs)
            builder.distillTime(time)
        }

        brewConfig.getString("$recipe.color")?.let { builder.color(it) }
        brewConfig.getInt("$recipe.difficulty")?.let { builder.difficulty(it) }
        brewConfig.getInt("$recipe.alcohol")?.let { builder.alcohol(it) }
        brewConfig.getStringList("$recipe.lore").takeIf { it.isNotEmpty() }?.let { builder.lore(it) }

        val brewRecipe = BRecipe.fromConfig(recipe, builder.build())?: continue // good one

        BRecipe.getMatching(recipe)?.let { BreweryApi.removeRecipe(it.recipeName) }
        BreweryApi.addRecipe(brewRecipe, false)
    }
}

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