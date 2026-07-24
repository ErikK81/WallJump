package com.erikk.walljump.utils;

import com.erikk.walljump.enums.WallFace;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.logging.Level;

public class EffectUtils {

    public static void spawnSlidingParticles(Player player, WallFace facing, String particleName, int amount, double speed) {
        try {
            if (amount <= 0)
                return;

            Location location = player.getLocation();
            Block wallBlock = LocationUtils.getBlockPlayerIsStuckOn(player, facing);
            if (wallBlock == null || !wallBlock.getType().isBlock())
                return;

            Particle particle = getParticle(particleName);
            Location particleLocation = location.clone().add(facing.xOffset * 0.3, 0.3, facing.zOffset * 0.3);

            if (particle == Particle.BLOCK) {
                player.getWorld().spawnParticle(
                        particle, particleLocation, amount, 0.2, 0.35, 0.2,
                        Math.max(0, speed), wallBlock.getBlockData()
                );
            } else {
                player.getWorld().spawnParticle(
                        particle, particleLocation, amount, 0.2, 0.35, 0.2,
                        Math.max(0, speed)
                );
            }
        }catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to spawn sliding particles for player {0}!", player.getName());
            Bukkit.getLogger().log(Level.WARNING, e.getMessage(), e);
        }
    }

    private static Particle getParticle(String particleName) {
        if (particleName == null || particleName.isBlank())
            return Particle.BLOCK;

        try {
            Particle particle = Particle.valueOf(particleName.trim().toUpperCase(Locale.ROOT));
            if (particle.getDataType() != Void.class && particle != Particle.BLOCK)
                return Particle.BLOCK;
            return particle;
        } catch (IllegalArgumentException e) {
            return Particle.BLOCK;
        }
    }

    public static void playWallJumpSound(Player player, WallFace facing, float volume, float pitch) {
        try {
            // Get the world the player is in and play a sound at their current location
            player.getWorld().playSound(
                    player.getLocation(),
                    // Get the block the player is stuck on in the given direction
                    //NmsUtils.getStepSoundForBlock(LocationUtils.getBlockPlayerIsStuckOn(player, facing)),
                    Material.STONE.createBlockData().getSoundGroup().getStepSound(),
                    volume, // Set the volume of the sound
                    pitch // Set the pitch of the sound
            );
        }catch (Exception e){
            Bukkit.getLogger().log(Level.WARNING, "Failed to play wall jump sound for player {0}!", player.getName());
            Bukkit.getLogger().warning(e.getMessage());
        }
    }
}
