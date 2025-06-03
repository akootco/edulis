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
import co.akoot.plugins.edulis.events.CovidContractEvent
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
        val remainingTime = getPDC<Long>(endKey) ?: return "$name is not sick."
        return "$name has ${getTimeString(remainingTime.minus(System.currentTimeMillis()))} remaining"
    }

val Player.isImmune: Boolean
    get() = this.getPDC<Byte>(immuneKey) != null

fun giveCovid(
    player: Player,
    plugin: FoxPlugin,
    wasCaught: Boolean = false,
    spreader: String? = null,
    resume: Boolean = false
) {
    if (player.isImmune || player.scoreboardTags.contains("CITIZENS_NPC")) return

    CovidContractEvent(player).fire() ?: return

    player.apply {
        Text(this) {
            Kolor.ERROR("You have been infected") +
                    if (wasCaught) (Kolor.ERROR(" by ") + Kolor.ERROR.player(spreader ?: "someone"))
                    else Kolor.ERROR("!")
        }

        val endTime =
            if (resume) {
                val remainingTime = getPDC<Long>(remainingKey)
                removePDC(remainingKey)
                System.currentTimeMillis() + (remainingTime ?: parseTime("30m"))
            } else {
                System.currentTimeMillis() + parseTime("30m")
            }

        setPDC(endKey, endTime)
    }
    covidTask[player] = Covid(player, plugin).runTaskTimer(plugin, 1L, 100L)
    log.info("${player.name} has been infected")
}

fun resumeCovid(player: Player, plugin: FoxPlugin) {
    if (player.isInfected) giveCovid(player, plugin, resume = true)
}

fun pauseCovid(player: Player) {
    if (!player.isInfected) return

    val remainingTimeMillis = player.getPDC<Long>(endKey)
        ?.minus(System.currentTimeMillis())

    player.setPDC(remainingKey, remainingTimeMillis)
    player.saveData()

    covidTask[player]?.cancel()
    covidTask.remove(player)
    log.info("${player.name}'s contagion has been paused")
}