package com.velocitypowered.proxy.gaya.network;

import com.velocitypowered.proxy.VelocityServer;
import gaya.pe.kr.network.connection.initializer.MinecraftServerInitializer;
import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketStartDirection;
import gaya.pe.kr.network.packet.startDirection.client.send.MinecraftBukkitChannelHandlerAddRequest;
import gaya.pe.kr.util.data.WaitingTicket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GayaSoftNetworkManager {

    private static class SingleTon {
        private static final GayaSoftNetworkManager GAYA_SOFT_NETWORK_MANAGER = new GayaSoftNetworkManager();
    }

    public static GayaSoftNetworkManager getInstance() {
        return SingleTon.GAYA_SOFT_NETWORK_MANAGER;
    }

    private final HashMap<String, Channel> minecraftServerInfoAsChannel = new HashMap<>(); // 127.0.0.1:3 서버의 Bach

    HashMap<Long, WaitingTicket<?>> packetWaitingResponseAsObjectHashMap = new HashMap<>();
    public Channel getGayaSoftChannel(String ip, int port) {
        return minecraftServerInfoAsChannel.get(String.format("%s:%d", ip, port));
    }


    public Channel getGayaSoftChannel(String address) {

        Channel channel = minecraftServerInfoAsChannel.get(address.replace("/", "").trim());

        System.out.printf("Request : %s Result : %s\n", address, channel != null ? channel : "NONE");

        return channel;
    }


    public void addGayaSoftChannel(MinecraftBukkitChannelHandlerAddRequest minecraftBukkitChannelHandlerAddRequest, Channel channel) {
        String address = String.format("%s:%d", minecraftBukkitChannelHandlerAddRequest.getIp(), minecraftBukkitChannelHandlerAddRequest.getPort());
        minecraftServerInfoAsChannel.put(address, channel);
        System.out.printf("[%s] - [%s] Minecraft Gaya Soft Channel Handler Adding\n", address, channel.toString());

        minecraftServerInfoAsChannel.forEach((s, channel1) -> {
            System.out.printf("[NOW] %s\n", s);
        });

    }

    public void init(VelocityServer velocityServer) {

        new Thread(() -> {
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {

                ServerBootstrap bootstrap = new ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new MinecraftServerInitializer(PacketStartDirection.SERVER, ()-> new GayaSoftBackendHandler(velocityServer)))
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true);
                ChannelFuture future = bootstrap.bind(6840).sync();

                System.out.println("[GAYASOFT] Geumu System Port " + 6840);

                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
                bossGroup.shutdownGracefully();
            }
        }).start();
    }


    public <T> T sendPacket(AbstractMinecraftPacket minecraftPacket, Channel channel, Class<T> exceptResult) throws Exception {
        WaitingTicket<T> waitingTicket = new WaitingTicket<>(exceptResult);
        addWaitingTicket(minecraftPacket, waitingTicket);

        ChannelFuture channelFuture = channel.writeAndFlush(minecraftPacket);

        Void result = channelFuture.get();
        CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(waitingTicket::getResult);

        return completableFuture.get(3, TimeUnit.SECONDS);

    }

    @SuppressWarnings("unchecked")
    public <T> WaitingTicket<T> getWaitingTicket(long requestPacketId) {

        WaitingTicket<T> result = (WaitingTicket<T>) packetWaitingResponseAsObjectHashMap.get(requestPacketId);
        for (Long aLong : packetWaitingResponseAsObjectHashMap.keySet()) {
            System.out.printf("NOW WAITING TICKET : %d\n", aLong);
        }

        if ( result != null ) {
            packetWaitingResponseAsObjectHashMap.remove(requestPacketId);
        }

        return result;
    }

    public void removeWaitingTicket(long requestPacketId) {
        packetWaitingResponseAsObjectHashMap.remove(requestPacketId);
    }

    public void addWaitingTicket(AbstractMinecraftPacket abstractMinecraftPacket, WaitingTicket<?> waitingTicket ) {
        System.out.printf("ADD WAITING TICKET(%s) %d\n", abstractMinecraftPacket.getType().name(), abstractMinecraftPacket.getPacketID());
        packetWaitingResponseAsObjectHashMap.put(abstractMinecraftPacket.getPacketID(), waitingTicket);
    }

    private <T> void handleWaitingTicket(long requestPacketId, T tObject) throws RuntimeException {
        WaitingTicket<T> waitingTicket = getWaitingTicket(requestPacketId);
        waitingTicket.setResult(tObject);
        removeWaitingTicket(requestPacketId);
    }

    public boolean isWaitingTicket(long requestPacketId) {
        return packetWaitingResponseAsObjectHashMap.containsKey(requestPacketId);
    }

    public HashMap<String, Channel> getMinecraftServerInfoAsChannel() {
        return minecraftServerInfoAsChannel;
    }
}
