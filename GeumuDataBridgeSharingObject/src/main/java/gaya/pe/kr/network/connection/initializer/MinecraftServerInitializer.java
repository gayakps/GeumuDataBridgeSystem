package gaya.pe.kr.network.connection.initializer;

import gaya.pe.kr.network.connection.decoder.MinecraftLengthFieldBasedFrameDecoder;
import gaya.pe.kr.network.connection.decoder.PacketDecoder;
import gaya.pe.kr.network.connection.encoder.PacketEncoder;
import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketStartDirection;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;

import java.util.function.Supplier;

public class MinecraftServerInitializer extends ChannelInitializer<SocketChannel> {

    PacketStartDirection packetStartDirection;
    Supplier<SimpleChannelInboundHandler<AbstractMinecraftPacket>> handlerSupplier;

    public MinecraftServerInitializer(PacketStartDirection packetStartDirection, Supplier<SimpleChannelInboundHandler<AbstractMinecraftPacket>> handlerSupplier) {
        this.packetStartDirection = packetStartDirection;
        this.handlerSupplier = handlerSupplier;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        // 패킷 디코더
        pipeline.addLast(new PacketEncoder(packetStartDirection));
        pipeline.addLast(new LengthFieldPrepender(4));
        pipeline.addLast(new PacketDecoder(packetStartDirection));
//        pipeline.addLast(new MinecraftLengthFieldBasedFrameDecoder(packetStartDirection));
        pipeline.addLast(handlerSupplier.get());
    }
}

