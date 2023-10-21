package gaya.pe.kr.network.packet.startDirection.server.request;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.AbstractMinecraftRequestPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataClientLoadResponse;
import gaya.pe.kr.network.packet.type.ResponseType;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import io.netty.channel.Channel;
import lombok.Getter;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 플레이어 데이터를 이동할 서버로 송신하는것 ( 로딩을 대기함 )
 * @see gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataClientLoadResponse 를 답변으로 함
 */
@Getter
public class PlayerDataClientLoadRequest extends AbstractMinecraftRequestPacket<PlayerDataClientLoadResponse> {
    PlayerPersistentData playerPersistentData;
    public PlayerDataClientLoadRequest(PlayerPersistentData playerPersistentData) {
        super(PacketType.PLAYER_DATA_CLIENT_LOAD_REQUEST);
        this.playerPersistentData = playerPersistentData;
    }

    @Override
    public void handleResponse(Supplier<PlayerDataClientLoadResponse> playerDataClientLoadResponseConsumer, Channel sendTargetChannel) throws RuntimeException {
        long packetId = getPacketID();
        PlayerDataClientLoadResponse playerDataClientLoadResponse = playerDataClientLoadResponseConsumer.get();
        System.out.println(packetId + " 으로 부터 요청이 들어옴" + playerDataClientLoadResponse.getPacketID() + " <<<< RESPONSE ID ");
        sendTargetChannel.writeAndFlush(playerDataClientLoadResponse);
    }
}
