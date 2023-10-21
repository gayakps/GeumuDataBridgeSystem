package gaya.pe.kr.util;

import gaya.pe.kr.core.GeumuDataBridgePlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

public class EventUtil {

    private static final PluginManager PLUGIN_MANAGER = Bukkit.getPluginManager();

    public static void call(Event event) {
        PLUGIN_MANAGER.callEvent(event);
    }

    public static void register(Listener listener) {
        PLUGIN_MANAGER.registerEvents(listener, GeumuDataBridgePlugin.getPlugin());
    }




}
