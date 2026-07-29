package com.kebab.walljump.player;

import com.kebab.walljump.WallJump;
import com.kebab.walljump.api.events.WallJumpEndEvent;
import com.kebab.walljump.api.events.WallJumpResetEvent;
import com.kebab.walljump.api.events.WallJumpStartEvent;
import com.kebab.walljump.config.WallJumpConfiguration;
import com.kebab.walljump.enums.WallFace;
import com.kebab.walljump.handlers.WorldGuardHandler;
import com.kebab.walljump.utils.BukkitUtils;
import com.kebab.walljump.utils.EffectUtils;
import com.kebab.walljump.utils.LocationUtils;
import com.kebab.walljump.utils.VelocityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.logging.Level;

public class WPlayer {

    private final Player player;
    private boolean wallJumping;
    private boolean onWall;
    private boolean sliding;
    private WallFace lastFacing;
    private Location lastJumpLocation;
    private int remainingJumps = -1;
    private BukkitTask velocityTask;
    private BukkitTask fallTask;
    private float velocityY;
    private BukkitTask stopWallJumpingTask;
    private final WallJumpConfiguration config;
    private final WorldGuardHandler worldGuard;
    public boolean enabled = true;
    private int sameFaceJumpCount = 0;

    protected WPlayer(Player player) {
        this.player = player;
        config = WallJump.getInstance().getWallJumpConfig();
        worldGuard = WallJump.getInstance().getWorldGuardHandler();
    }

    public void onWallJumpStart() {
        try {
            if (!canWallJump()) return;

            WallJumpStartEvent event = new WallJumpStartEvent(this);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            onWall = true;
            wallJumping = true;

            WallFace currentFacing = LocationUtils.getPlayerFacing(player);
            if (lastFacing != null && lastFacing.equals(currentFacing)) {
                sameFaceJumpCount++;
            } else {
                sameFaceJumpCount = 0;
            }
            lastFacing = currentFacing;

            lastJumpLocation = player.getLocation();
            if (remainingJumps > 0) remainingJumps--;

            EffectUtils.playWallJumpSound(player, lastFacing, 0.3f, 1.2f);
            EffectUtils.spawnSlidingParticles(player, 5, lastFacing);

            velocityY = 0;
            if (BukkitUtils.isVersionBefore(BukkitUtils.Version.V1_9))
                velocityY = 0.04f;

            velocityTask = Bukkit.getScheduler().runTaskTimerAsynchronously(WallJump.getInstance(), () -> {
                player.setVelocity(new Vector(0, velocityY, 0));
                if (velocityY != 0) {
                    EffectUtils.spawnSlidingParticles(player, 2, lastFacing);
                    if (sliding) {
                        if (player.isOnGround() || !Objects.requireNonNull(LocationUtils.getBlockPlayerIsStuckOn(player, lastFacing)).getType().isSolid()) {
                            Bukkit.getScheduler().runTask(WallJump.getInstance(), () -> {
                                player.setFallDistance(0);
                                player.teleport(player.getLocation());
                                onWallJumpEnd(false);
                            });
                        }
                        if (lastJumpLocation.getY() - player.getLocation().getY() >= 1.2) {
                            lastJumpLocation = player.getLocation();
                            Bukkit.getScheduler().runTask(WallJump.getInstance(), () -> {
                                EffectUtils.playWallJumpSound(player, lastFacing, 0.2f, 0.6f);
                            });
                        }
                    }
                }
            }, 0, 1);

            if (fallTask != null) fallTask.cancel();
            fallTask = Bukkit.getScheduler().runTaskLaterAsynchronously(WallJump.getInstance(), () -> {
                if (onWall) {
                    if (config.getBoolean("slide")) {
                        velocityY = (float) -config.getDouble("slidingSpeed");
                        sliding = true;
                    } else {
                        Bukkit.getScheduler().runTask(WallJump.getInstance(), (Runnable) this::onWallJumpEnd);
                    }
                }
            }, (long)(config.getDouble("timeOnWall") * 20));

            if (stopWallJumpingTask != null) stopWallJumpingTask.cancel();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to start wall jump for player {0}!", player.getName());
        }
    }

