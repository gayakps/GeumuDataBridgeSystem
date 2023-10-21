package com.velocitypowered.proxy.connection.player;

import com.velocitypowered.proxy.Velocity;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.backend.BackendTransitionSessionHandler;
import com.velocitypowered.proxy.connection.util.MinecraftJoinPacketWrapper;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.JoinGame;
import com.velocitypowered.proxy.scheduler.VelocityScheduler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.concurrent.CompletableFuture;

public class MinecraftJoinPacketHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof MinecraftJoinPacketWrapper) {
            // JoinGame 패킷 처리 로직

            MinecraftJoinPacketWrapper minecraftJoinPacketWrapper = (MinecraftJoinPacketWrapper) msg;

            try {

                JoinGame joinGame = minecraftJoinPacketWrapper.getJoinGamePacket();
                MinecraftSessionHandler minecraftSessionHandler = minecraftJoinPacketWrapper.getSessionHandler();
                    try {

                        if ( minecraftSessionHandler instanceof BackendTransitionSessionHandler ) {

//                            System.out.printf("[%s] MinecraftJoinPacketWrapper 수신 완 Wait 10s\n", Thread.currentThread().getName());

                            BackendTransitionSessionHandler backendTransitionSessionHandler = (BackendTransitionSessionHandler) minecraftSessionHandler;
//                            System.out.printf("[%s] MinecraftJoinPacketWrapper 처리 시작 NIO thread\n", Thread.currentThread().getName());
                            backendTransitionSessionHandler.handle(joinGame);

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            } catch ( Exception e) {
                e.printStackTrace();
            }

        } else {
            // JoinGame 패킷이 아닐 경우 다음 핸들러로 전달
            ctx.fireChannelRead(msg);
        }
    }
}
