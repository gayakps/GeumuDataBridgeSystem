package gaya.pe.kr.network.packet.startDirection.client.response;


import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.AbstractResponsePacket;
import gaya.pe.kr.network.packet.global.PacketType;
import gaya.pe.kr.network.packet.startDirection.server.request.PlayerDataRequest;
import gaya.pe.kr.network.packet.type.ResponseType;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import lombok.Getter;

/**
 * 플레이어 데이터 응답 ( 버킷 쪽에서 전달 해줌 )
 * @see PlayerDataRequest 의 답변
 */

@Getter
public class PlayerDataResponse extends AbstractResponsePacket<PlayerPersistentData> {

    public PlayerDataResponse(long requestPacketId, ResponseType responseType, PlayerPersistentData playerPersistentData) {
        super(PacketType.PLAYER_DATA_RESPONSE, requestPacketId, responseType, playerPersistentData);
    }

}
