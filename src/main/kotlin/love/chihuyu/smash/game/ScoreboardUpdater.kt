package love.chihuyu.smash.game

import love.chihuyu.smash.SmashAPI
import love.chihuyu.smash.SmashPlugin.Companion.SmashPlugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot

object ScoreboardUpdater {

    fun updateVelocity(player: Player) {
        val mainScoreboard = SmashPlugin.server.scoreboardManager.mainScoreboard
        val objective = mainScoreboard.getObjective(DisplaySlot.BELOW_NAME) ?: mainScoreboard.registerNewObjective("%", "").apply {
            displaySlot = DisplaySlot.BELOW_NAME
        }
        objective.getScore(player.name).score = (SmashAPI.velocities[player.uniqueId] ?: 0)
        player.scoreboard = mainScoreboard
    }

    fun updateAllVelocity() {
        SmashPlugin.server.onlinePlayers.forEach(this::updateVelocity)
    }

    fun updateKillCounts(player: Player) {
        val killer = SmashAPI.lastAttackers[player.uniqueId] ?: player.uniqueId
        val mainScoreboard = SmashPlugin.server.scoreboardManager.mainScoreboard
        (
            mainScoreboard.getObjective(DisplaySlot.PLAYER_LIST) ?: mainScoreboard.registerNewObjective("smash-kills", "").apply {
                displaySlot = DisplaySlot.PLAYER_LIST
            }
            ).getScore(Bukkit.getOfflinePlayer(killer)).score = SmashAPI.killCounts[killer] ?: 0
        player.scoreboard = mainScoreboard
    }

    fun updateAllKillCounts() {
        SmashPlugin.server.onlinePlayers.forEach(this::updateKillCounts)
    }
}
