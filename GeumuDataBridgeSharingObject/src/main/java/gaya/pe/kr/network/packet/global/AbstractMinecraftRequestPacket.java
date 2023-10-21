package gaya.pe.kr.network.packet.global;

import io.netty.channel.Channel;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractMinecraftRequestPacket<RESPONSE_PACKET> extends AbstractMinecraftPacket {

    protected AbstractMinecraftRequestPacket(PacketType type) {
        super(type);
    }

    public abstract void handleResponse(Supplier<RESPONSE_PACKET> supplier, Channel sendTargetChannel) throws RuntimeException;



}
