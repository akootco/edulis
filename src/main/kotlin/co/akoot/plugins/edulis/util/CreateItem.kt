package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.Edulis.Companion.pluginEnabled
import co.akoot.plugins.plushies.util.builders.FoodBuilder
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import com.dre.brewery.api.BreweryApi
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import java.util.*
import kotlin.collections.HashMap

object CreateItem {

    val resolvedResults: MutableMap<String, ItemStack> = HashMap()
    val pendingRecipes: MutableSet<String> = mutableSetOf()

    val foodKey = NamespacedKey("edulis", "food")

    // get recipe input items
    fun getInput(item: String, recipeName: String): RecipeChoice? {
        return when {
            // items created by flugin™
            item.startsWith("edulis:") ->
                resolvedResults[item.removePrefix("edulis:")]?.let {
                    RecipeChoice.ExactChoice(it)
                }

            // brewery items
            item.startsWith("brewery:") -> {
                // Check if the Brewery plugin is enabled
                if (pluginEnabled("Brewery")) {
                    BreweryApi.createBrewItem(BreweryApi.getRecipe(item.removePrefix("brewery:")
                            .replace("_", " ")), 10
                    )?.let { brewItem ->
                        RecipeChoice.ExactChoice(brewItem)
                    }
                } else {
                    pendingRecipes.add(recipeName.lowercase())
                    log.info("recipe pending: $recipeName")
                    return null
                }
            }

            // material tags
            item.startsWith("tag.") -> {
                val tag = when (item.removePrefix("tag.").uppercase()) {
                    "WOOL" -> Tag.WOOL
                    "LEAVES" -> Tag.LEAVES
                    "PLANKS" -> Tag.PLANKS
                    "LOGS" -> Tag.LOGS
                    else -> {
                        // if the tag is not found, skip.
                        // i would like this to handle every tag without having to list them eventually
                        log.error("$item not found, skipping ingredient")
                        return null
                    }
                }
                RecipeChoice.MaterialChoice(tag)
            }

            // if no prefix, check for vanilla material.
            else -> Material.getMaterial(item.uppercase())?.let { RecipeChoice.MaterialChoice(it) }
        }
    }

    // try to get material from config
    fun getMaterial(config: ConfigurationSection, path: String): ItemStack? {
        val materialName = config.getString(path)
        if (materialName == null) {
            log.error("Missing material at: $path")
            return null
        }

        val material = resolvedResults[materialName.removePrefix("edulis:")]
            ?: Material.getMaterial(materialName.uppercase(Locale.getDefault()))?.let { ItemStack(it) }

        if (material == null) {
            log.error("Invalid material at $path: $materialName")
            return null
        }
        return material
    }


    fun createItem(config: ConfigurationSection, recipeName: String): ItemStack? {
        val itemStack = getMaterial(config, "material") ?: return null

        // need to set pdc first, or else nothing else gets set.
        val itemWithPDC = ItemBuilder.builder(itemStack).apply {
                pdc(foodKey, recipeName)

                config.getStringList("attributes").joinToString(";").takeIf { it.isNotBlank() }
                    ?.let { pdc(NamespacedKey("edulis", "attributes"), it) }

            }.build()

        val item = ItemBuilder.builder(itemWithPDC).apply {

                config.getString("itemName")?.let { name ->
                    itemName(Text(name).component)
                }

                val amount = config.getInt("amount", 1)
                amount.takeIf { it != 1 }?.let {
                    itemStack.amount = it
                }

                config.getString("textures")?.let { id ->
                    headTexture(id)
                }

                config.getInt("customModelData").takeIf { it != 0 }?.let {
                    customModelData(it)
                }

                lore(config.getStringList("lore").map { Text(it).component })
                // stackSize needs to be 1-99 or else the server will explode (real)
                config.getInt("stackSize").takeIf { it in 1..99 }?.let {
                    stackSize(it)
                }

            }.build()

        if (config.contains("food")) {
            val foodItem = FoodBuilder.builder(item).apply {
                hunger(
                    config.getInt("food.hunger", 2),
                    config.getDouble("food.saturation", 2.0).toFloat(),
                    config.getDouble("food.eatTime", 1.6).toFloat()
                )

                if (config.getBoolean("food.isSnack")) isSnack()
                config.getDouble("food.tp").takeIf { it != 0.0 }?.let { range -> tp(range.toFloat()) }
                config.getString("food.sound.burp")?.let { afterEatSound(it.lowercase()) }
                config.getString("food.sound.eat")?.let { eatSound(it.lowercase()) }
            }.build()

            // Add the food item to resolvedResults
            resolvedResults[recipeName] = foodItem
            return foodItem
        } else {
            // Add the non-food item to resolvedResults
            resolvedResults[recipeName] = item
            return item
        }
    }
}