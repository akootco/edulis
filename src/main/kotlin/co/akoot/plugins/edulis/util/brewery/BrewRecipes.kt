package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.util.loaders.ConfigLoader.brewRecipesConfig
import com.dre.brewery.api.BreweryApi
import com.dre.brewery.recipe.BCauldronRecipe
import com.dre.brewery.recipe.BRecipe

object BrewRecipes {

    fun loadBrewRecipes() {

        val recipeSection = brewRecipesConfig.getConfigurationSection("recipes") ?: run {
            log.info("No recipes section found in config.")
            return
        }

        for (key in recipeSection.getKeys(false)) {
            val recipe = BRecipe.fromConfig(recipeSection, key)
            if (recipe == null) {
                log.warn("Skipping invalid recipe: $key")
                continue
            }
            BreweryApi.addRecipe(recipe, false)
        }

        val cauldronSection = brewRecipesConfig.getConfigurationSection("cauldron") ?: run {
            log.info("No cauldron section found in config.")
            return
        }

        for (key in cauldronSection.getKeys(false)) {
            val cauldronRecipe = BCauldronRecipe.fromConfig(cauldronSection, key)
            if (cauldronRecipe == null) {
                log.warn("Skipping invalid cauldron recipe: $key")
                continue
            }
            BreweryApi.addCauldronRecipe(cauldronRecipe, false)
        }
    }
}