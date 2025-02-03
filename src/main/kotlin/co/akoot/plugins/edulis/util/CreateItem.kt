package co.akoot.plugins.edulis.util

import co.akoot.plugins.bluefox.util.Txt
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.util.loaders.ItemLoader.Companion.foodKey
import co.akoot.plugins.plushies.util.builders.FoodBuilder
import co.akoot.plugins.plushies.util.builders.ItemBuilder
import com.dre.brewery.api.BreweryApi
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import java.util.*

object CreateItem {

    val resolvedResults: MutableMap<String, ItemStack> = HashMap()

    // get recipe input items
    fun getInput(item: String): RecipeChoice? {
        return when {
            // items created by flugin™
            item.startsWith("edulis:") ->
                resolvedResults[item.removePrefix("edulis:")]?.let {
                    RecipeChoice.ExactChoice(it)
                }

            // brewery items
            item.startsWith("brewery:") -> {
                BreweryApi.createBrewItem(
                    BreweryApi.getRecipe(item.removePrefix("brewery:").replace("_", " ")),
                    10
                )?.let {
                    RecipeChoice.ExactChoice(it)
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
        }
        return material
    }


    fun createItem(config: ConfigurationSection, recipeName: String): ItemStack? {
        val itemStack = getMaterial(config, "material") ?: return null

        // need to set pdc first, or else nothing else gets set.
        val item = ItemBuilder.builder(itemStack).pdc(foodKey, recipeName).apply {
            config.getString("itemName")?.let { name ->
                itemName(Txt(name).c)
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

            lore(config.getStringList("lore").map { Component.text(it) })

            // stackSize needs to be 1-99 or else the server will explode (real)
            config.getInt("stackSize").takeIf { it in 1..99 }?.let {
                stackSize(it)
            }

        }.build()

        if (config.contains("food")) {
            val foodItem = FoodBuilder.builder(item).apply {
                hunger(
                    config.getInt("food.hunger", 4),
                    config.getDouble("food.saturation", 4.5).toFloat(),
                    config.getDouble("food.eatTime", 1.6).toFloat()
                )

                if (config.getBoolean("food.isSnack", false)) isSnack()
                config.getDouble("food.tp").takeIf { it != 0.0 }?.let { range -> tp(range.toFloat()) }
                config.getString("food.sound.burp", "entity.player.burp")
                    ?.let { sound -> afterEatSound(sound.lowercase()) }
                config.getString("food.sound.eat", "entity.generic.eat")?.let { sound -> eatSound(sound.lowercase()) }
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