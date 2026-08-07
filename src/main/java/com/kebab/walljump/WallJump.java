package com.kebab.walljump;

import com.kebab.walljump.api.WallJumpAPI;
import com.kebab.walljump.command.WallJumpCommand;
import com.kebab.walljump.config.WallJumpConfiguration;
import com.kebab.walljump.listeners.*;
import com.kebab.walljump.handlers.WorldGuardHandler;
import com.erikk.walljump.listeners.*;
import com.kebab.walljump.player.PlayerManager;
import com.kebab.walljump.player.WPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;
import java.util.logging.Level;

// This class is the main class of the plugin
public final class WallJump extends JavaPlugin {

    private static WallJump plugin;
    private PlayerManager playerManager;
    private WallJumpConfiguration config;
    private WallJumpConfiguration dataConfig;
    private WorldGuardHandler worldGuard;

    public WallJumpAPI api;

    public static WallJump getInstance() {
        return plugin;
    }
    public WallJumpAPI getAPI() { return api; }
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    public WallJumpConfiguration getWallJumpConfig() {
        return config;
    }
    public WallJumpConfiguration getDataConfig() {
        return dataConfig;
    }
    public WorldGuardHandler getWorldGuardHandler() {
        return worldGuard;
    }


    @Override
    public void onEnable() {
        try {
            playerManager = new PlayerManager();
            Bukkit.getLogger().info("[Walljump] PlayerManager initialized.");

            registerEvents(
                    new PlayerJoinListener(),
                    new PlayerQuitListener(),
                    new PlayerToggleSneakListener(),
                    new PlayerDamageListener(),
                    new PlayerGroundPoundListener()
            );
            Bukkit.getLogger().info("[Walljump] Listeners registered.");

            Objects.requireNonNull(this.getCommand("walljump")).setExecutor(new WallJumpCommand());

            //in case the plugin has been loaded while the server is running using plugman or any other similar methods, register all the online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerManager.registerPlayer(player);
            }

            api = new WallJumpAPI();
            Bukkit.getLogger().info("[Walljump] WallJump has been enabled!");

        }catch (Exception e) {
            Bukkit.getLogger().info("WallJump has failed to enable!");
            Bukkit.getLogger().log(Level.INFO, "Error: {0}", e.getMessage());
        }
    }

    @Override
    public void onLoad() {
        plugin = this;
        config = new WallJumpConfiguration("config.yml");
        dataConfig = new WallJumpConfiguration("data.yml");

        Plugin worldGuardPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldGuardPlugin != null) {
            worldGuard = new WorldGuardHandler(this);
        }
    }

    @Override
    public void onDisable() {
        if (config.getBoolean("toggleCommand")) {
            for (WPlayer wplayer : playerManager.getWPlayers()) {
                dataConfig.set(wplayer.getPlayer().getUniqueId().toString(), wplayer.enabled);
            }
            dataConfig.save();
        }
        Bukkit.getLogger().info("WallJump has been disabled!");
    }

    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }
}
