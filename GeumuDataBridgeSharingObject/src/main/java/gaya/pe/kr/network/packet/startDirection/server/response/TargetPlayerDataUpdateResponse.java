package gaya.pe.kr.network.packet.startDirection.server.response;

import gaya.pe.kr.network.packet.global.AbstractResponsePacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.type.ResponseType;

public class TargetPlayerDataUpdateResponse extends AbstractResponsePacket<Boolean> {
    public TargetPlayerDataUpdateResponse(long requestPacketId, ResponseType responseType, String reason, Boolean responseObject) {
        super(PacketType.TARGET_PLAYER_DATA_UPDATE_RESPONSE, requestPacketId, responseType, reason, responseObject);
    }
}
