/*
 * Copyright (C) 2019-2023 Velocity Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.velocitypowered.proxy.connection.backend;

import static com.velocitypowered.proxy.connection.backend.BackendConnectionPhases.IN_TRANSITION;
import static com.velocitypowered.proxy.connection.forge.legacy.LegacyForgeHandshakeBackendPhase.HELLO;

import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.ConnectionTypes;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.connection.player.TestObj;
import com.velocitypowered.proxy.connection.util.ConnectionMessages;
import com.velocitypowered.proxy.connection.util.ConnectionRequestResults;
import com.velocitypowered.proxy.connection.util.ConnectionRequestResults.Impl;
import com.velocitypowered.proxy.gaya.databse.DBConnection;
import com.velocitypowered.proxy.gaya.network.GayaSoftNetworkManager;
import com.velocitypowered.proxy.gaya.player.PlayerHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.Disconnect;
import com.velocitypowered.proxy.protocol.packet.JoinGame;
import com.velocitypowered.proxy.protocol.packet.KeepAlive;
import com.velocitypowered.proxy.protocol.packet.PluginMessage;
import com.velocitypowered.proxy.protocol.util.PluginMessageUtil;
import java.io.IOException;

import gaya.pe.kr.network.packet.startDirection.server.request.PlayerDataClientLoadRequest;
import gaya.pe.kr.network.packet.startDirection.server.request.PlayerDataRequest;
import gaya.pe.kr.util.converter.ObjectConverter;
import gaya.pe.kr.util.data.player.GameMode;
import gaya.pe.kr.util.data.player.PlayerPersistentData;
import io.netty.channel.Channel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A special session handler that catches "last minute" disconnects.
 */
public class BackendTransitionSessionHandler implements MinecraftSessionHandler {

  private static final Logger logger = LogManager.getLogger(BackendTransitionSessionHandler.class);

  private final VelocityServer server;
  private final VelocityServerConnection serverConn;
  private final CompletableFuture<Impl> resultFuture;
  private final BungeeCordMessageResponder bungeecordMessageResponder;

  /**
   * Creates the new transition handler.
   *
   * @param server       the Velocity server instance
   * @param serverConn   the server connection
   * @param resultFuture the result future
   */
  BackendTransitionSessionHandler(VelocityServer server,
      VelocityServerConnection serverConn,
      CompletableFuture<Impl> resultFuture) {
    this.server = server;
    this.serverConn = serverConn;
    this.resultFuture = resultFuture;
    this.bungeecordMessageResponder = new BungeeCordMessageResponder(server,
        serverConn.getPlayer());
  }

  @Override
  public boolean beforeHandle() {
    if (!serverConn.isActive()) {
      // Obsolete connection
      serverConn.disconnect();
      return true;
    }
    return false;
  }

  @Override
  public boolean handle(KeepAlive packet) {
    serverConn.ensureConnected().write(packet);
    return true;
  }

