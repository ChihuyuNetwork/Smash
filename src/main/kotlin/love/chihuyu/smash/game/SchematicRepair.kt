package love.chihuyu.smash.game

import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.session.ClipboardHolder
import love.chihuyu.smash.SmashPlugin.Companion.SmashPlugin
import love.chihuyu.smash.SmashPlugin.Companion.mapsConfig
import java.io.File
import java.io.FileInputStream

object SchematicRepair {

    fun recovery(map: String) {
        val file = File(SmashPlugin.dataFolder, "../WorldEdit/schematics/$map.schematic")
        val format = ClipboardFormat.findByFile(file)
        val world = SmashPlugin.server.worlds[0]
        val clipboard = try {
            format!!.getReader(FileInputStream(file)).read(BukkitWorld(SmashPlugin.server.worlds[0]).worldData)
        } catch (e: Throwable) {
            e.printStackTrace()
            return
        }
        val mapVector = mapsConfig.getVector("maps.$map.center")
        val worldEditPlugin = WorldEditPlugin.getPlugin(WorldEditPlugin::class.java)
        val operation = ClipboardHolder(clipboard, BukkitWorld(world).worldData)
            .createPaste(
                worldEditPlugin.createEditSession(SmashPlugin.server.onlinePlayers.filter { it.isOnline }[0]),
                BukkitWorld(world).worldData
            )
            .to(Vector(mapVector.x, mapVector.y, mapVector.z))
            .ignoreAirBlocks(false)
            .build()
        Operations.completeBlindly(operation)
    }
}