    public void onWallJumpEnd() {
        onWallJumpEnd(true);
    }

    public void onWallJumpEnd(boolean jump) {
        try {
            onWall = false;
            sliding = false;

            player.setFallDistance(0);
            velocityTask.cancel();

            WallJumpEndEvent event = new WallJumpEndEvent(this, config.getDouble("horizontalJumpPower"), config.getDouble("verticalJumpPower"));
            Bukkit.getPluginManager().callEvent(event);

            if (jump && ((velocityY == 0 && player.getLocation().getPitch() < 85) ||
                    (config.getBoolean("canJumpWhileSliding") && player.getLocation().getPitch() < 60))) {
                VelocityUtils.pushPlayerInFront(player, event.getHorizontalPower(), event.getVerticalPower());
            }

            Bukkit.getScheduler().runTaskLaterAsynchronously(WallJump.getInstance(), () -> {
                if (LocationUtils.isOnGround(player)) {
                    reset();
                }
            }, 12);

            stopWallJumpingTask = Bukkit.getScheduler().runTaskLaterAsynchronously(WallJump.getInstance(), this::reset, 24);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to end wall jump for player {0}!", player.getName());
        }
    }

    private void reset() {
        try {
            wallJumping = false;
            lastFacing = null;
            lastJumpLocation = null;
            sameFaceJumpCount = 0;

            remainingJumps = config.getInt("maxJumps");
            if (remainingJumps == 0) remainingJumps = -1;

            if (stopWallJumpingTask != null) stopWallJumpingTask.cancel();
            stopWallJumpingTask = null;

            Bukkit.getScheduler().runTask(WallJump.getInstance(), () -> {
                WallJumpResetEvent event = new WallJumpResetEvent(this);
                Bukkit.getPluginManager().callEvent(event);
            });
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to reset wall jump for player {0}!", player.getName());
        }
    }

    public boolean canWallJump() {
        try {
            WallFace facing = LocationUtils.getPlayerFacing(player);
            if(lastJumpLocation != null)
                lastJumpLocation.setY(player.getLocation().getY());

            int maxSameFaceJumps = player.hasPermission("walljump.extrajump") ? 1 : 0;
            boolean isSameWallExtraJump = (lastFacing != null && lastFacing.equals(facing) && sameFaceJumpCount < maxSameFaceJumps);

            if(!enabled ||
                    onWall ||
                    remainingJumps == 0 ||
                    (lastFacing != null && lastFacing.equals(facing) && sameFaceJumpCount >= maxSameFaceJumps) ||
                    (!isSameWallExtraJump && lastJumpLocation != null && player.getLocation().distance(lastJumpLocation) <= config.getDouble("minimumDistance")) ||
                    player.getVelocity().getY() < config.getDouble("maximumVelocity") ||
                    (config.getBoolean("needPermission") && !player.hasPermission("walljump.use")) ||
                    (worldGuard != null && !worldGuard.canWallJump(player))) {
                return false;
            }

            boolean onBlacklistedBlock = config.getMaterialList("blacklistedBlocks").contains(
                    player.getLocation().clone().add(facing.xOffset, facing.yOffset, facing.zOffset)
                            .getBlock()
                            .getType());
            boolean reverseBlockBlacklist = config.getBoolean("reversedBlockBlacklist");

            if((!reverseBlockBlacklist && onBlacklistedBlock) || (reverseBlockBlacklist && !onBlacklistedBlock)) {
                return false;
            }

            boolean inBlacklistedWorld = config.getWorldList("blacklistedWorlds").contains(player.getWorld());
            boolean reverseWorldBlacklist = config.getBoolean("reversedWorldBlacklist");

            return (reverseWorldBlacklist || !inBlacklistedWorld) && (!reverseWorldBlacklist || inBlacklistedWorld);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to check if player {0} can wall jump!", player.getName());
            return false;
        }
    }

    public boolean isOnWall() {
        return onWall;
    }

    public boolean isWallJumping() {
        return wallJumping;
    }

    public boolean isSliding() {
        return sliding;
    }

    public Player getPlayer() {
        return player;
    }
}