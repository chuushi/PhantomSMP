# ![Logo](https://www.spigotmc.org/data/resource_icons/59/59721.jpg) PhantomSMP

Better Phantom handling for SMP

# Configuration

There are two configuration options that you can set.

* `remove-targeting-rested`: *Default: true.* Remove phantoms that try to
  target players who recently rested.
* `remove-when-sleeping`: *Default: false.* Remove phantoms as soon as player
  gets in the bed.
* `disallow-targeting-for`: *Default: 72000.* Ticks since player's last rest
  before a phantom starts targeting them. 

For additional information, check [`config.yml`](src/main/resources/config.yml).

## Permissions

All permission nodes are not applied by default, even to server operators.

* `phantomsmp.disallowspawn`: If given, phantoms will not spawn on this player.
* `phantomsmp.ignore`: If given, phantoms will essentially ignore this player.

For additional information, check [`plugin.yml`](src/main/resources/plugin.yml).

# Disallowing Phantom Spawn

The Bukkit API does not allow for directly checking which player caused the
phantoms to spawn.  Therefore, if `disallow-targeting-for` is greater than
72000, phantoms will spawn in for a few seconds until they start targeting a
player.  When the very first target of the phantom is a well-rested player,
they will despawn immediately.  The same happens when the player is given the
`phantomsmp.disallowspawn` permission.

# External Links

* [Spigot resource link](https://www.spigotmc.org/resources/phantomsmp.59721/)
* [BukkitDev resource link](https://dev.bukkit.org/projects/phantomsmp)
