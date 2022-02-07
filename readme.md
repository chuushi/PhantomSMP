# ![Logo](https://www.spigotmc.org/data/resource_icons/59/59721.jpg) PhantomSMP
![Bukkit](https://img.shields.io/badge/bukkit-1.13%20--%201.14-blue.svg)
[![GitHub All Release Downloads](https://img.shields.io/github/downloads/SimonOrJ/PhantomSMP/total.svg?label=github%20downloads)](https://github.com/SimonOrJ/PhantomSMP/releases)
[![GitHub release](https://img.shields.io/github/release/SimonOrJ/PhantomSMP.svg)](https://github.com/SimonOrJ/PhantomSMP/releases/latest)
[![GitHub pre-release](https://img.shields.io/github/release-pre/SimonOrJ/PhantomSMP.svg?label=pre-release)](https://github.com/SimonOrJ/PhantomSMP/releases)
[![Build Status](https://travis-ci.org/SimonOrJ/PhantomSMP.svg?branch=master)](https://travis-ci.org/SimonOrJ/PhantomSMP) [![Maintainability](https://api.codeclimate.com/v1/badges/ec57a5bdcfdb28f9d5cb/maintainability)](https://codeclimate.com/github/SimonOrJ/PhantomSMP/maintainability)

*Better Phantom handling for SMP*

This plugin was created to combat phantom issues in the survival Multiplayer
server environment.

Normally, phantoms spawn above players who had three or more Minecraft days of
restlessness. After the phantom(s) spawn, when the player uses the bed or is
killed, phantoms will continue to haunt the same player until they cannot find
the player!  While they still exist, they will move on to assault any other
players regardless of if they rested.

In single player, the above will not be any problem since the lone player can
skip the night and let the sun kill off the phantoms. However, that's not the
case on multiplayer servers where players have to endure phantom attacks until
the next sunrise.

This plugin addresses this issue. Features:

* Phantoms will ignore players who used the bed within last three Minecraft
  days. (The duration is configurable)
* Phantoms will ignore or despawn on players who uses the bed or is killed.
  This is configurable as well.
* Control if the phantoms should despawn when they try to target a rested
  player using a configuration option.

# Configuration

There are three core configuration options that you can set.

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
* [bStats Metrics](https://bstats.org/plugin/bukkit/PhantomSMP/)

[![bStats PhantomSMP Signature](https://bstats.org/signatures/bukkit/PhantomSMP.svg)](https://bstats.org/plugin/bukkit/PhantomSMP/)
