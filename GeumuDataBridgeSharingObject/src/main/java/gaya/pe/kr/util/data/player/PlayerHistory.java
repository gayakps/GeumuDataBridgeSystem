package gaya.pe.kr.util.data.player;

import gaya.pe.kr.network.packet.type.PlayerConnectionLogType;
import gaya.pe.kr.util.data.BukkitLocation;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;


@Getter
public class PlayerHistory implements Serializable {

    String name;

    UUID playerUUID;

    int historyID;
    String server;
    PlayerDataForHistory playerDataForHistory;
    BukkitLocation bukkitLocation;
    PlayerConnectionLogType logType;
    Date date = new Date();

    public PlayerHistory(String name, UUID playerUUID, int historyID, String server, PlayerDataForHistory playerDataForHistory, BukkitLocation bukkitLocation, PlayerConnectionLogType logType) {
        this.name = name;
        this.playerUUID = playerUUID;
        this.historyID = historyID;
        this.server = server;
        this.playerDataForHistory = playerDataForHistory;
        this.bukkitLocation = bukkitLocation;
        this.logType = logType;
    }


    public PlayerHistory(String name, UUID playerUUID, int historyID, String server, PlayerDataForHistory playerDataForHistory, BukkitLocation bukkitLocation, PlayerConnectionLogType logType, Date date) {
        this.name = name;
        this.playerUUID = playerUUID;
        this.historyID = historyID;
        this.server = server;
        this.playerDataForHistory = playerDataForHistory;
        this.bukkitLocation = bukkitLocation;
        this.logType = logType;
        this.date = date;
    }
}
