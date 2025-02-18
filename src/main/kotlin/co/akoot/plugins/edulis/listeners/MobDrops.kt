package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.Edulis.Companion.mobDropConfig
import co.akoot.plugins.edulis.util.CreateItem.getMaterial
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Ageable
import org.bukkit.entity.Frog
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

class MobDrops : Listener {

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return
        val entity = event.entity
        val keys = mobDropConfig.getKeys()

        when (entity) {
            is Frog -> { // this should be okay to check first since baby frogs are tadpoles
                val variant = "FROG_${entity.variant.key.key.uppercase()}"
                if (keys.contains(variant)) {
                    mobDropConfig.getStringList(variant).forEach {
                        val dropItem = ItemStack(getMaterial(it) ?: return)
                        handleLooting(event, dropItem, killer)
                    }
                    return
                }
            }
        }

        if (entity is Ageable && !entity.isAdult) {
            val key = "${entity.type.name}_BABY"
            if (keys.contains(key)) {
                mobDropConfig.getStringList(key).forEach {
                    val dropItem = ItemStack(getMaterial(it) ?: return)
                    handleLooting(event, dropItem, killer)
                }
                return
            }
        }

        if (keys.contains(event.entityType.name)) { // handle all other mobs
            mobDropConfig.getStringList(event.entityType.name).forEach {
                val dropItem = ItemStack(getMaterial(it) ?: return)
                handleLooting(event, dropItem, killer)
            }
        }
    }

    private fun handleLooting(event: EntityDeathEvent, dropItem: ItemStack, killer: Player): Boolean {
        // get looting level
        val lootingLevel = killer.inventory.itemInMainHand.getEnchantmentLevel(Enchantment.LOOTING)

        if (lootingLevel == 0) {
            dropItem.amount = 1
        } else {
            // TODO: make looting better, this sucks
            dropItem.amount += (2 * lootingLevel)
        }
        // drop item
        return event.drops.add(dropItem)
    }
}