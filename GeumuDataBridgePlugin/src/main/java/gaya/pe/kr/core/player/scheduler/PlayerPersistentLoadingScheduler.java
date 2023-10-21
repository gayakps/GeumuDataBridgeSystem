package gaya.pe.kr.core.player.scheduler;

import gaya.pe.kr.core.player.manager.PlayerPersistentDataManager;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.nio.Buffer;
import java.util.*;

public class PlayerPersistentLoadingScheduler implements Runnable {

    PlayerPersistentDataManager playerPersistentDataManager = PlayerPersistentDataManager.getInstance();

    @Override
    public void run() {

        HashMap<UUID, PlayerPersistentData> loadingPlayer = playerPersistentDataManager.getPlayerLoadWaitingData();

        List<UUID> removeTarget = new ArrayList<>();

        loadingPlayer.forEach((uuid, playerPersistentData) -> {

            Player bukkitPlayer = Bukkit.getPlayer(uuid);
            if ( bukkitPlayer != null ) {
                playerPersistentDataManager.overridePlayerData(bukkitPlayer, playerPersistentData);
                removeTarget.add(uuid);
            }

        });

        for (UUID uuid : removeTarget) {
            loadingPlayer.remove(uuid);
        }

    }
}
