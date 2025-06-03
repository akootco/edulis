package co.akoot.plugins.edulis.listeners.tasks

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.edulis.Edulis.Companion.pluginEnabled
import com.dre.brewery.BPlayer
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.random.Random

class Covid(private val player: Player, private val plugin: FoxPlugin) : BukkitRunnable() {

    private var lastEffectTime = 0L

    override fun run() {
        val endTime = player.getPDC<Long>(endKey)

        if (endTime == null || System.currentTimeMillis() > endTime) {
            player.apply {
                sendMessage("You are no longer contagious.")
                clearActivePotionEffects()
                setPDC<Byte>(caughtKey, 1)
                removePDC(endKey)
            }

            covidTask.remove(player)
            return cancel() // cancel the task
        }

        // 50% chance to trigger effect, every 3 minutes
        if (System.currentTimeMillis() - lastEffectTime >= 180 * 1000 && Random.nextBoolean()) {
            // reset timer
            lastEffectTime = System.currentTimeMillis()
            player.chat("*cough*")

            // 50% chance on which effect to give
            if (Random.nextBoolean()) {
                if (pluginEnabled("brewery")) {
                    BPlayer.addPuke(player, 64)
                } else {
                    player.addPotionEffects(listOf(
                        PotionEffect(PotionEffectType.DARKNESS, 200, 10),
                        PotionEffect(PotionEffectType.POISON, 100, 0)
                    ))
                }
            }
        }

        for (target in player.getNearbyEntities(3.0, 3.0, 3.0)) { // spread it
            if (target is Player && !target.isInfected) {
                giveCovid(target, plugin, true, player.name)
            }
        }

        player.addPotionEffects(listOf(
                PotionEffect(PotionEffectType.WEAKNESS, -1, 4),
                PotionEffect(PotionEffectType.UNLUCK, -1, 0)
            )
        )
    }
}