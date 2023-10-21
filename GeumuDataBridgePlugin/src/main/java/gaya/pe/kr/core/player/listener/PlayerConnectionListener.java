package gaya.pe.kr.core.player.listener;

import gaya.pe.kr.GayaSoftMain;
import gaya.pe.kr.core.network.manager.NetworkManager;
import gaya.pe.kr.core.player.manager.PlayerPersistentDataManager;
import gaya.pe.kr.network.packet.startDirection.client.send.PlayerConnectionHistorySaveRequest;
import gaya.pe.kr.network.packet.type.PlayerConnectionLogType;
import gaya.pe.kr.thread.SchedulerUtil;
import gaya.pe.kr.util.PoketmonUtil;
import gaya.pe.kr.util.data.BukkitLocation;
import gaya.pe.kr.util.data.player.PlayerHistory;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import net.minecraft.server.v1_16_R3.NBTCompressedStreamTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;

import java.util.UUID;
import java.util.stream.Collectors;

import static gaya.pe.kr.core.GeumuDataBridgePlugin.log;


public class PlayerConnectionListener implements Listener {

    PlayerPersistentDataManager playerPersistentDataManager = PlayerPersistentDataManager.getInstance();

    @EventHandler ( priority = EventPriority.LOWEST )
    public void aQuitPlayer(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if ( playerPersistentDataManager.isSuccessLoadingPlayer(player.getUniqueId()) ) {
            NetworkManager networkManager = NetworkManager.getInstance();
            PlayerPersistentData playerPersistentData = playerPersistentDataManager.getNowPlayerPersistentData(player);

            Location location = player.getLocation();
            BukkitLocation bukkitLocation = new BukkitLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            PlayerConnectionHistorySaveRequest playerConnectionHistorySaveRequest = new PlayerConnectionHistorySaveRequest("127.0.0.1", Bukkit.getPort(), player.getName(), player.getUniqueId(), playerPersistentDataManager.getPlayerDataForHistory(player), bukkitLocation, PlayerConnectionLogType.LOGOUT);
            networkManager.sendPacket(playerConnectionHistorySaveRequest);
            playerPersistentDataManager.sendSaveTicket(playerPersistentData, true);

        }
    }

    @EventHandler ( priority = EventPriority.LOWEST )
    public void aJoinPlayer(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        player.sendTitle("§c!", "로딩 중..", 10, 200, 10);

        SchedulerUtil.runLaterTask( ()-> {

            if ( !playerPersistentDataManager.isSuccessLoadingPlayer(player.getUniqueId()) ) {
                player.kickPlayer("로딩에 실패하였습니다 재접속 해주세요");
            }

        }, 20*10);

    }


    public void unregister() {
        HandlerList.unregisterAll(this);
    }


}
