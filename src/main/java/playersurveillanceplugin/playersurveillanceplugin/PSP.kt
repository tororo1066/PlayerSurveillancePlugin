package playersurveillanceplugin.playersurveillanceplugin

import org.bukkit.plugin.java.JavaPlugin

class PSP : JavaPlugin() {

    companion object{
        lateinit var plugin : PSP
        const val prefix = "§f§l[§a§lP§7§lS§b§4§lP§f§l]§r"
    }
    override fun onEnable() {
        plugin = this
        server.pluginManager.registerEvents(EventListener,this)
        getCommand("psp")?.setExecutor(Command)
        saveDefaultConfig()
    }




}