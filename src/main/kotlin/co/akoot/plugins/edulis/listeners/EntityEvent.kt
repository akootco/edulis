package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.util.Materials.matches
import co.akoot.plugins.edulis.util.Materials.resolvedResults
import co.akoot.plugins.edulis.util.VillagerTrades.modifyTrader
import co.akoot.plugins.edulis.util.VillagerTrades.modifyVillager
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Goat
import org.bukkit.entity.Parrot
import org.bukkit.entity.Villager
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent

class EntityEvent : Listener {

    @EventHandler
    fun interactAtEntity(event: PlayerInteractEntityEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand

        when (val entity = event.rightClicked) {
            is WanderingTrader -> {
                modifyTrader(entity)
            }

            is Villager -> {
                if (item.type.matches(Material.CAKE) && entity.profession in listOf(Villager.Profession.NONE, Villager.Profession.NITWIT)) {
                    modifyVillager(entity)
                }
            }

            is Goat -> {
                if (item.type.matches(Material.BUCKET)) {
                    val milk = resolvedResults["goat_milk"]?: return
                    event.isCancelled = true
                    player.inventory.itemInMainHand.amount -= 1
                    player.give(milk)
                }
            }
            is Parrot -> {
                if (item.type.matches(Material.BUCKET)) {
                    val milk = resolvedResults["bird_spit"]?: return
                    event.isCancelled = true
                    player.inventory.itemInMainHand.amount -= 1
                    entity.location.world.playSound(entity.location, Sound.ENTITY_LLAMA_SPIT, 0.5f, 2f)
                    player.give(milk)
                }
            }
        }
    }
}