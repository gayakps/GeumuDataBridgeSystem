package gaya.pe.kr.network.connection.decoder;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketStartDirection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class MinecraftLengthFieldBasedFrameDecoder extends io.netty.handler.codec.LengthFieldBasedFrameDecoder {

    PacketStartDirection packetStartDirection;


    public MinecraftLengthFieldBasedFrameDecoder(PacketStartDirection packetStartDirection) {
        super(1048576, 0, 2, 0, 2); // 최대 1MB, 길이 필드는 2바이트
        this.packetStartDirection = packetStartDirection;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);

        if (frame == null) {
//            System.out.printf("%s -> [MinecraftLengthFieldBasedFrameDecoder] FRAME IS NULL\n", packetStartDirection.name());
            return null;
        }

        try {
            byte packetId = frame.readByte();
            int packetSize = frame.readableBytes();

//            System.out.printf("%s -> [MinecraftLengthFieldBasedFrameDecoder] Packet Id : %d Data Size : %d\n", packetStartDirection.name(), packetId, packetSize);
            return AbstractMinecraftPacket.fromData(packetId, frame);

        } finally {
            frame.release();
        }
    }

}
