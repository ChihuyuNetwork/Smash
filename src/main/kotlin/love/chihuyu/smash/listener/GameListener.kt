package love.chihuyu.smash.listener

import love.chihuyu.smash.SmashAPI
import love.chihuyu.smash.SmashPlugin.Companion.SmashPlugin
import love.chihuyu.smash.SmashPlugin.Companion.gameTimer
import love.chihuyu.smash.SmashPlugin.Companion.inCountdown
import love.chihuyu.timerapi.timer.Timer
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.util.Vector
import kotlin.random.Random.Default.nextInt

object GameListener : Listener {

    @EventHandler
    fun onHit(e: EntityDamageByEntityEvent) {
        if (inCountdown || gameTimer == null) {
            e.isCancelled = true
            return
        }

        val player = e.entity as? Player ?: return
        val damager = e.damager

        SmashAPI.velocities[player.uniqueId] = SmashAPI.velocities[player.uniqueId]?.plus(nextInt(4, 10)) ?: 5
        player.velocity = damager.location.direction.multiply((SmashAPI.velocities[player.uniqueId] ?: 1) / 50.0).setY(0.5)
        SmashAPI.lastAttackers[player.uniqueId] = damager.uniqueId

        val mainScoreboard = SmashPlugin.server.scoreboardManager.mainScoreboard
        val objective = mainScoreboard.getObjective(DisplaySlot.BELOW_NAME) ?: mainScoreboard.registerNewObjective("%", "").apply {
            displaySlot = DisplaySlot.BELOW_NAME
        }
        objective.getScore(player.name).score = (SmashAPI.velocities[player.uniqueId] ?: 0)
        player.scoreboard = mainScoreboard

        Timer("smash-velocity-${player.uniqueId}", 10, 1)
            .tick {
                if ((SmashAPI.velocities[player.uniqueId] ?: 0) > 50 &&
                    (
                            !player.world.getBlockAt(player.location.apply { this.x -= .5 }).isEmpty ||
                                    !player.world.getBlockAt(player.location.apply { this.x += .5 }).isEmpty ||
                                    !player.world.getBlockAt(player.location.apply { this.z -= .5 }).isEmpty ||
                                    !player.world.getBlockAt(player.location.apply { this.x += .5 }).isEmpty ||
                                    !player.world.getBlockAt(player.location.apply { this.y += 1 }).isEmpty ||
                                    !player.world.getBlockAt(player.location.apply { this.y += 2 }).isEmpty
                            )
                ) {
                    mapOf(
                        player.world.getBlockAt(player.location.apply { this.x -= .5 }) to mapOf(
                            player.world.getBlockAt(player.location.apply { this.z -= .5 }) to listOf(
                                player.world.getBlockAt(player.location.apply { this.y += 2 }),
                                player.world.getBlockAt(player.location.apply { this.y += 1 }),
                                player.world.getBlockAt(player.location.apply { this.y })
                            ),
                            player.world.getBlockAt(player.location.apply { this.z }) to listOf(
                                player.world.getBlockAt(player.location.apply { this.y += 2 }),
                                player.world.getBlockAt(player.location.apply { this.y += 1 }),
                                player.world.getBlockAt(player.location.apply { this.y })
                            ),
                            player.world.getBlockAt(player.location.apply { this.z += .5 }) to listOf(
                                player.world.getBlockAt(player.location.apply { this.y += 2 }),
                                player.world.getBlockAt(player.location.apply { this.y += 1 }),
                                player.world.getBlockAt(player.location.apply { this.y })
                            )
                        ),
                        player.world.getBlockAt(player.location.apply { this.x += .5 }) to mapOf(
                            player.world.getBlockAt(player.location.apply { this.z -= .5 }) to listOf(
                                player.world.getBlockAt(player.location.apply { this.y += 2 }),
                                player.world.getBlockAt(player.location.apply { this.y += 1 }),
                                player.world.getBlockAt(player.location.apply { this.y })
                            ),
                            player.world.getBlockAt(player.location.apply { this.z }) to listOf(
                                player.world.getBlockAt(player.location.apply { this.y += 2 }),
                                player.world.getBlockAt(player.location.apply { this.y += 1 }),
                                player.world.getBlockAt(player.location.apply { this.y })
                            ),
                            player.world.getBlockAt(player.location.apply { this.z += .5 }) to listOf(
                                player.world.getBlockAt(player.location.apply { this.y += 2 }),
                                player.world.getBlockAt(player.location.apply { this.y += 1 }),
                                player.world.getBlockAt(player.location.apply { this.y })
                            )
                        )
                    ).forEach { (xBlock, z) ->
                        if (!xBlock.isEmpty) {
                            SmashAPI.brokenBlocks[xBlock.location] = xBlock.state.data.clone()
                            xBlock.type = Material.AIR
                            player.world.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                        }
                        z.forEach { (zBlock, y) ->
                            if (!zBlock.isEmpty) {
                                SmashAPI.brokenBlocks[zBlock.location] = zBlock.state.data.clone()
                                zBlock.type = Material.AIR
                                player.world.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                            }
                            y.forEach { yBlock ->
                                if (!yBlock.isEmpty) {
                                    SmashAPI.brokenBlocks[yBlock.location] = yBlock.state.data.clone()
                                    yBlock.type = Material.AIR
                                    player.world.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                                }
                            }
                        }
                    }
                }
            }.run()
    }

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        val player = e.player ?: return

