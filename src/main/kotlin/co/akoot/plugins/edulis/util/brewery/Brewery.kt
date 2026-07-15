package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.edulis.Edulis.Companion.brewConfig
import co.akoot.plugins.edulis.util.Util.foodid
import com.dre.brewery.BarrelWoodType
import com.dre.brewery.api.BreweryApi
import com.dre.brewery.configuration.sector.capsule.ConfigRecipe
import com.dre.brewery.recipe.BRecipe
import com.dre.brewery.recipe.PluginItem
import org.bukkit.inventory.ItemStack

class BrewItems : PluginItem() {
    override fun matches(item: ItemStack): Boolean { return item.foodid == itemId }
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