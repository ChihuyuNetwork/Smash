package love.chihuyu.smash

import org.bukkit.Location
import org.bukkit.material.MaterialData
import java.util.*

object SmashAPI {

    val killCounts = mutableMapOf<UUID, Int>()
    val velocities = mutableMapOf<UUID, Int>()
    val doubleJumpCooltimed = mutableListOf<UUID>()
    val lastAttackers = mutableMapOf<UUID, UUID>()
    val brokenBlocks = mutableMapOf<Location, MaterialData>()
}
