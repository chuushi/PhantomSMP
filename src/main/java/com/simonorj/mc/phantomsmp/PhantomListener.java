package com.simonorj.mc.phantomsmp;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PhantomListener implements Listener {
    private static final String DISALLOW_SPAWN_PERM = "phantomsmp.disallowspawn";
    private static final String IGNORE_PERM = "phantomsmp.ignore";

    private final NamespacedKey newlySpawnedKey;

    private final Multimap<UUID, UUID> playerPhantomMap = MultimapBuilder.hashKeys().hashSetValues().build();
    private final Map<UUID, UUID> phantomPlayerMap = new HashMap<>();
    private final PhantomSMP plugin;

    PhantomListener() {
        this.plugin = PhantomSMP.getInstance();
        newlySpawnedKey = new NamespacedKey(plugin, "newlyspawned");

        // Initiate map
        for (World w : plugin.getServer().getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL)
                continue;

            for (Entity e : w.getLivingEntities())
                if (e instanceof Phantom)
                    targeting((Phantom) e, null, null);
        }
    }

    private boolean recentlyRested(Player p) {
        return p.getStatistic(Statistic.TIME_SINCE_REST) < plugin.disallowSpawningFor;
    }

    private void targeting(Phantom phantom, Player newTarget, Cancellable e) {
        // Clean up old target
        UUID old = phantomPlayerMap.remove(phantom.getUniqueId());
        if (old != null) {
            playerPhantomMap.remove(old, phantom.getUniqueId());
        }

        // get new target
        Player p;
        if (newTarget != null)
            p = newTarget;
        else if (phantom.getTarget() instanceof Player)
            p = (Player) phantom.getTarget();
        else
            return;

        boolean newlySpawned = phantom.getPersistentDataContainer().has(newlySpawnedKey, PersistentDataType.BYTE);
        if (newlySpawned) {
            phantom.getPersistentDataContainer().remove(newlySpawnedKey);
        }
        if (ignorePlayer(p, newlySpawned)) {
            if (e != null)
                e.setCancelled(true);

            if (newlySpawned || !p.hasPermission(IGNORE_PERM) && plugin.removeTargetingRested && phantom.getCustomName() == null)
                phantom.remove();
            else
                phantom.setTarget(null);
        }

        // Phantom target is legal
        playerPhantomMap.put(p.getUniqueId(), phantom.getUniqueId());
        phantomPlayerMap.put(phantom.getUniqueId(), p.getUniqueId());
    }

    private boolean ignorePlayer(Player p, boolean newlySpawnedPhantom) {
        return p.hasPermission(IGNORE_PERM) || newlySpawnedPhantom && p.hasPermission(DISALLOW_SPAWN_PERM) || recentlyRested(p);
    }

    private void spawned(Phantom phantom) {
        phantom.getPersistentDataContainer().set(newlySpawnedKey, PersistentDataType.BYTE, (byte) 1);
    }

    private void untarget(Player p, boolean sleeping) {
        Iterator<UUID> i = playerPhantomMap.get(p.getUniqueId()).iterator();
        while(i.hasNext()) {
            Entity entity = plugin.getServer().getEntity(i.next());
            if (entity instanceof Phantom) {
                Phantom phantom = (Phantom) entity;
                if (phantom.getTarget() == p) {
                    phantomPlayerMap.remove(phantom.getUniqueId());
                    phantom.setTarget(null);
                }

                if (sleeping && plugin.removeWhenSleeping)
                    phantom.remove();
            }
            i.remove();
        }
    }

    private void removePhantom(Phantom phantom) {
        UUID p = phantomPlayerMap.remove(phantom.getUniqueId());
        if (p == null)
            return;
        playerPhantomMap.remove(p, phantom.getUniqueId());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        for (UUID phantomId : playerPhantomMap.removeAll(p.getUniqueId())) {
            Entity entity = plugin.getServer().getEntity(phantomId);
            if (entity instanceof Phantom && ((Phantom) entity).getTarget() == p) {
                ((Phantom) entity).setTarget(null);
                phantomPlayerMap.remove(entity.getUniqueId(), p.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPhantomSpawn(CreatureSpawnEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Phantom)) {
            return;
        }

        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL)
            spawned((Phantom) e.getEntity());
    }

    @EventHandler
    public void onPlayerSleeping(PlayerBedEnterEvent e) {
        if (e.isCancelled())
            return;

        untarget(e.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        untarget(e.getEntity(), false);
    }

    // Remove phantom that targets player who slept
    @EventHandler
    public void onPhantomTargeting(EntityTargetLivingEntityEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Phantom && e.getTarget() instanceof Player)) {
            return;
        }

        targeting((Phantom) e.getEntity(), (Player) e.getTarget(), e);
    }

    @EventHandler
    public void onPhantomInLoadedChunk(ChunkLoadEvent e) {
        if (e.isNewChunk() || e.getWorld().getEnvironment() != World.Environment.NORMAL)
            return;

        for (Entity ent : e.getChunk().getEntities())
            if (ent instanceof Phantom)
                targeting((Phantom) ent, null, null);
    }

    @EventHandler
    public void onPhantomInUnloadedChunk(ChunkUnloadEvent e) {
        if (e.getWorld().getEnvironment() != World.Environment.NORMAL)
            return;

        for (Entity ent : e.getChunk().getEntities())
            if (ent instanceof Phantom)
                removePhantom((Phantom) ent);
    }

    @EventHandler
    public void onPhantomDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Phantom))
            return;

        removePhantom((Phantom) e.getEntity());
    }
}
