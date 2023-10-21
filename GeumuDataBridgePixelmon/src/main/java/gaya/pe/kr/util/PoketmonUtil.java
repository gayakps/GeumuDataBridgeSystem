package gaya.pe.kr.util;

import com.pixelmonmod.pixelmon.api.events.battles.CatchComboEvent;
import com.pixelmonmod.pixelmon.api.storage.*;
import com.pixelmonmod.pixelmon.api.storage.breeding.PlayerDayCare;
import com.pixelmonmod.pixelmon.api.util.NBTTools;
import com.pixelmonmod.pixelmon.api.util.PixelmonPlayerUtils;
import com.pixelmonmod.pixelmon.api.util.helpers.NetworkHelper;
import com.pixelmonmod.pixelmon.comm.EnumUpdateType;
import com.pixelmonmod.pixelmon.comm.data.PixelmonPacket;
import com.pixelmonmod.pixelmon.comm.packetHandlers.daycare.SendEntireDayCarePacket;
import com.pixelmonmod.pixelmon.storage.playerData.CaptureCombo;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.*;
import gaya.pe.kr.nbt.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.Base64;
import java.util.UUID;

public class PoketmonUtil {

    public static String serializeParty(UUID uuid) {
        PlayerPartyStorage party = StorageProxy.getParty(uuid);
        CompoundNBT tag = new CompoundNBT();
        party.writeToNBT(tag);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            NBTTools.saveNBT(tag, out, false);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static byte[] serializePartyToByte(UUID uuid) {
        CompoundNBT tag = new CompoundNBT();
        PlayerPartyStorage party = StorageProxy.getParty(uuid);
        party.writeToNBT(tag);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            NBTTools.saveNBT(tag, out, false);
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadParty(UUID uuid, byte[] bytes) {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        DataInputStream dataInputStream = new DataInputStream(input);
        try {
            CompoundNBT tag = CompressedStreamTools.read(dataInputStream);
            PlayerPartyStorage party = StorageProxy.getParty(uuid);

            party.readFromNBT(tag);
            party.setHasChanged(true);

            PlayerDayCare playerDayCare = party.getDayCare();

            if ( playerDayCare.getAllowedBoxes() != 7) playerDayCare.setAllowedBoxes(7);

            for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                if ( player.getUniqueID().equals(uuid) ) {
                    NetworkHelper.sendPacket(new SendEntireDayCarePacket(playerDayCare), player);

                    CaptureCombo captureCombo = party.transientData.captureCombo;
                    captureCombo.getCurrentCombo();
                    captureCombo.onCapture(player, captureCombo.getCurrentSpecies());

                }
            }

            for (int i = 0; i < 6; i++) {
                party.notifyListeners(new StoragePosition(-1, i), party.get(i), EnumUpdateType.CLIENT);
            }

            party.starterPicked = true;

            party.playerPokedex.update();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String serializePC(UUID uuid) {
        CompoundNBT tag = new CompoundNBT();
        PCStorage pc = StorageProxy.getPCForPlayer(uuid);
        pc.writeToNBT(tag);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            NBTTools.saveNBT(tag, out, false);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static byte[] serializePCToByte(UUID uuid) {
        CompoundNBT tag = new CompoundNBT();
        PCStorage pc = StorageProxy.getPCForPlayer(uuid);
        pc.writeToNBT(tag);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            NBTTools.saveNBT(tag, out, false);
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadPC(UUID uuid, byte[] bytes) {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        DataInputStream dataInputStream = new DataInputStream(input);
        try {
            CompoundNBT tag = CompressedStreamTools.read(dataInputStream);
            PCStorage pcStorage = StorageProxy.getPCForPlayer(uuid);
            pcStorage.readFromNBT(tag);
            pcStorage.setHasChanged(true);

            for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                if ( player.getUniqueID().equals(uuid) ) {
                    StorageProxy.initializePCForPlayer(player, pcStorage);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
