package love.chihuyu.smash.listener

import love.chihuyu.smash.SmashAPI
import love.chihuyu.smash.SmashPlugin.Companion.SmashPlugin
import love.chihuyu.smash.SmashPlugin.Companion.gameTimer
import love.chihuyu.smash.SmashPlugin.Companion.inCountdown
import love.chihuyu.timerapi.utils.Schedular.sync
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
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

        SmashAPI.velocities[player.uniqueId] = SmashAPI.velocities[player.uniqueId]?.plus(nextInt(1, 7)) ?: 5
        try {
            player.velocity = damager.location.direction.multiply((SmashAPI.velocities[player.uniqueId] ?: 1) / 100).setY(0.6)
        } catch (_: Throwable) { }
        SmashAPI.lastAttackers[player.uniqueId] = damager.uniqueId

        val mainScoreboard = SmashPlugin.server.scoreboardManager.mainScoreboard
        val objective = mainScoreboard.getObjective(DisplaySlot.BELOW_NAME) ?: mainScoreboard.registerNewObjective("%", "").apply {
            displaySlot = DisplaySlot.BELOW_NAME
        }
        objective.getScore(player.name).score = (SmashAPI.velocities[player.uniqueId] ?: 0)
        player.scoreboard = mainScoreboard

        SmashPlugin.sync(3) {
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
                        player.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                    }
                    z.forEach { (zBlock, y) ->
                        if (!zBlock.isEmpty) {
                            SmashAPI.brokenBlocks[zBlock.location] = zBlock.state.data.clone()
                            zBlock.type = Material.AIR
                            player.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                        }
                        y.forEach { yBlock ->
                            if (!yBlock.isEmpty) {
                                SmashAPI.brokenBlocks[yBlock.location] = yBlock.state.data.clone()
                                yBlock.type = Material.AIR
                                player.playSound(player.location, Sound.ZOMBIE_WOODBREAK, 1f, 1f)
                            }
                        }
                    }
                }
            }
        }
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
            player.world.spawnEntity(player.location, EntityType.LIGHTNING)
            SmashAPI.velocities[player.uniqueId] = 0
            val killer = SmashAPI.lastAttackers[player.uniqueId]
            if (killer != player.uniqueId && killer != null) {
                SmashAPI.killCounts[killer] = SmashAPI.killCounts[killer]?.inc() ?: 1
                val mainScoreboard = SmashPlugin.server.scoreboardManager.mainScoreboard
                (
                    mainScoreboard.getObjective(DisplaySlot.PLAYER_LIST) ?: mainScoreboard.registerNewObjective("smash-kills", "").apply {
                        displaySlot = DisplaySlot.PLAYER_LIST
                    }
                    ).getScore(Bukkit.getOfflinePlayer(killer)).score = SmashAPI.killCounts[killer] ?: 0
                SmashPlugin.server.onlinePlayers.forEach {
                    it.scoreboard = mainScoreboard
                }
            }
        }
    }
}
