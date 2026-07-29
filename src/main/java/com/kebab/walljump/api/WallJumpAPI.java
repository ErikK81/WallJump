package com.kebab.walljump.api;

import com.kebab.walljump.WallJump;
import com.kebab.walljump.config.WallJumpConfiguration;
import com.kebab.walljump.player.PlayerManager;
import com.kebab.walljump.player.WPlayer;
import org.bukkit.entity.Player;

public class WallJumpAPI {

    private static PlayerManager playerManager;
    private static WallJumpConfiguration config;

    public WallJumpAPI() {
        WallJump wallJump = WallJump.getInstance();
        playerManager = wallJump.getPlayerManager();
        config = wallJump.getWallJumpConfig();
    }

    public static WPlayer getWPlayer(Player player) {
        return playerManager.getWPlayer(player);
    }
    public static int getMaxJumps() {
        return config.getInt("maxJumps");
    }
    public static boolean requiresPermission() {
        return config.getBoolean("needPermission");
    }
    public static float getDefaultHorizontalPower() {
        return (float)config.getDouble("horizontalJumpPower");
    }
    public static float getDefaultVerticalPower() {
        return (float) config.getDouble("verticalJumpPower");
    }
    public static boolean isSlidingEnabled() {
        return config.getBoolean("slide");
    }
}
