package gaya.pe.kr.network.packet.global;

import gaya.pe.kr.network.packet.type.ResponseType;
import lombok.Getter;


@Getter
public abstract class AbstractResponsePacket<T> extends AbstractMinecraftPacket {

    ResponseType responseType;
    String reason = "";

    long requestPacketId;
    T responseObject;

    public AbstractResponsePacket(PacketType type, long requestPacketId, ResponseType responseType, String reason, T responseObject) {
        super(type);
        this.requestPacketId = requestPacketId;
        this.responseType = responseType;
        this.reason = reason;
        this.responseObject = responseObject;
    }

    public AbstractResponsePacket(PacketType type, long requestPacketId, ResponseType responseType, T responseObject) {
        super(type);
        this.requestPacketId = requestPacketId;
        this.responseType = responseType;
        this.responseObject = responseObject;
    }
}
