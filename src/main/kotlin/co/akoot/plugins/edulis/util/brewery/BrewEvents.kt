package co.akoot.plugins.edulis.util.brewery

import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.quote
import co.akoot.plugins.bluefox.util.runLater
import co.akoot.plugins.edulis.Edulis
import co.akoot.plugins.edulis.Edulis.Companion.key
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.edulis.listeners.tasks.foodEaten
import co.akoot.plugins.plushies.util.Recipes.getMaterial
import com.dre.brewery.BarrelWoodType
import com.dre.brewery.api.events.PlayerPukeEvent
import com.dre.brewery.api.events.barrel.BarrelAccessEvent
import com.dre.brewery.recipe.PluginItem
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import kotlin.random.Random

class BrewEvents : Listener {

    private var ItemStack.age: Float?
        get() = itemMeta?.getPDC(key("aging.time"))
        set(value) {
            if (value != null) editMeta {
                it.setPDC(key("aging.time"), value)
                it.lore(listOf(quote("Aged for ${value.toInt()} year${if (value == 1f) "" else "s"}")))
            }
        }

    @EventHandler
    fun onBarrelAccess(event: BarrelAccessEvent) {
        val barrel = event.barrel

        barrel.inventory.contents.forEachIndexed { slot, item ->
            if (item == null) return@forEachIndexed

            val recipe = barrelRecipes.firstOrNull {
                it.input.test(item) &&
                        (it.barrelType == BarrelWoodType.ANY || it.barrelType == barrel.wood)
            } ?: return@forEachIndexed

            val age = (item.age ?: 0f) + barrel.time
            item.age = age

            if (age >= recipe.age) {
                barrel.inventory.setItem(slot, recipe.result.clone())
            }
        }
    }

    @EventHandler
    fun PlayerPukeEvent.onPuke() {
        val foods = player.foodEaten
        if (foods.isEmpty()) return

        isCancelled = true
        for (i in 0 until count) {
            runLater(2L * i) {
                val pukeItem = getMaterial(foods.random())?.clone() ?: return@runLater
                val loc = player.location.apply {
                    y += 1.1
                    pitch = pitch - 10 + Random.nextInt(20)
                    yaw = yaw - 10 + Random.nextInt(20)
                }

                pukeItem.setData(DataComponentTypes.MAX_STACK_SIZE, 1)

                player.world.dropItem(loc, pukeItem).apply {
                    velocity = loc.getDirection().multiply(0.5)
                    owner = UUID.fromString("78277c50-4e17-48bf-af38-7a25143da732") // PENJAMIN
                    ticksLived = 5600 + (0..50).random()
                    setMetadata("brewery_puke", FixedMetadataValue(Edulis.instance, true))
                }
            }
        }
    }


    @EventHandler
    fun onBreweryEnable(event: PluginEnableEvent) {
        // we cant load the recipes until brewery is enabled smh
        if (event.plugin.name == "BreweryX") {
            PluginItem.registerForConfig("edulis") { BrewItems() }
            loadBrewRecipes()
            registerBarrelRecipes()
            log.info("Loaded Brewery Recipes!")
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun InventoryPickupItemEvent.pukeItems() {
        isCancelled = item.hasMetadata("brewery_puke")
    }
}