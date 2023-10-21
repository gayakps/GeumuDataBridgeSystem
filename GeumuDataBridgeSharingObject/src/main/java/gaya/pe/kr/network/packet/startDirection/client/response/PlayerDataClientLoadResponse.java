package gaya.pe.kr.network.packet.startDirection.client.response;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.AbstractResponsePacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.server.request.PlayerDataClientLoadRequest;
import gaya.pe.kr.network.packet.type.ResponseType;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import lombok.Getter;
import lombok.Setter;

/**
 * 플레이어 데이터 잘 받았습니다 로딩을 대기합니다 할 때
 * @see PlayerDataClientLoadRequest 의 답변임
 */

@Getter
public class PlayerDataClientLoadResponse extends AbstractResponsePacket<Boolean> {

    public PlayerDataClientLoadResponse(long requestPacketId, ResponseType responseType) {
        super(PacketType.PLAYER_DATA_CLIENT_LOAD_RESPONSE, requestPacketId, responseType, responseType.equals(ResponseType.SUCCESS));
    }


}
