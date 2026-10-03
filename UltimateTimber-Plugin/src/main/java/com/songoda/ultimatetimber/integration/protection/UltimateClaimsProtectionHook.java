package com.songoda.ultimatetimber.integration.protection;

import com.songoda.core.hooks.protection.ProtectionHook;
import com.songoda.ultimateclaims.api.UltimateClaimsApi;
import com.songoda.ultimateclaims.api.claim.ClaimPermission;
import com.songoda.ultimateclaims.api.claim.ClaimsService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * UltimateClaims protection provider using its public API.
 */
public final class UltimateClaimsProtectionHook implements ProtectionHook {

    @Override
    public boolean canBreak(@NotNull Player player, @NotNull Location location) {
        return hasPermission(player, location, ClaimPermission.BREAK);
    }

    @Override
    public boolean canInteract(@NotNull Player player, @NotNull Location location) {
        return hasPermission(player, location, ClaimPermission.INTERACT);
    }

    private boolean hasPermission(Player player, Location location, ClaimPermission permission) {
        ClaimsService claimsService = UltimateClaimsApi.getClaimsService();
        return claimsService == null || claimsService.hasPermission(player, location.getChunk(), permission.getKey());
    }
}
