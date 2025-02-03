package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.util.loaders.ConfigLoader
import co.akoot.plugins.edulis.util.CreateItem.resolvedResults
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

class MobDrops : Listener {

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val killer = event.entity.killer ?: return

        val dropsConfig = ConfigLoader.mobDropsConfig

        dropsConfig.getKeys(false).forEach { mobKey ->
            // get the mob's config
            val mobConfig = dropsConfig.getConfigurationSection(mobKey) ?: return
            val name = mobConfig.getString("mob") ?: return

            // if mob matches, handle the drop
            if (name == event.entityType.name) {
                val dropItem: ItemStack = resolvedResults[mobKey] ?: return
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