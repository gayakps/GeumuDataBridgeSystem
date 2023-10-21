package gaya.pe.kr.core.network.manager;

import gaya.pe.kr.core.history.db.DBConnection;
import gaya.pe.kr.core.network.handler.MinecraftServerPacketHandler;
import gaya.pe.kr.network.connection.initializer.MinecraftServerInitializer;
import gaya.pe.kr.network.packet.global.AbstractMinecraftPacket;
import gaya.pe.kr.network.packet.global.PacketStartDirection;
import gaya.pe.kr.thread.SchedulerUtil;
import gaya.pe.kr.util.ConfigurationManager;
import gaya.pe.kr.util.data.ConsumerTwoObject;
import gaya.pe.kr.util.data.WaitingTicket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class NetworkManager {

    private static class SingleTon {
        private static final NetworkManager NETWORK_MANAGER = new NetworkManager();
    }

    public static NetworkManager getInstance() {
        return SingleTon.NETWORK_MANAGER;
    }

    Channel channel;

    MinecraftServerPacketHandler minecraftServerPacketHandler;

    @Getter
    FileConfiguration configuration;

    public void init() {

        ConfigurationManager configurationManager = ConfigurationManager.getInstance();

        configuration = configurationManager.getConfiguration("option/config.yml", "option/config.yml");

        String ip = configuration.getString("ip", "127.0.0.1");
        int port = configuration.getInt("port", 6840);

        DBConnection.init(configuration);

        System.out.printf("IP : %s port : %d\n", ip, port);

        new Thread(() -> {
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                minecraftServerPacketHandler = new MinecraftServerPacketHandler();
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .handler(new MinecraftServerInitializer(PacketStartDirection.CLIENT, () -> minecraftServerPacketHandler));
                ChannelFuture future = bootstrap.connect(ip, port).sync();
                channel = future.channel();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                workerGroup.shutdownGracefully();
            }
        }).start();

    }

    public void sendPacket(AbstractMinecraftPacket minecraftPacket) {
        SchedulerUtil.runWaitTask( ()-> {
            ChannelFuture channelFuture = channel.writeAndFlush(minecraftPacket);
            try {
                Void result = channelFuture.get();
            } catch (InterruptedException | ExecutionException e ) {
                e.printStackTrace();
            }
        });
    }

    public <T> void sendPacket(AbstractMinecraftPacket minecraftPacket, Class<T> exceptResult) {
        SchedulerUtil.runWaitTask( ()-> {
            WaitingTicket<T> waitingTicket = new WaitingTicket<>(exceptResult);
            minecraftServerPacketHandler.addWaitingTicket(minecraftPacket, waitingTicket);

            ChannelFuture channelFuture = channel.writeAndFlush(minecraftPacket);
            try {
                Void result = channelFuture.get();
                CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(waitingTicket::getResult);
                completableFuture.get(5, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e ) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                System.out.printf("Ticket %s 에서 문제가 발생했습니다 \n", minecraftPacket.getType().name());
                throw new RuntimeException(e);
            }
        });
    }



    public <T> void sendPacketGetResult(Player player, ConsumerTwoObject<Player, T> playerConsumer, AbstractMinecraftPacket minecraftPacket, Class<T> exceptResult) {
        SchedulerUtil.runWaitTask( ()-> {
            WaitingTicket<T> waitingTicket = new WaitingTicket<>(exceptResult);
            minecraftServerPacketHandler.addWaitingTicket(minecraftPacket, waitingTicket);

            ChannelFuture channelFuture = channel.writeAndFlush(minecraftPacket);
            try {
                Void result = channelFuture.get();
                CompletableFuture<T> completableFuture = CompletableFuture.supplyAsync(waitingTicket::getResult);
                T t = completableFuture.get(5, TimeUnit.SECONDS);
                SchedulerUtil.runLaterTask( ()-> playerConsumer.accept(player,t),1);

            } catch (InterruptedException | ExecutionException e ) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                System.out.printf("Ticket %s 에서 문제가 발생했습니다 요청자 : %s \n", minecraftPacket.getType().name(), player.getName());
                throw new RuntimeException(e);
            }
        });
    }


}
