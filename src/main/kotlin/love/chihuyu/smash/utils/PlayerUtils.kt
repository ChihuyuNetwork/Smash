package love.chihuyu.smash.utils

import love.chihuyu.smash.SmashPlugin
import org.bukkit.entity.Player

object PlayerUtils {

    fun showAllPlayer(player: Player) {
        SmashPlugin.SmashPlugin.server.onlinePlayers.forEach {
            it.showPlayer(player)
            player.showPlayer(it)
        }
    }
}