        player.allowFlight = true
        if (player.isFlying && player.gameMode != GameMode.CREATIVE) {
            player.isFlying = false
            val uuid = player.uniqueId
            if (uuid !in SmashAPI.doubleJumpCooltimed) {
                player.velocity = player.velocity.setY(0.9).setX(player.location.direction.x * 0.9).setZ(player.location.direction.z * 0.9)
                SmashAPI.doubleJumpCooltimed.add(uuid)
                player.exp = 0f
                player.allowFlight = false
                player.world.playSound(player.location, Sound.ENDERDRAGON_WINGS, .5f, 1f)
            } else {
                player.velocity = player.velocity.add(Vector(.0, .01, .0))
            }
        }

        if (!player.world.getBlockAt(player.location.apply { this.y -= .1 }).isEmpty && player.uniqueId in SmashAPI.doubleJumpCooltimed) {
            SmashAPI.doubleJumpCooltimed.remove(player.uniqueId)
            player.exp = .999f
            player.allowFlight = true
        }

        if (player.location.y <= 0) {
            player.health = .0
            player.spigot().respawn()
            player.world.playSound(player.location, Sound.EXPLODE, 1f, 1f)
            SmashAPI.velocities[player.uniqueId] = 0
            val killer = SmashAPI.lastAttackers[player.uniqueId] ?: player.uniqueId
            if (killer != player.uniqueId) {
                SmashAPI.killCounts[killer] = SmashAPI.killCounts[killer]?.inc() ?: 1
                SmashAPI.lastAttackers.remove(player.uniqueId)
                val mainScoreboard = SmashPlugin.server.scoreboardManager.mainScoreboard
                (mainScoreboard.getObjective(DisplaySlot.PLAYER_LIST) ?: mainScoreboard.registerNewObjective("smash-kills", "").apply {
                    displaySlot = DisplaySlot.PLAYER_LIST
                }).getScore(Bukkit.getOfflinePlayer(killer)).score = SmashAPI.killCounts[killer] ?: 0
                SmashPlugin.server.onlinePlayers.forEach {
                    it.scoreboard = mainScoreboard
                }

                val objective = mainScoreboard.getObjective(DisplaySlot.BELOW_NAME) ?: mainScoreboard.registerNewObjective("%", "").apply {
                    displaySlot = DisplaySlot.BELOW_NAME
                }
                objective.getScore(player.name).score = (SmashAPI.velocities[player.uniqueId] ?: 0)
                player.scoreboard = mainScoreboard
            }
        }
    }

    @EventHandler
    fun onBreak(e: BlockBreakEvent) {
        if ((gameTimer == null || inCountdown) && e.player.gameMode != GameMode.CREATIVE) {
            e.isCancelled = true
            return
        }
        if (e.player.gameMode != GameMode.CREATIVE) SmashAPI.brokenBlocks[e.block.location] = e.block.state.data.clone()
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        player.health = .0
        player.spigot().respawn()
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        val player = e.entity as? Player ?: return
        if ((gameTimer == null || inCountdown) && player.gameMode != GameMode.CREATIVE) {
            e.isCancelled = true
            return
        }

        SmashAPI.velocities[player.uniqueId] = SmashAPI.velocities[player.uniqueId]?.plus(nextInt(4, 10)) ?: 5

        val mainScoreboard = SmashPlugin.server.scoreboardManager.mainScoreboard
        val objective = mainScoreboard.getObjective(DisplaySlot.BELOW_NAME) ?: mainScoreboard.registerNewObjective("%", "").apply {
            displaySlot = DisplaySlot.BELOW_NAME
        }
        objective.getScore(player.name).score = (SmashAPI.velocities[player.uniqueId] ?: 0)
        player.scoreboard = mainScoreboard
    }
}
