package com.kebab.walljump.listeners;

import com.kebab.walljump.WallJump;
import com.kebab.walljump.player.PlayerManager;
import com.kebab.walljump.player.WPlayer;
import com.kebab.walljump.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerToggleSneakListener implements Listener {

    private final PlayerManager playerManager = WallJump.getInstance().getPlayerManager();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleSneak(@NotNull PlayerToggleSneakEvent event) {
        try {
            Player player = event.getPlayer();
            if(!player.isFlying()) {
                WPlayer wplayer = playerManager.getWPlayer(player);
                if(wplayer.isOnWall() && !event.isSneaking())
                    wplayer.onWallJumpEnd();
                else if(LocationUtils.isTouchingAWall(player) && event.isSneaking() && !player.isOnGround())
                    wplayer.onWallJumpStart();
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("An error occurred while handling a player toggle sneak event.");
        }
    }
}