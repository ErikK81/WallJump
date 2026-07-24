package com.erikk.walljump.listeners;

import com.erikk.walljump.WallJump;
import com.erikk.walljump.player.PlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final PlayerManager playerManager = WallJump.getInstance().getPlayerManager();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        playerManager.registerPlayer(event.getPlayer());
    }

}
