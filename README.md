# WallJump

WallJump is a lightweight Paper plugin that adds wall jumping and wall sliding
to Minecraft. Players can grab a nearby wall while airborne, remain attached
for a configurable amount of time, and jump away from it.

## Requirements

- Paper 1.21.11
- Java 21 or newer
- WorldGuard 7.0.17 (optional)

## Installation

1. Download or build `walljump-4.0.3.jar`.
2. Place the JAR in the server's `plugins` directory.
3. Restart the server.
4. Edit `plugins/WallJump/config.yml` as needed and restart or reload the
   configuration.

To build the plugin locally:

```shell
./gradlew build
```

On Windows, use `gradlew.bat build`. The generated JAR is placed in
`build/libs`.

## Usage

While airborne and next to a solid wall, press sneak to attach to it. Release
sneak to jump away. Jump strength, wall time, sliding behavior, jump limits,
and block or world restrictions can be changed in `config.yml`.

Available commands:

| Command | Description |
| --- | --- |
| `/walljump help` | Displays the command help. |
| `/walljump info` | Displays plugin information. |
| `/walljump reload` | Reloads `config.yml`. |
| `/walljump toggle on\|off` | Enables or disables wall jumping for the player. |

Command aliases are `/wj` and `/wjump`. Player toggling must be enabled with
`toggleCommand: true`.

When `needPermission` is enabled, players require the `walljump.use`
permission to wall-jump. The `walljump.reload` permission controls whether the
reload option appears in tab completion.

## WorldGuard

When WorldGuard is installed, WallJump registers the `wall-jump` state flag.
It can be configured per region:

```text
/rg flag <region> wall-jump allow
/rg flag <region> wall-jump deny
```

Overlapping regions follow WorldGuard's normal priority, inheritance, and
membership rules. The `worldGuardFlagDefault` setting controls behavior when
no region provides an explicit value:

- `true`: wall jumping is allowed unless denied.
- `false`: wall jumping is denied unless allowed.

WorldGuard is optional; without it, region checks are skipped.

## Configuration

The default configuration includes:

- Horizontal and vertical jump power
- Time spent attached to a wall
- Wall sliding and sliding speed
- Particle type, amount, and speed (`BLOCK` matches the wall block)
- Maximum consecutive jumps
- Block and world blacklists
- Optional permission checks and per-player toggling
- Default WorldGuard flag behavior

After changing the configuration, use `/walljump reload` or restart the server.
