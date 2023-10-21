package gaya.pe.kr.network.packet.startDirection.server.response;

import gaya.pe.kr.network.packet.global.AbstractResponsePacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.type.ResponseType;
import gaya.pe.kr.util.data.player.PlayerHistory;
import lombok.Getter;

/**
 * TargetPlayer History Response
 * @see gaya.pe.kr.network.packet.startDirection.client.request.player.TargetPlayerHistoryRequest
 *
 */

@Getter
public class TargetHistoryResponse extends AbstractResponsePacket<PlayerHistory[]> {

    int expireDay;

    public TargetHistoryResponse(long requestPacketId, ResponseType responseType, String reason, PlayerHistory[] responseObject, int expireDay) {
        super(PacketType.TARGET_HISTORY_RESPONSE, requestPacketId, responseType, reason, responseObject);
        this.expireDay = expireDay;
    }

    public TargetHistoryResponse(long requestPacketId, ResponseType responseType, PlayerHistory[] responseObject, int expireDay) {
        super(PacketType.TARGET_HISTORY_RESPONSE, requestPacketId, responseType, responseObject);
        this.expireDay = expireDay;
    }
}
