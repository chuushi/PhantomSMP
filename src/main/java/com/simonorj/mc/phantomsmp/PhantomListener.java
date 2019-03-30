package com.simonorj.mc.phantomsmp;

import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

public class PhantomListener implements Listener {
    private static final String DISALLOW_SPAWN_PERM = "phantomsmp.disallowspawn";
    private static final String IGNORE_PERM = "phantomsmp.ignore";

    private Map<Player, LinkedHashSet<Phantom>> playerPhantomMap = new HashMap<>();
    private Map<Phantom, Player> phantomPlayerMap = new HashMap<>();
    private PhantomSMP plugin;

    PhantomListener() {
        this.plugin = PhantomSMP.getInstance();
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            this.playerPhantomMap.put(p, new LinkedHashSet<>());
        }

        // Initiate map
        for (World w : plugin.getServer().getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL)
                continue;

            for (Entity e : w.getLivingEntities())
                if (e instanceof Phantom)
                    addPhantom((Phantom) e);
        }
    }

    void disable() {
        this.playerPhantomMap = null;
        this.phantomPlayerMap = null;
        this.plugin = null;
    }

    private boolean phantomSpawnAllowed(Player p) {
        return p.getStatistic(Statistic.TIME_SINCE_REST) > plugin.disallowSpawningFor || p.hasPermission(DISALLOW_SPAWN_PERM);
    }

    private boolean phantomIgnore(Player p) {
        return plugin.removeTargetingRested || p.hasPermission(IGNORE_PERM);
    }

    private void addPhantom(Phantom phantom) {
        addPhantom(phantom, null);
    }

    private void addPhantom(Phantom phantom, Cancellable e) {
        addPhantom(phantom, null, e);
    }

    private void addPhantom(Phantom phantom, Player newTarget, Cancellable e) {
        if (newTarget == null && !(phantom.getTarget() instanceof Player)) {
            return;
        }

        Player p = newTarget != null ? newTarget : (Player) phantom.getTarget();

        // Player rested before
        if (!phantomSpawnAllowed(p)) {
            if (e != null)
                e.setCancelled(true);

            if (phantomIgnore(p))
                if (phantom.getCustomName() == null)
                    phantom.remove();
        }

        // Phantom spawn is legal
        playerPhantomMap.computeIfAbsent(p, k -> new LinkedHashSet<>()).add(phantom);
        phantomPlayerMap.put(phantom, p);
    }

    private void removePlayerPhantom(Player p) {
        Iterator<Phantom> i = playerPhantomMap.get(p).iterator();
        while(i.hasNext()) {
            Phantom phantom = i.next();
            if (phantom.getTarget() == p) {
                phantomPlayerMap.remove(phantom);
                phantom.setTarget(null);
            }
            i.remove();
        }
    }

    // Initiate when player joins
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerJoin(PlayerJoinEvent e) {
        playerPhantomMap.put(e.getPlayer(), new LinkedHashSet<>());
    }

    // Reset when player leaves
    @EventHandler
    public void playerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        for (Phantom phantom : playerPhantomMap.get(p)) {
            if (phantom.getTarget() == p) {
                phantom.setTarget(null);
                phantomPlayerMap.remove(phantom);
            }
        }
    }

    // Remove phantoms when player sleeps
    @EventHandler
    public void playerUseBed(PlayerBedEnterEvent e) {
        if (e.isCancelled())
            return;

        removePlayerPhantom(e.getPlayer());
    }

    @EventHandler
    public void playerDied(PlayerDeathEvent e) {
        removePlayerPhantom(e.getEntity());
    }

    // Check phantom when they spawn wrongly
    @EventHandler
    public void phantomSpawn(CreatureSpawnEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Phantom)) {
            return;
        }

        addPhantom((Phantom) e.getEntity(), e);
    }

    // Remove phantom that targets player who slept
    @EventHandler
    public void phantomTarget(EntityTargetLivingEntityEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Phantom && e.getTarget() instanceof Player)) {
            return;
        }

        addPhantom((Phantom) e.getEntity(), (Player) e.getTarget(), e);
    }

    // Check phantom in loaded chunks
    @EventHandler
    public void phantomInLoadedChunk(ChunkLoadEvent e) {
        if (e.getWorld().getEnvironment() != World.Environment.NORMAL)
            return;

        for (Entity ent : e.getChunk().getEntities())
            if (ent instanceof Phantom)
                addPhantom((Phantom) ent);
    }

    // Check phantom on death
    @EventHandler
    public void phantomDied(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Phantom))
            return;

        Phantom phantom = (Phantom) e.getEntity();

        Player p = phantomPlayerMap.remove(phantom);
        if (p == null)
            return;

        playerPhantomMap.get(p).remove(phantom);
    }
}
