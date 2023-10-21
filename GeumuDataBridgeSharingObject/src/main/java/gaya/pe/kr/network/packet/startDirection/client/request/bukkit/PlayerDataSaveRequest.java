package gaya.pe.kr.network.packet.startDirection.client.request.bukkit;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.AbstractMinecraftRequestPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataResponse;
import gaya.pe.kr.network.packet.startDirection.server.response.PlayerDataSaveResponse;
import gaya.pe.kr.network.packet.type.ResponseType;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import io.netty.channel.Channel;
import lombok.Getter;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 플레이어 Quit 했을 때, Veloicty 측으로 저장 부탁합니다! 라고 왔을 때임.
 * @see gaya.pe.kr.network.packet.startDirection.server.response.PlayerDataSaveResponse 의 응답이다
 */

@Getter
public class PlayerDataSaveRequest extends AbstractMinecraftRequestPacket<PlayerDataSaveResponse> {

    String ip;
    int port;
    PlayerPersistentData playerPersistentData;
    boolean forcedSave = false;
    public PlayerDataSaveRequest(PlayerPersistentData playerPersistentData) {
        super(PacketType.PLAYER_DATA_SAVE_REQUEST);
        this.playerPersistentData = playerPersistentData;
    }

    public PlayerDataSaveRequest(PlayerPersistentData playerPersistentData, boolean forcedSave) {
        super(PacketType.PLAYER_DATA_SAVE_REQUEST);
        this.playerPersistentData = playerPersistentData;
        this.forcedSave = forcedSave;
    }

    @Override
    public void handleResponse(Supplier<PlayerDataSaveResponse> supplier, Channel sendTargetChannel) throws RuntimeException {
        sendTargetChannel.writeAndFlush(supplier.get());
    }
}
