package com.kebab.walljump.api.events;

import com.kebab.walljump.player.WPlayer;
import org.jetbrains.annotations.NotNull;

public class WallJumpResetEvent extends WallJumpEvent {

    public WallJumpResetEvent(@NotNull WPlayer who) {
        super(who);
    }

}
