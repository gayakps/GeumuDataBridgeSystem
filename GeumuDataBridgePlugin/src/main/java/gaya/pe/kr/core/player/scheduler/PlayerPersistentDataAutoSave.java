package gaya.pe.kr.core.player.scheduler;

import gaya.pe.kr.core.network.manager.NetworkManager;
import gaya.pe.kr.core.player.manager.PlayerPersistentDataManager;
import gaya.pe.kr.network.packet.startDirection.client.send.PlayerConnectionHistorySaveRequest;
import gaya.pe.kr.network.packet.type.PlayerConnectionLogType;
import gaya.pe.kr.util.data.BukkitLocation;
import gaya.pe.kr.util.data.player.PlayerHistory;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class PlayerPersistentDataAutoSave implements Runnable {

    NetworkManager networkManager = NetworkManager.getInstance();
    @Override
    public void run() {
        PlayerPersistentDataManager playerPersistentDataManager = PlayerPersistentDataManager.getInstance();
        Bukkit.getOnlinePlayers().parallelStream().forEach( player -> {

            PlayerPersistentData playerPersistentData = PlayerPersistentDataManager.getInstance().getNowPlayerPersistentData(player);

            playerPersistentDataManager.sendSaveTicket(playerPersistentData, true); // 데이터 저장 요청

            Location location = player.getLocation();
            BukkitLocation bukkitLocation = new BukkitLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
            PlayerConnectionHistorySaveRequest playerConnectionHistorySaveRequest = new PlayerConnectionHistorySaveRequest("127.0.0.1", Bukkit.getPort(), player.getName(), player.getUniqueId(), playerPersistentDataManager.getPlayerDataForHistory(player), bukkitLocation, PlayerConnectionLogType.AUTO_UPDATE);

            networkManager.sendPacket(playerConnectionHistorySaveRequest);
        });



    }
}
