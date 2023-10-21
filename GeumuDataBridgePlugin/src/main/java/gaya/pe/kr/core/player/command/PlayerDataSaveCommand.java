package gaya.pe.kr.core.player.command;

import gaya.pe.kr.core.GeumuDataBridgePlugin;
import gaya.pe.kr.core.network.manager.NetworkManager;
import gaya.pe.kr.core.player.manager.PlayerPersistentDataManager;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PlayerDataSaveCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if ( commandSender instanceof Player ) {

            Player player = ((Player) commandSender).getPlayer();

            if ( !player.isOp() ) {
                return false;
            }

        }


        PlayerPersistentDataManager.getInstance().allPlayerDataSave(commandSender, false);

        return false;
    }
}