  @Override
  public boolean handle(JoinGame packet) {

      logger.info("----- Start to JoinGame Handling : " + Thread.currentThread().getName() + " ----- ");
      GayaSoftNetworkManager gayaSoftNetworkManager = GayaSoftNetworkManager.getInstance();

      try {

          MinecraftConnection smc = serverConn.ensureConnected();
          RegisteredServer previousServer = serverConn.getPreviousServer().orElse(null);

          // This block will be executed in a separate thread
          VelocityServerConnection existingConnection = serverConn.getPlayer().getConnectedServer();
          final ConnectedPlayer player = serverConn.getPlayer();
//          System.out.printf("MinecraftConnection Address : %s\n", smc.getChannel().localAddress());

          PlayerPersistentData playerPersistentData = null;

          String afterServerAddress = smc.getRemoteAddress().toString();

          if (existingConnection != null) {

              //TODO 이전서버로 부터 데이터를 받아와야함

              InetSocketAddress inetAddress = previousServer.getServerInfo().getAddress();

              String beforeServerAddress = (inetAddress.getAddress().getHostAddress()+":"+inetAddress.getPort());

              if (PlayerHandler.playerPersistentDataHashMap.containsKey(player.getUniqueId()) ) {
                  PlayerHandler.playerPersistentDataHashMap.get(player.getUniqueId());
                  System.out.println("Source for Player Handler Remove Data");
              } else {
                  Channel beforeServerChannel = gayaSoftNetworkManager.getGayaSoftChannel(beforeServerAddress);
                  try {
                      playerPersistentData = gayaSoftNetworkManager.sendPacket(new PlayerDataRequest(player.getUniqueId()), beforeServerChannel, PlayerPersistentData.class);
                      System.out.println("Direct Player persistent Data from before server : " + playerPersistentData.getPlayerUUID());
                  } catch ( Exception e) {
                      player.disconnect(Component.text(beforeServerAddress + " Fail to request Data"));
                  }

              }

              // Shut down the existing server connection.
              player.setConnectedServer(null);
              existingConnection.disconnect();

              // Send keep alive to try to avoid timeouts
              player.sendKeepAlive();

              // Reset Tablist header and footer to prevent desync
              player.clearHeaderAndFooter();

              logger.info(String.format("Server Switching ( %s -> %s )",beforeServerAddress ,afterServerAddress));

          } else {

              if (PlayerHandler.playerPersistentDataHashMap.containsKey(player.getUniqueId()) ) {
                  playerPersistentData = PlayerHandler.playerPersistentDataHashMap.get(player.getUniqueId());
                  PlayerHandler.playerPersistentDataHashMap.remove(player.getUniqueId());
                  System.out.println("[신규접속] PlayerHandler 데이터 존재");
              } else {

                  PlayerPersistentData.PlayerPersistentDataBuilder playerPersistentDataBuilder = new PlayerPersistentData.PlayerPersistentDataBuilder(player.getUniqueId());

                  try (Connection connection = DBConnection.getConnection() ) {

                      String sql = "select * from player_data where player_uuid = ?";

                      PreparedStatement preparedStatement = connection.prepareStatement(sql);

                      preparedStatement.setString(1, player.getUniqueId().toString());

                      ResultSet resultSet = preparedStatement.executeQuery();

                      if ( resultSet.next() ) {

                          String nbtDataStr = resultSet.getString("nbt_data");
                          String gameModeStr = resultSet.getString("game_mode");
                          String effectsStr = resultSet.getString("effects");
                          byte[] poketmonData = resultSet.getBytes("poketmon_data");
                          byte[] pcData = resultSet.getBytes("pc_data");


                          playerPersistentDataBuilder
                                  .setPlayerData(ObjectConverter.getStringAsByte(nbtDataStr))
                                  .setPlayerGameMode(GameMode.valueOf(gameModeStr))
                                  .setEffects(ObjectConverter.getStringAsByte(effectsStr))
                                  .setPoketmonData(poketmonData)
                                  .setPcData(pcData);

                          playerPersistentData = playerPersistentDataBuilder.build();

                          System.out.printf("%s 데이터 추출 PC ( %s ) / POKETMON ( %s )\n",player.getUsername() , pcData != null, poketmonData != null);

                      } else {
                          playerPersistentDataBuilder.setPlayerGameMode(GameMode.SURVIVAL);
                          playerPersistentData = playerPersistentDataBuilder.build();
                          System.out.println("[DB / Handler] 데이터 X 신규 제작");
                      }

                  } catch (Exception e) {
                      e.printStackTrace();
                      player.disconnect(Component.text("DB로 부터 로딩 실패"));
                  }


              }

          }


          smc.eventLoop().execute(() -> {
//              System.out.printf("[%s] 에서 smc.setAutoReading False 를 함\n", Thread.currentThread().getName());
              smc.setAutoReading(false);
          });
          // The goods are in hand! We got JoinGame. Let's transition completely to the new state.

          PlayerPersistentData finalPlayerPersistentData = playerPersistentData;
          server.getEventManager()
                  .fire(new ServerConnectedEvent(player, serverConn.getServer(), previousServer))

                  .thenRunAsync(() -> {

                      try {

                          String serverName = serverConn.getServer().getServerInfo().getName();

                          // Make sure we can still transition (player might have disconnected here).
                          if (!serverConn.isActive()) {
                              // Connection is obsolete.
                              serverConn.disconnect();
                              logger.info("Server Conn exist Disconnect");
                              return;
                          }

                          // Change the client to use the ClientPlaySessionHandler if required.
//                          logger.info("ClientPlaySessionHandler 생성 5초전");
//                          player.sendKeepAlive(); // keep alive
//                          Thread.sleep(5000);
                          ClientPlaySessionHandler playHandler;

                          if (player.getConnection().getSessionHandler() instanceof ClientPlaySessionHandler) {
                              playHandler = (ClientPlaySessionHandler) player.getConnection().getSessionHandler();
                          } else {
                              playHandler = new ClientPlaySessionHandler(server, player);
                              player.getConnection().setSessionHandler(playHandler);
                          }

                          playHandler.handleBackendJoinGame(packet, serverConn);
                          // Set the new play session handler for the server. We will have nothing more to do
                          // with this connection once this task finishes up.
                          smc.setSessionHandler(new BackendPlaySessionHandler(server, serverConn));

                          // Clean up disabling auto-read while the connected event was being processed.
                          smc.setAutoReading(true);

                          // Now set the connected server.
                          serverConn.getPlayer().setConnectedServer(serverConn);

                          Channel afterChannel = gayaSoftNetworkManager.getGayaSoftChannel(afterServerAddress);
                          try {
                              boolean result = gayaSoftNetworkManager.sendPacket(new PlayerDataClientLoadRequest(finalPlayerPersistentData), afterChannel, Boolean.class);

                              if (!result) {
                                  player.disconnect(Component.text("Fail to Data Loading ( Switching Fail )"));
                              } else {
                                  System.out.println("After Server success send data");
                              }

                          } catch ( Exception e) {
                              player.disconnect(Component.text("데이터 로딩에 실패했습니다 ( 스위칭 실패 ) -" + e.getMessage() ));
                          }

                          // We're done! :)
                          server.getEventManager().fireAndForget(new ServerPostConnectEvent(player,
                                  previousServer));
                          resultFuture.complete(ConnectionRequestResults.successful(serverConn.getServer()));

                          PlayerHandler.playerPersistentDataHashMap.remove(player.getUniqueId()); // 완전히 끝나면 종료

                          System.out.println("--------------------------------------------------------- [ Success " + player.getUsername() + " ]");

                      } catch (Exception e) {
                          e.printStackTrace();
                      }

                  }, smc.eventLoop())
                  .exceptionally(exc -> {
                      logger.error("Unable to switch to new server {} for {}",
                              serverConn.getServerInfo().getName(),
                              player.getUsername(), exc);
                      player.disconnect(ConnectionMessages.INTERNAL_SERVER_CONNECTION_ERROR);
                      resultFuture.completeExceptionally(exc);
                      return null;
                  });


      } catch (Exception e) {
          e.printStackTrace();
      }


      return true;

  }


