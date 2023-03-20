package love.chihuyu.smash.commands

import love.chihuyu.smash.SmashAPI
import love.chihuyu.smash.SmashPlugin.Companion.SmashPlugin
import love.chihuyu.smash.SmashPlugin.Companion.gameTimer
import love.chihuyu.smash.SmashPlugin.Companion.inCountdown
import love.chihuyu.smash.SmashPlugin.Companion.mapsConfig
import love.chihuyu.smash.SmashPlugin.Companion.prefix
import love.chihuyu.timerapi.timer.Timer
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.util.Vector

object SmashCommand : Command("smash") {
    override fun onCommand(sender: CommandSender, label: String, args: Array<out String>) {
        if (sender !is Player || !sender.hasPermission("smash.command.smash") || args.size > 2) return

        when (args[0]) {
            "start" -> {
                sender.sendMessage(
                    try {
                        if (gameTimer != null) "$prefix ${ChatColor.RED}既にゲームは開始されています"
                        val map = mapsConfig.getConfigurationSection("maps.${args[1]}")

                        gameTimer = Timer("Smash-Game", SmashPlugin.config.getLong("gameDuration"), 20)
                            .start {
                                inCountdown = false
                                SmashPlugin.server.broadcast(TextComponent("$prefix ${ChatColor.BOLD}Game Start"))
                                SmashPlugin.server.onlinePlayers.forEach {
                                    it.maximumNoDamageTicks = if (SmashPlugin.config.getBoolean("nodelay")) 0 else 20
                                }
                            }
                            .tick {
                                val mainScoreboard = SmashPlugin.server.scoreboardManager.mainScoreboard
                                mainScoreboard.getObjective(DisplaySlot.SIDEBAR)?.unregister()
                                val objective = mainScoreboard.registerNewObjective("${ChatColor.GOLD}${ChatColor.BOLD}   Smash   ", "").apply {
                                    displaySlot = DisplaySlot.SIDEBAR
                                }
                                objective.getScore("").score = 0
                                objective.getScore("マップ: ${args[1]}").score = 1
                                objective.getScore("残り時間 ${(duration - elapsed).floorDiv(60)}:${"%02d".format((duration - elapsed) % 60)}").score = 2
                                objective.getScore(" ").score = 3
                                SmashPlugin.server.onlinePlayers.forEach {
                                    it.scoreboard = mainScoreboard
                                }

                                SmashPlugin.server.onlinePlayers.forEach {
                                    it.setBedSpawnLocation((map.getList("spawns") as List<Vector>).map { spawn -> spawn.toLocation(sender.world) }.random(), true)
                                }
                            }
                            .end {
                                val scores = mutableMapOf<Int, String>()
                                SmashPlugin.server.onlinePlayers.forEach {
                                    val mainScoreboard = SmashPlugin.server.scoreboardManager.mainScoreboard
                                    mainScoreboard.getObjective(DisplaySlot.BELOW_NAME)?.unregister()
                                    val score = (
                                        mainScoreboard.getObjective(DisplaySlot.PLAYER_LIST) ?: mainScoreboard.registerNewObjective("smash-kills", "").apply {
                                            displaySlot = DisplaySlot.PLAYER_LIST
                                        }
                                        ).getScore(it.name)
                                    scores[score.score] = score.entry
                                    SmashAPI.velocities[it.uniqueId] = 0
                                    SmashAPI.killCounts[it.uniqueId] = 0
                                    it.setBedSpawnLocation(SmashPlugin.config.getVector("lobby-spawn").toLocation(sender.world), true)
                                    it.world.setGameRuleValue("showDeathMessages", "false")
                                    it.health = .0
                                    it.world.setGameRuleValue("showDeathMessages", "true")
                                    it.spigot().respawn()
                                }
                                SmashAPI.brokenBlocks.forEach {
                                    sender.world.getBlockAt(it.key).setType(it.value.itemType, false)
                                    sender.world.getBlockAt(it.key).state.rawData = it.value.data
                                    sender.world.getBlockAt(it.key).state.update(true)
                                }
                                SmashAPI.brokenBlocks.clear()
                                SmashPlugin.server.broadcast(TextComponent("$prefix 勝者は${scores.toList().sortedByDescending { it.first }[0].second}です！"))
                                gameTimer = null
                            }

                        Timer("Smash-Countdown", 6, 20)
                            .start {
                                SmashPlugin.server.onlinePlayers.forEachIndexed { index, player ->
                                    val mainScoreboard = SmashPlugin.server.scoreboardManager.mainScoreboard
                                    (
                                        mainScoreboard.getObjective(DisplaySlot.PLAYER_LIST) ?: mainScoreboard.registerNewObjective("smash-kills", "").apply {
                                            displaySlot = DisplaySlot.PLAYER_LIST
                                        }
                                        ).getScore(player.name).score = 0
                                    player.teleport((map.getList("spawns") as List<Vector>).map { spawn -> spawn.toLocation(sender.world) }[index % map.getList("spawns").size.dec()])
                                    player.gameMode = GameMode.SURVIVAL
                                }
                                inCountdown = true
                            }
                            .tick {
                                SmashPlugin.server.onlinePlayers.forEach {
                                    it.playSound(it.location, Sound.ORB_PICKUP, 1f, 1f)
                                }
                                if (elapsed != 6L) SmashPlugin.server.broadcast(TextComponent("$prefix ${ChatColor.BOLD}${duration - elapsed}"))
                            }
                            .end {
                                gameTimer!!.run()
                            }.run()

                        "$prefix ゲームを開始しました"
                    } catch (e: Throwable) {
                        "$prefix ${ChatColor.RED}ゲーム開始に失敗しました"
                    }
                )
            }
            "end" -> {
                if (gameTimer == null) "$prefix ${ChatColor.RED}ゲームは開始されていません"
                gameTimer!!.kill()
                gameTimer = null
            }
        }
    }

    override fun onTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String> {
        return when (args.size) {
            1 -> listOf("start", "end")
            2 -> mapsConfig.getConfigurationSection("maps").getKeys(false).toList()
            else -> emptyList()
        }
    }
}
