package co.akoot.plugins.edulis.listeners.tasks

import co.akoot.plugins.bluefox.api.FoxPlugin
import co.akoot.plugins.bluefox.api.Kolor
import co.akoot.plugins.bluefox.extensions.getPDC
import co.akoot.plugins.bluefox.extensions.invoke
import co.akoot.plugins.bluefox.extensions.removePDC
import co.akoot.plugins.bluefox.extensions.setPDC
import co.akoot.plugins.bluefox.util.Text
import co.akoot.plugins.bluefox.util.TimeUtil.getTimeString
import co.akoot.plugins.bluefox.util.TimeUtil.parseTime
import co.akoot.plugins.edulis.Edulis.Companion.log
import co.akoot.plugins.plushies.util.Util.pl
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

val covidTask = mutableMapOf<Player, BukkitTask>()
val endKey = NamespacedKey("covid", "end")
val remainingKey = NamespacedKey("covid", "remaining")
val caughtKey = NamespacedKey("covid", "experienced")
val immuneKey = NamespacedKey("covid", "immune")

val Player.isInfected: Boolean
    get() = this.getPDC<Long>(endKey) != null

val Player.timeUntilCured: String
    get() {
        val remainingTime = getPDC<Long>(remainingKey) ?: return "$name is not sick."
        return getTimeString(remainingTime)
    }

val Player.isImmune: Boolean
    get() = this.getPDC<Byte>(immuneKey) != null

fun giveCovid(player: Player, plugin: FoxPlugin, wasCaught: Boolean = false, spreader: String? = null) {

    if (player.isInfected ||
        player.isImmune ||
        player.scoreboardTags.contains("CITIZENS_NPC")
    ) return

    player.apply {
        Text(this) {
            Kolor.ERROR("You have been infected") +
                    if (wasCaught) (Kolor.ERROR(" by ") + Kolor.ERROR.player(spreader ?: "someone"))
                    else Kolor.ERROR("!")
        }

        setPDC(endKey, System.currentTimeMillis() + parseTime("30m")) // set the end time to 30 minutes
    }
    covidTask[player] = Covid(player, plugin).runTaskTimer(plugin, 1L, 100L)
    pl.logger.info("${player.name} has been infected")
}

fun resumeCovid(player: Player, plugin: FoxPlugin) {
    val remainingTime = player.getPDC<Long>(remainingKey) ?: return

    player.apply {
        removePDC(remainingKey)
        setPDC(endKey, System.currentTimeMillis() + remainingTime)
    }

    log.info("${player.name}'s contagion has been resumed, ${player.timeUntilCured} remaining")
    giveCovid(player, plugin)
}

fun pauseCovid(player: Player) {
    val endTime = player.getPDC<Long>(endKey)?: return

    val remainingTimeMillis = endTime.minus(System.currentTimeMillis())

    player.setPDC(remainingKey, remainingTimeMillis)
    player.saveData() // need to run this or the pdc isn't saved

    covidTask[player]?.cancel()
    covidTask.remove(player)
    log.info("${player.name}'s contagion has been paused")
}