  @Override
  public boolean handle(Disconnect packet) {
    final MinecraftConnection connection = serverConn.ensureConnected();
    serverConn.disconnect();

    // If we were in the middle of the Forge handshake, it is not safe to proceed. We must kick
    // the client.
    if (connection.getType() == ConnectionTypes.LEGACY_FORGE
        && !serverConn.getPhase().consideredComplete()) {
      resultFuture.complete(ConnectionRequestResults.forUnsafeDisconnect(packet,
          serverConn.getServer()));
    } else {
      resultFuture.complete(ConnectionRequestResults.forDisconnect(packet, serverConn.getServer()));
    }

    return true;
  }

  @Override
  public boolean handle(PluginMessage packet) {
    if (bungeecordMessageResponder.process(packet)) {
      return true;
    }

    if (PluginMessageUtil.isRegister(packet)) {
      serverConn.getPlayer().getKnownChannels().addAll(PluginMessageUtil.getChannels(packet));
    } else if (PluginMessageUtil.isUnregister(packet)) {
      serverConn.getPlayer().getKnownChannels().removeAll(PluginMessageUtil.getChannels(packet));
    }

    // We always need to handle plugin messages, for Forge compatibility.
    if (serverConn.getPhase().handle(serverConn, serverConn.getPlayer(), packet)) {
      // Handled, but check the server connection phase.
      if (serverConn.getPhase() == HELLO) {
        VelocityServerConnection existingConnection = serverConn.getPlayer().getConnectedServer();
        if (existingConnection != null && existingConnection.getPhase() != IN_TRANSITION) {
          // Indicate that this connection is "in transition"
          existingConnection.setConnectionPhase(IN_TRANSITION);

          // Tell the player that we're leaving and we just aren't coming back.
          existingConnection.getPhase().onDepartForNewServer(existingConnection,
              serverConn.getPlayer());
        }
      }
      return true;
    }

    serverConn.getPlayer().getConnection().write(packet.retain());
    return true;
  }

  @Override
  public void disconnected() {
      logger.info("[BackendTransitionSessionHandler] Disconnected");
    resultFuture
        .completeExceptionally(new IOException("Unexpectedly disconnected from remote server"));
  }
}
