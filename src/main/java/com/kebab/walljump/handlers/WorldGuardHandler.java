package com.kebab.walljump.handlers;

import com.kebab.walljump.WallJump;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public final class WorldGuardHandler {

    private static final String WALL_JUMP_FLAG_NAME = "wall-jump";

    private final WallJump plugin;
    private final boolean defaultAllow;
    private StateFlag wallJumpFlag;

    public WorldGuardHandler(WallJump plugin) {
        this.plugin = plugin;
        this.defaultAllow = plugin.getWallJumpConfig().getBoolean("worldGuardFlagDefault");
        registerFlag();
    }

    private void registerFlag() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        StateFlag flag = new StateFlag(WALL_JUMP_FLAG_NAME, defaultAllow);

        try {
            registry.register(flag);
            wallJumpFlag = flag;
            plugin.getLogger().info("Registered WorldGuard flag: " + WALL_JUMP_FLAG_NAME);
        } catch (FlagConflictException exception) {
            Flag<?> existingFlag = registry.get(WALL_JUMP_FLAG_NAME);
            if (existingFlag instanceof StateFlag existingStateFlag) {
                wallJumpFlag = existingStateFlag;
                plugin.getLogger().info("Using existing WorldGuard flag: " + WALL_JUMP_FLAG_NAME);
            } else {
                plugin.getLogger().severe(
                        "The WorldGuard flag '" + WALL_JUMP_FLAG_NAME
                                + "' already exists with an incompatible type. WorldGuard integration is disabled."
                );
            }
        }
    }

    public boolean canWallJump(Player player) {
        if (wallJumpFlag == null) {
            return true;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            StateFlag.State state = query.queryState(
                    BukkitAdapter.adapt(player.getLocation()),
                    WorldGuardPlugin.inst().wrapPlayer(player),
                    wallJumpFlag
            );

            return state == null ? defaultAllow : state == StateFlag.State.ALLOW;
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.WARNING,
                    "Could not query the WorldGuard flag for player " + player.getName() + "; allowing wall jump.",
                    exception
            );
            return true;
        }
    }
}
