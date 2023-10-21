package gaya.pe.kr;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import gaya.pe.kr.util.PoketmonUtil;
import io.izzel.arclight.common.mod.server.api.DefaultArclightServer;
import com.pixelmonmod.pixelmon.client.storage.ClientStorageManager;
import io.izzel.arclight.api.Arclight;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraftforge.common.MinecraftForge;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;


public final class GayaSoftMain extends JavaPlugin implements Listener {

    public static Plugin plugin;
    public static final String version = "1.0.1";

    public static HashMap<UUID, ServerPlayerEntity> serverPlayerEntityHashMap = new HashMap<>();


    @Override
    public void onEnable() {
        plugin = this;

        DefaultArclightServer defaultArclightServer = new DefaultArclightServer();

        System.out.println(Arclight.getVersion().getName() + " --------------------- [VERSION !!!!!!!]");

        System.out.println("--------- System Starting - GAYA SOFT ---------");

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {}

    public static byte[] getPlayerPoketmonNBT(UUID playerUUID) {
        PCStorage pcStorage = StorageProxy.getPCForPlayer(playerUUID);

        CompoundNBT compoundNBT = new CompoundNBT();
        CompoundNBT result = pcStorage.writeToNBT(compoundNBT);

        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(io);
            CompressedStreamTools.write(result, dataOutputStream);
            byte[] resultByte = io.toByteArray();
            return resultByte;
        } catch ( Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadPlayerPoketmonNBT(UUID playerUUID, byte[] bytes) {

        System.out.printf("%s Load Request %d\n", playerUUID, bytes.length);
        PCStorage pcStorage = ClientStorageManager.pcs.get(playerUUID);

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            DataInputStream dataInputStream = new DataInputStream(in);
            CompoundNBT result = CompressedStreamTools.read(dataInputStream);
            PCStorage resultPCStorage = pcStorage.readFromNBT(result);
            ClientStorageManager.pcs.put(playerUUID, resultPCStorage);

        } catch ( Exception e) {
            e.printStackTrace();
        }


    }




}
