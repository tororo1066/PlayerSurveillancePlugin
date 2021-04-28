package playersurveillanceplugin.playersurveillanceplugin

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import playersurveillanceplugin.playersurveillanceplugin.PSP.Companion.plugin
import java.util.*
import kotlin.collections.ArrayList

object Util {

    var tpplayer : Player? = null
    fun Player.perm(p : String) : Boolean{
       return if (!this.hasPermission(p)){
           this.send("§4あなたは権限がありません")
           false
       }else true
    }

    fun String.int(p : Player) : Int? {
        val int = this.toIntOrNull()
        return if (int == null){
            p.send("§4'${this}'を数字にすることができませんでした")
            null
        }else int
    }

    fun Player.send(s : String){
        this.sendMessage(PSP.prefix + s)
    }

    fun Array<out String>.argscheck(i : Int, p : Player): Boolean {
        return if (this.size != i){
            p.send("args.sizeを$i にしてください！")
            false
        }else true
    }

    fun loctp(sender : Player, p : Player, c : Int){
        val l = plugin.config.getStringList("cameraloc")
        object : BukkitRunnable(){
            override fun run() {
                if (!Command.tpnow){
                    cancel()
                    return
                }

                if (!p.isOnline){
                    sender.send("cameraplayerがオフラインになりました")
                    Command.tpnow = false
                    cancel()
                    return
                }
                val loc = Location(Bukkit.getWorld(UUID.fromString(l[c].split(":")[0])),l[c].split(":")[1].toDouble(),l[c].split(":")[2].toDouble(),l[c].split(":")[3].toDouble(),l[c].split(":")[4].toFloat(),l[c].split(":")[5].toFloat())
                p.teleport(loc)
                if (l.size == c+1){
                    loctp(sender, p, 0)
                    cancel()
                    return
                }
                loctp(sender,p,c+1)
                cancel()
                return
            }
        }.runTaskLater(plugin,plugin.config.getLong("cameraloop")*20)
    }
    fun playertp(sender : Player, p : Player, c : Int){
        val l = ArrayList<Player>(Bukkit.getOnlinePlayers())
        if (plugin.config.getStringList("excepttp").contains(l[c].uniqueId.toString())) playertp(sender,p, c+1)
        val dis = plugin.config.getDouble("playerdistance")
        object : BukkitRunnable(){
            override fun run() {
                if (!Command.tpnow){
                    cancel()
                    return
                }

                if (!p.isOnline){
                    sender.send("cameraplayerがオフラインになりました")
                    Command.tpnow = false
                    cancel()
                    return
                }
                p.teleport(l[c].location.add(dis,dis,dis))
                if (l.size <= c+1){
                    playertp(sender,p, 0)
                    cancel()
                    return
                }
                playertp(sender,p, c+1)
                cancel()
                return
            }
        }.runTaskLater(plugin,plugin.config.getLong("cameraloop")*20)
    }

    fun selectplayertp(sender : Player, p : Player, c : Int){
        val dis = plugin.config.getDouble("playerdistance")
        val l = ArrayList<String>(plugin.config.getStringList("playertp"))
        object : BukkitRunnable(){
            override fun run() {
                if (!Command.tpnow){
                    cancel()
                    return
                }

                if (!p.isOnline){
                    sender.send("cameraplayerがオフラインになりました")
                    Command.tpnow = false
                    cancel()
                    return
                }
                if (Bukkit.getPlayer(UUID.fromString(l[c])) == null || !Bukkit.getPlayer(UUID.fromString(l[c]))?.isOnline!!){
                    if (l.size <= c+1){
                        selectplayertp(sender,p, 0)
                        cancel()
                        return
                    }else{
                        selectplayertp(sender,p, c+1)
                        cancel()
                        return
                    }
                }
                Bukkit.getPlayer(UUID.fromString(l[c]))?.location?.add(dis,dis,dis)?.let { p.teleport(it) }
                if (l.size <= c+1){
                    selectplayertp(sender,p, 0)
                    cancel()
                    return
                }
                selectplayertp(sender,p, c+1)
                cancel()
                return
            }
        }.runTaskLater(plugin,plugin.config.getLong("cameraloop")*20)

    }

}