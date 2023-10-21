package gaya.pe.kr.network.packet.startDirection.client.send;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.type.PlayerConnectionLogType;
import gaya.pe.kr.util.data.BukkitLocation;
import gaya.pe.kr.util.data.player.PlayerDataForHistory;
import gaya.pe.kr.util.data.player.PlayerHistory;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import lombok.Getter;

import java.util.Date;
import java.util.UUID;

/**
 * 플레이어의 로그를 저장요청 ( 버킷 측에서 자동으로 하는 것임 )
 *
 */

@Getter
public class PlayerConnectionHistorySaveRequest extends AbstractMinecraftPacket {

    String ip;
    int port;

    String playerName;
    UUID uuid;

    PlayerDataForHistory playerDataForHistory;
    BukkitLocation bukkitLocation;
    PlayerConnectionLogType logType;

    Date date = new Date();


    public PlayerConnectionHistorySaveRequest(String ip, int port, String playerName, UUID uuid, PlayerDataForHistory playerDataForHistory, BukkitLocation bukkitLocation, PlayerConnectionLogType logType) {
        super(PacketType.PLAYER_CONNECTION_HISTORY_SAVE_REQUEST);
        this.ip = ip;
        this.port = port;
        this.playerName = playerName;
        this.uuid = uuid;
        this.playerDataForHistory = playerDataForHistory;
        this.bukkitLocation = bukkitLocation;
        this.logType = logType;
    }
}
