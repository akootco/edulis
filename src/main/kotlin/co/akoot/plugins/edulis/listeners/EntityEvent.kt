package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.util.VillagerTrades.modifyTrader
import co.akoot.plugins.edulis.util.VillagerTrades.modifyVillager
import org.bukkit.Material
import org.bukkit.entity.Villager
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

class EntityEvent : Listener {

    @EventHandler
    fun interactAtEntity(event: PlayerInteractAtEntityEvent) {
        val player = event.player
        when (val entity = event.rightClicked) {
            is WanderingTrader -> {
                modifyTrader(entity)
            }

            is Villager -> {
                val item = player.inventory.itemInMainHand
                if (item.type == Material.CAKE && entity.profession == Villager.Profession.NONE) {
                    modifyVillager(entity)
                }
            }
        }
    }
}