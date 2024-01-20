package net.vnleng.gulag;

import net.vnleng.gulag.commands.GulagSettingsCommand;
import net.vnleng.gulag.commands.GulagZoneCommand;
import net.vnleng.gulag.events.PlayerWorldEvent;
import net.vnleng.gulag.utils.Settings;
import net.vnleng.gulag.zone.GulagHandler;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GulagPlugin extends JavaPlugin {

    public static GulagPlugin current_instance;
    private PlayerWorldEvent pwe;
    private Settings stts;

    @Override
    public void onEnable() {
        current_instance = this;
        pwe=new PlayerWorldEvent();

        stts= Settings.getInstance();
        stts.initDefaults();
        stts.saveConfig();

        GulagHandler.getInstance();
        saveDefaultConfig();

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(pwe,this);


        setupCommands();
    }

    private void setupCommands(){
        PluginCommand gulagsettings = getServer().getPluginCommand("gulagsettings");
        GulagSettingsCommand x =new GulagSettingsCommand();
        if (gulagsettings != null) {
            gulagsettings.setExecutor(x);
            gulagsettings.setTabCompleter(x);
        }

        PluginCommand gulagtest = getServer().getPluginCommand("gulagtest");
        GulagZoneCommand gzc = new GulagZoneCommand();
        if(gulagtest!=null){
            gulagtest.setExecutor(gzc);
        }
    }

    @Override
    public void onDisable() {
        //Rimuovo il listener gloabale
        pwe.unregister();
        //Salvataggio impostazioni
        stts.saveConfig();
    }
}
