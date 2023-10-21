package gaya.pe.kr.network.connection.encoder;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketStartDirection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<AbstractMinecraftPacket> {

    PacketStartDirection packetStartDirection;

    public PacketEncoder(PacketStartDirection packetStartDirection) {
        this.packetStartDirection = packetStartDirection;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractMinecraftPacket packet, ByteBuf out) throws Exception {
        // 패킷 ID를 쓰고
        ByteBuf data = packet.getData();
        int packetLength = data.readableBytes();

        out.writeByte(packet.getType().getId()); // 패킷 타입 쓰기
        out.writeInt(packetLength); // 패킷 길이 쓰기
        out.writeBytes(data, 0, packetLength); // 패킷 데이터 쓰기

        System.out.printf("%s -> [ENCODING] PACKET CREATED TYPE : [%s] LENGTH : %d TOTAL : %d\n",packetStartDirection.name(), packet.getType().name(), packetLength, out.readableBytes());

    }

}
