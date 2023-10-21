package gaya.pe.kr.core.listener;

import gaya.pe.kr.core.GeumuDataBridgePlugin;
import gaya.pe.kr.core.player.manager.PlayerPersistentDataManager;
import gaya.pe.kr.thread.SchedulerUtil;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

public class CoreSystemListener implements Listener {

    @EventHandler
    public void closeCommand(ServerCommandEvent event) {

        String command = event.getCommand();

        if ( command.equals("stop") || command.equals("/stop")) {

            event.setCancelled(true);

            Plugin plugin = GeumuDataBridgePlugin.getPlugin();

            Server server =  plugin.getServer();

            server.setWhitelist(true);

            CommandSender commandSender = event.getSender();

            commandSender.sendMessage("1초 뒤 서버 종료 시퀸스 동작");

            SchedulerUtil.runLaterTask( ()-> {

                commandSender.sendMessage("------------------------------------- 서버 종료 시퀀스 작동");
                commandSender.sendMessage("WhiteList ON");

                PlayerPersistentDataManager playerPersistentDataManager = PlayerPersistentDataManager.getInstance();

                playerPersistentDataManager.getPlayerConnectionListener().unregister();
                PlayerPersistentDataManager.getInstance().allPlayerDataSave(commandSender, true);

                commandSender.sendMessage("WhiteList OFF");
                commandSender.sendMessage("------------------------------------- 서버 종료 시퀀스 종료");

                server.setWhitelist(false);

                server.shutdown();
            }, 20);

        }

    }


}
