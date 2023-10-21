package gaya.pe.kr.network.packet.startDirection.client.request.player;

import gaya.pe.kr.network.packet.global.AbstractMinecraftRequestPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.server.response.TargetHistoryResponse;
import io.netty.channel.Channel;
import lombok.Getter;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 특정 플레이어의 History 를 가져옴
 */

@Getter
public class TargetPlayerHistoryRequest extends AbstractMinecraftRequestPacket<TargetHistoryResponse> {
    UUID targetPlayerUUID;
    public TargetPlayerHistoryRequest(UUID targetPlayerUUID) {
        super(PacketType.TARGET_PLAYER_HISTORY_REQUEST);
        this.targetPlayerUUID = targetPlayerUUID;
    }

    @Override
    public void handleResponse(Supplier<TargetHistoryResponse> supplier, Channel sendTargetChannel) throws RuntimeException {
        sendTargetChannel.writeAndFlush(supplier.get());
    }
}
