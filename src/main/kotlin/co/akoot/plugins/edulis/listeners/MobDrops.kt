package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.bluefox.util.runLater
import co.akoot.plugins.edulis.Edulis.Companion.mobDropConfig
import co.akoot.plugins.edulis.util.Materials.getMaterial
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Ageable
import org.bukkit.entity.Bat
import org.bukkit.entity.Frog
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class MobDrops : Listener {

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val keys = mobDropConfig.getKeys()
        val name = entity.type.name

        when (entity) {
            is Frog -> { // this should be okay to check first since baby frogs are tadpoles
                val key = "FROG_${entity.variant.key.key.uppercase()}"
                if (keys.contains(key)) {
                    return handleDrop(event, key)
                }
            }

            is Bat -> {
                if (Random.nextDouble() < 0.13) {
                   return handleDrop(event, event.entityType.name, false)
                }
                return
            }
        }

        // es bebe?
        if (entity is Ageable && !entity.isAdult) {
            val key = "${name}_BABY"
            if (keys.contains(key)) {
                return handleDrop(event, key)
            }
        }

        // handle other mobs
        if (keys.contains(event.entityType.name)) {
            return handleDrop(event, event.entityType.name)
        }
    }

    private fun handleDrop(event: EntityDeathEvent, key: String, looting: Boolean = true) {
        runLater(1) { // run 1 tick later or else fire ticks will always be -1
            val item = handleFire(event, key) ?: return@runLater
            val killer = event.entity.killer

            // looting
            if (killer != null && looting) {
                // this still SUCKS
                val lootingLevel = killer.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.LOOTING) + 1
                item.amount += lootingLevel
            }

            // for some reason using runlater stops the item from dropping
            // gotta use dropItem instead
            val loc = event.entity.location
            loc.world.dropItemNaturally(loc.add(0.0,0.5,0.0), item)
        }
    }

    private fun handleFire(event: EntityDeathEvent, mob: String): ItemStack? {
        val name = if (event.entity.fireTicks > -1) "${mob}.cookedDrops" else "${mob}.drops"

        mobDropConfig.getStringList(name).forEach {
            return ItemStack(getMaterial(it) ?: return null)
        }

        return null
    }
}