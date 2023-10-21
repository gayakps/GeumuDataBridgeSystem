package gaya.pe.kr.network.packet.startDirection.server.request;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.AbstractMinecraftRequestPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataResponse;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 플레이어 데이터를 '서버로부터' 요청할 떄 사용함
 * @see gaya.pe.kr.network.packet.startDirection.client.response.PlayerDataResponse
 */

@Getter
public class PlayerDataRequest extends AbstractMinecraftRequestPacket<PlayerDataResponse> {


    @NotNull UUID playerUUID;
    @Nullable @Setter String playerName;

    public PlayerDataRequest(@NotNull UUID playerUUID) {
        super(PacketType.PLAYER_DATA_REQUEST);
        this.playerUUID = playerUUID;
    }

    @Override
    public void handleResponse(Supplier<PlayerDataResponse> supplier, Channel sendTargetChannel) throws RuntimeException {
        PlayerDataResponse playerDataResponse = supplier.get();
        System.out.println(getPacketID() + " (데이터 주세요)으로 부터 요청이 들어옴" + playerDataResponse.getPacketID() + " <<<< RESPONSE ID");
        sendTargetChannel.writeAndFlush(playerDataResponse);
    }
}
