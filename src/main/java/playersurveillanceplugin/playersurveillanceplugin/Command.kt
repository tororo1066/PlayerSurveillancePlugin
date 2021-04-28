package playersurveillanceplugin.playersurveillanceplugin

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import playersurveillanceplugin.playersurveillanceplugin.PSP.Companion.plugin
import playersurveillanceplugin.playersurveillanceplugin.PSP.Companion.prefix
import playersurveillanceplugin.playersurveillanceplugin.Util.int
import playersurveillanceplugin.playersurveillanceplugin.Util.argscheck
import playersurveillanceplugin.playersurveillanceplugin.Util.loctp
import playersurveillanceplugin.playersurveillanceplugin.Util.perm
import playersurveillanceplugin.playersurveillanceplugin.Util.playertp
import playersurveillanceplugin.playersurveillanceplugin.Util.selectplayertp
import playersurveillanceplugin.playersurveillanceplugin.Util.send
import java.util.*

object Command : CommandExecutor {

    var tpnow = false

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player)return true
        if (!sender.perm("psp.admin"))return true
        if (args.isEmpty()){

        }
        when(args[0]){
            "help"->{
                sender.sendMessage("""
                    §a§l=========PlayerSurveillancePlugin===================
                    §a/psp cameraplayer (プレイヤー名) カメラのプレイヤーを設定できます
                    §a/psp loop (Int) カメラのtp周期を設定できます(Int*20)
                    §a/psp loc help ロケーション関連のhelpを表示します
                    §a/psp player help プレイヤー関連のhelpを表示します
                    §a§l=========PlayerSurveillancePlugin===Author:tororo_1066
                """.trimIndent())
            }
            "cameraplayer","camerap"->{
                if (!args.argscheck(2,sender))return true
                val p = Bukkit.getPlayer(args[1])
                if (p == null){
                    sender.sendMessage(prefix + "このプレイヤーは存在しません！")
                    return true
                }
                if (!p.isOnline){
                    sender.sendMessage(prefix + "このプレイヤーはオフラインです！")
                    return true
                }
                plugin.config.set("cameraplayer",p.uniqueId.toString())
                plugin.saveConfig()
                sender.send("configを変更しました")
                return true
            }
            "loc"->{
                when(args[1]){
                    "help"->{
                        sender.sendMessage("""
                            §a§l=========PlayerSurveillancePlugin(loc menu)===========
                            §a/psp loc set そこにいる位置、向きにtp先を保存します
                            §a/psp loc show 現在あるlocを表示します
                            §a/psp loc delete (Int or null) 指定した、または全てのlocデータを消します
                            §a/psp loc tp 指定したカメラをloc順に動かします
                            §a§l=========PlayerSurveillancePlugin===Author:tororo_1066
                        """.trimIndent())
                        return true
                    }

                    "setloc","set"-> {
                        val l = plugin.config.getStringList("cameraloc")
                        l.add("${sender.world.uid}:${sender.location.x}:${sender.location.y}:${sender.location.z}:${sender.location.pitch}:${sender.location.yaw}")
                        plugin.config.set("cameraloc", l)
                        plugin.saveConfig()
                        sender.send("§alocationを追加しました")
                        return true
                    }
                    "showloc","show"->{
                        val l = plugin.config.getStringList("cameraloc")
                        sender.send("§acameraloc一覧")
                        for ((int, i) in l.withIndex()){
                            sender.sendMessage("$int $i")
                        }
                        return true
                    }
                    "deleteloc","delete"->{
                        if (args.size == 2){
                            val l = plugin.config.getStringList("cameraloc")
                            l.clear()
                            plugin.config.set("cameraloc",l)
                            plugin.saveConfig()
                            sender.send("§acameralocを全て削除しました")
                            return true
                        }
                        if (!args.argscheck(3,sender))return true
                        if (args[2].int(sender) == null)return true
                        val l = plugin.config.getStringList("cameraloc")
                        if (l.size <= args[2].toInt()){
                            sender.send("§40~${l.size}の間で指定してください")
                            return true
                        }
                        l.removeAt(args[2].toInt())
                        plugin.config.set("cameraloc",l)
                        plugin.saveConfig()
                        sender.send("${args[2].toInt()}番を削除しました")
                        return true

                    }
                    "loctp","tp"-> {
                        if (!args.argscheck(2,sender))return true
                        if (tpnow) {
                            tpnow = false
                            sender.send("§a監視をキャンセルしました")
                            return true
                        }
                        if (!Bukkit.getPlayer(UUID.fromString(plugin.config.getString("cameraplayer")))?.isOnline!!) {
                            sender.send("§4cameraplayerがオフラインです")
                            return true
                        }
                        tpnow = true
                        val p = Bukkit.getPlayer(UUID.fromString(plugin.config.getString("cameraplayer")))!!
                        loctp(sender,p,0)
                    }
                }
            }

            "loop"->{
                if (!args.argscheck(2,sender))return true
                plugin.config.set("cameraloop",args[1].toIntOrNull()?:return true)
                plugin.saveConfig()
                sender.send("§acameraloopを変更しました")
                return true
            }


            "player","p"->{
                if (args.size < 2)return true
                when(args[1]){
                    "help"->{
                        sender.sendMessage("""
                            §a§l=========PlayerSurveillancePlugin(player menu)========
                            §a/psp p dis プレイヤーと話す距離を設定します
                            §a/psp p add (プレイヤー名) tpするプレイヤーを追加します(tpのみ)
                            §a/psp p except (プレイヤー名) tpを除外するプレイヤーを追加します(alltpのみ)
                            §a/psp p show (player or except) 設定されているものを見れます
                            §a/psp p delete (player or except) (Int or null)
                            §a指定した、または全てのデータを削除します
                            §a/psp p tp カメラプレイヤーをaddで追加したプレイヤーにtpします
                            §a/psp p alltp 全てのプレイヤーにtpします(exceptは除く)
                            §a§l=========PlayerSurveillancePlugin===Author:tororo_1066
                        """.trimIndent())
                        return true
                    }
                    "distance","dis"->{
                        if (!args.argscheck(3,sender))return true
                        plugin.config.set("playerdistance",args[2].int(sender) ?: return true)
                        plugin.saveConfig()
                        sender.send("§aplayerdistnceを変更しました")
                        return true
                    }
                    "addplayer","addp","add"->{
                        if (!args.argscheck(3,sender))return true
                        val p = Bukkit.getPlayer(args[2])
                        if (p == null){
                            sender.send("プレイヤーが存在しません！")
                            return true
                        }
                        if (!p.isOnline){
                            sender.send("プレイヤーがオフラインです！")
                            return true
                        }
                        val l = plugin.config.getStringList("playertp")
                        l.add(p.uniqueId.toString())
                        plugin.config.set("playertp",l)
                        plugin.saveConfig()
                        sender.send("§aplayertpを追加しました")
                        return true
                    }
                    "exceptplayer","exceptp","except"->{
                        if (!args.argscheck(3,sender))return true
                        val p = Bukkit.getPlayer(args[2])
                        if (p == null){
                            sender.send("プレイヤーが存在しません！")
                            return true
                        }
                        if (!p.isOnline){
                            sender.send("プレイヤーがオフラインです！")
                            return true
                        }
                        val l = plugin.config.getStringList("excepttp")
                        l.add(p.uniqueId.toString())
                        plugin.config.set("excepttp",l)
                        plugin.saveConfig()
                        sender.send("§aexcepttpを追加しました")
                    }
                    "alltp"->{
                        if (!args.argscheck(2,sender))return true
                        if (tpnow){
                            tpnow = false
                            sender.send("§a監視をキャンセルしました")
                            return true
                        }
                        tpnow = true
                        val p = Bukkit.getPlayer(UUID.fromString(plugin.config.getString("cameraplayer")))!!
                        playertp(sender,p,0)
                    }
                    "tp"->{
                        if (!args.argscheck(2,sender))return true
                        if (tpnow){
                            tpnow = false
                            sender.send("§a監視をキャンセルしました")
                            return true
                        }
                        tpnow = true
                        val p = Bukkit.getPlayer(UUID.fromString(plugin.config.getString("cameraplayer")))!!
                        selectplayertp(sender,p,0)
                    }

                    "showp","show"->{
                        if (!args.argscheck(3,sender))return true
                        when(args[2]){
                            "except"->{
                                val l = plugin.config.getStringList("excepttp")
                                sender.send("§aexcepttp一覧")
                                for ((int, i) in l.withIndex()){
                                    sender.sendMessage("$int $i")
                                }
                            }
                            "player"->{
                                val l = plugin.config.getStringList("playertp")
                                sender.send("§aplayertp一覧")
                                for ((int, i) in l.withIndex()){
                                    sender.sendMessage("$int $i")
                                }
                            }
                            else->return true
                        }

                        return true
                    }
                    "deletep","delete"->{

                        if (args.size == 3){
                            when(args[2]){
                                "except"->{
                                    val l = plugin.config.getStringList("excepttp")
                                    l.clear()
                                    plugin.config.set("excepttp",l)
                                    plugin.saveConfig()
                                    sender.send("§cexcepttpを全て削除しました")
                                    return true
                                }
                                "player"->{
                                    val l = plugin.config.getStringList("playertp")
                                    l.clear()
                                    plugin.config.set("playertp",l)
                                    plugin.saveConfig()
                                    sender.send("§cplayertpを全て削除しました")
                                    return true
                                }
                            }

                        }
                        when(args[2]){
                            "except"->{
                                if (!args.argscheck(4,sender))return true
                                if (args[3].int(sender) == null)return true
                                val l = plugin.config.getStringList("excepttp")
                                if (l.size <= args[3].toInt()){
                                    sender.send("§40~${l.size}の間で指定してください")
                                    return true
                                }
                                l.removeAt(args[3].toInt())
                                plugin.config.set("excepttp",l)
                                plugin.saveConfig()
                                sender.send("§cexcepttpの${args[3].toInt()}番を削除しました")
                                return true
                            }
                            "player"->{
                                if (!args.argscheck(4,sender))return true
                                if (args[3].int(sender) == null)return true
                                val l = plugin.config.getStringList("playertp")
                                if (l.size <= args[3].toInt()){
                                    sender.send("§40~${l.size}の間で指定してください")
                                    return true
                                }
                                l.removeAt(args[3].toInt())
                                plugin.config.set("playertp",l)
                                plugin.saveConfig()
                                sender.send("§cplayertpの${args[3].toInt()}番を削除しました")
                                return true
                            }
                        }


                    }
                }
            }
        }
        return true
    }
}