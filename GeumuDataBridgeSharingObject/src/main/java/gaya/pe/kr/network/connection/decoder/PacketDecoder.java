package gaya.pe.kr.network.connection.decoder;

import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketStartDirection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {

    PacketStartDirection packetStartDirection;

    public PacketDecoder(PacketStartDirection packetStartDirection) {
        this.packetStartDirection = packetStartDirection;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

        // 최소 패킷 길이는 패킷 ID(1바이트) + 데이터 길이(4바이트)입니다.
        if (in.readableBytes() < 5) {
            return;
        }

        // 현재 readerIndex를 기억합니다.
        in.markReaderIndex();

        // 패킷 ID를 읽습니다.
        byte packetId = in.readByte();

        // 데이터 길이를 읽습니다.
        int dataLength = in.readInt();

        System.out.printf("PACKET ID : %,d DATA LENGTH : %,d\n", packetId ,dataLength);

        // 데이터가 모두 도착하지 않았으면 처리를 중지합니다.
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        // 패킷 데이터를 읽습니다.
        ByteBuf data = in.readBytes(dataLength);

        System.out.printf("%s -> [PACKET DECODER] Packet Id : %d Data Length : %d DATA SIZE : %d\n",packetStartDirection.name() , packetId, dataLength, data.readableBytes());

        // 패킷 객체를 생성합니다.
        AbstractMinecraftPacket packet = AbstractMinecraftPacket.fromData(packetId, data);

//        if (in.readerIndex() != in.writerIndex()) {
//            throw new DecoderException("Unused bytes exist in the end of packet: " + (in.writerIndex() - in.readerIndex()) + " bytes");
//        }

        // 리스트에 패킷을 추가합니다.
        out.add(packet);

    }

}

