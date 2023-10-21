package gaya.pe.kr.network.packet.startDirection.client.request.player;

import gaya.pe.kr.network.packet.global.AbstractMinecraftRequestPacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.server.response.TargetHistoryResponse;
import io.netty.channel.Channel;
import lombok.Getter;

import java.util.function.Supplier;

/**
 *
 */
@Getter
public class TargetHistoryRequest extends AbstractMinecraftRequestPacket<TargetHistoryResponse> {

    int historyNumber;

    public TargetHistoryRequest(int historyNumber) {
        super(PacketType.TARGET_HISTORY_REQUEST);
        this.historyNumber = historyNumber;
    }

    @Override
    public void handleResponse(Supplier<TargetHistoryResponse> supplier, Channel sendTargetChannel) throws RuntimeException {
        sendTargetChannel.writeAndFlush(supplier.get());
    }
}
