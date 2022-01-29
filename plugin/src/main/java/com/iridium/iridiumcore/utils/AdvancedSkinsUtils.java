package com.iridium.iridiumcore.utils;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.soknight.advancedskins.api.AdvancedSkinsApi;
import me.soknight.advancedskins.api.exception.FeatureUnavailableException;
import me.soknight.advancedskins.api.profile.PlayerProfile;
import me.soknight.advancedskins.api.profile.ProfileCache;

import java.util.Collection;
import java.util.UUID;

public final class AdvancedSkinsUtils {

    private static final boolean SUPPORTS = XMaterial.supports(14);

    public static ItemStack setHeadData(ItemStack itemStack, String playerName) {
        if (playerName == null)
            return itemStack;

        Player onlinePlayer = Bukkit.getPlayer(playerName);
        if (onlinePlayer != null && !onlinePlayer.isOnline())
            return setOnlinePlayerData(itemStack, onlinePlayer);
        else
            return setOfflinePlayerData(itemStack, playerName);
    }

    private static ItemStack setOnlinePlayerData(ItemStack itemStack, Player onlinePlayer) {
        WrappedGameProfile gameProfile = WrappedGameProfile.fromPlayer(onlinePlayer);

        Collection<WrappedSignedProperty> texturesProperties = gameProfile.getProperties().get("textures");
        if (texturesProperties == null || texturesProperties.isEmpty())
            return itemStack;

        WrappedSignedProperty texturesProperty = texturesProperties.iterator().next();
        String signature = texturesProperty.getSignature();
        String value = texturesProperty.getValue();

        return setHeadData(itemStack, onlinePlayer.getUniqueId(), signature, value);
    }

    private static ItemStack setOfflinePlayerData(ItemStack itemStack, String playerName) {
        try {
            AdvancedSkinsApi api = AdvancedSkinsApi.get();
            PlayerProfile profile = api.getProfile(playerName).orElse(null);
            if (profile == null)
                return itemStack;

            ProfileCache profileCache = profile.getProfileCache();
            if (profileCache == null || !profileCache.isCached())
                return itemStack;

            UUID uuid = profile.getPlayerUUID().orElseGet(UUID::randomUUID);
            String value = profileCache.getBase64Value();
            String signature = profileCache.getSignature();

            return setHeadData(itemStack, uuid, value, signature);
        } catch (FeatureUnavailableException ignored) {
            return itemStack;
        }
    }

    private static ItemStack setHeadData(ItemStack itemStack, UUID uuid, String value, String signature) {
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound skull = nbtItem.addCompound("SkullOwner");
        if (SUPPORTS) {
            skull.setUUID("Id", uuid);
        } else {
            skull.setString("Id", uuid.toString());
        }

        NBTListCompound texture = skull.addCompound("Properties").getCompoundList("textures").addCompound();
        if(signature != null)
            texture.setString("Signature", signature);
        if(value != null)
            texture.setString("Value", value);
        return nbtItem.getItem();
    }

}
