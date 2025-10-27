package co.akoot.plugins.edulis.listeners

import co.akoot.plugins.edulis.util.Materials.matches
import co.akoot.plugins.edulis.util.VillagerTrades.modifyTrader
import co.akoot.plugins.edulis.util.VillagerTrades.modifyVillager
import co.akoot.plugins.plushies.util.Items.customItems
import co.akoot.plugins.plushies.util.isCustomBlock
import io.papermc.paper.event.entity.EntityInsideBlockEvent
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Goat
import org.bukkit.entity.Parrot
import org.bukkit.entity.Villager
import org.bukkit.entity.WanderingTrader
import org.bukkit.entity.Cow
import org.bukkit.entity.Player
import org.bukkit.entity.Entity
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
                when {
                    item.type.matches(Material.BUCKET) -> {
                        val milk = customItems["goat_milk"]?: return
                        event.isCancelled = true
                        player.inventory.itemInMainHand.amount -= 1
                        player.give(milk)
                    }
                    item.type.matches(Material.GLASS_BOTTLE) -> { milkBottle(player, entity) }
                    else -> return
                }
            }

            is Cow -> {
                if (item.type == Material.GLASS_BOTTLE) { milkBottle(player, entity) }
            }

            is Parrot -> {
                if (item.type.matches(Material.BUCKET)) {
                    val milk = customItems["bird_spit"]?: return
                    event.isCancelled = true
                    player.inventory.itemInMainHand.amount -= 1
                    entity.location.world.playSound(entity.location, Sound.ENTITY_LLAMA_SPIT, 0.5f, 2f)
                    player.give(milk)
                }
            }
        }
    }

    @EventHandler
    fun inBlock(event: EntityInsideBlockEvent) {
        val block = event.block
        event.isCancelled = (block.type == Material.SWEET_BERRY_BUSH && block.isCustomBlock)
    }

    private fun milkBottle(player: Player, entity: Entity) {
        val milkBottle = customItems["milk_bottle"] ?: return
        entity.location.world.playSound(entity.location, Sound.ENTITY_COW_MILK, 0.5f, 2f)
        player.inventory.itemInMainHand.amount -= 1
        player.give(milkBottle.asOne())
    }
}