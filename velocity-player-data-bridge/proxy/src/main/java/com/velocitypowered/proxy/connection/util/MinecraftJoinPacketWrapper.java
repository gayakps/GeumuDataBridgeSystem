package com.velocitypowered.proxy.connection.util;

import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.packet.JoinGame;

public class MinecraftJoinPacketWrapper {


    private final JoinGame joinGamePacket;
    private final MinecraftSessionHandler sessionHandler;

    public MinecraftJoinPacketWrapper(JoinGame joinGamePacket, MinecraftSessionHandler sessionHandler) {
        this.joinGamePacket = joinGamePacket;
        this.sessionHandler = sessionHandler;
    }

    public JoinGame getJoinGamePacket() {
        return joinGamePacket;
    }

    public MinecraftSessionHandler getSessionHandler() {
        return sessionHandler;
    }
}
