package com.kebab.walljump.listeners;

import com.kebab.walljump.WallJump;
import com.kebab.walljump.player.PlayerManager;
import com.kebab.walljump.utils.LocationUtils;
import com.kebab.walljump.api.events.GroundPoundEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerGroundPoundListener implements Listener {

    private final PlayerManager playerManager;

    public PlayerGroundPoundListener() {
        playerManager = WallJump.getInstance().getPlayerManager();
    }

    // This method handles the GroundPoundEvent
    @EventHandler
    public void onGroundPound(GroundPoundEvent event) {
        try {
            // If the player is wall jumping or touching a wall, cancel the event
            if(playerManager.getWPlayer(event.getPlayer()).isWallJumping() || LocationUtils.isTouchingAWall(event.getPlayer()))
                event.setCancelled(true);
        }catch (Exception e){
            Bukkit.getLogger().warning("An error occurred while handling the GroundPoundEvent");
        }
    }
}
