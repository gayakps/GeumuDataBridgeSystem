package gaya.pe.kr.core;

import gaya.pe.kr.core.history.manager.HistoryServiceManager;
import gaya.pe.kr.core.listener.CoreSystemListener;
import gaya.pe.kr.core.network.manager.NetworkManager;
import gaya.pe.kr.core.player.manager.PlayerPersistentDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.logging.Level;

public final class GeumuDataBridgePlugin extends JavaPlugin {

    static Plugin plugin;

    NetworkManager networkManager = NetworkManager.getInstance();
    PlayerPersistentDataManager playerPersistentDataManager = PlayerPersistentDataManager.getInstance();

    HistoryServiceManager historyServiceManager = HistoryServiceManager.getInstance();

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        playerPersistentDataManager.init();
        networkManager.init();
        historyServiceManager.init();
        registerEvent(new CoreSystemListener());

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    public static void log(String message) {
        getPlugin().getLogger().log(Level.INFO, ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void registerEvent(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        log(String.format("&f[&6&l%s&f]의 클래스가 정상적으로 이벤트 핸들러에 등록됐습니다", listener.getClass().getName()));

    }

    public static void registerCommand(String command, CommandExecutor commandExecutor) {
        Bukkit.getPluginCommand(command).setExecutor(commandExecutor);
        log(String.format("&f[&6&l%s&f]의 클래스가 정상적으로 커맨드 핸들러에 등록됐습니다 커맨드 : &f[&6&l%s&f]", commandExecutor.getClass().getName(), command));
    }

    public static void registerTabCommand(String command, TabCompleter tabCompleter) {
        Bukkit.getPluginCommand(command).setTabCompleter(tabCompleter);
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static BukkitScheduler getBukkitScheduler() {
        return Bukkit.getScheduler();
    }

    public static void msg(Player player, String... s) {

        for (String s1 : s) {
            player.sendMessage(String.format("&c&l| SYSTEM &f%s", s1).replace("&", "§"));
        }

    }


}
