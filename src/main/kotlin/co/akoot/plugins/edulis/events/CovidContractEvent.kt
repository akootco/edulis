package co.akoot.plugins.edulis.events

import co.akoot.plugins.bluefox.api.events.FoxEventCancellable
import org.bukkit.entity.Player

class CovidContractEvent(val player: Player): FoxEventCancellable()