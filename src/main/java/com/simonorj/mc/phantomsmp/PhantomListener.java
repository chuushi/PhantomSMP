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
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.*;

public class PhantomListener implements Listener {
    private static final String DISALLOW_SPAWN_PERM = "phantomsmp.disallowspawn";
    private static final String IGNORE_PERM = "phantomsmp.ignore";

    private final Map<Player, LinkedHashSet<Phantom>> playerPhantomMap = new HashMap<>();
    private final Map<Phantom, Player> phantomPlayerMap = new HashMap<>();
    private final Set<Phantom> newPhantom = new LinkedHashSet<>();
    private final PhantomSMP plugin;

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
                    targeting((Phantom) e, null, null);
        }
    }

    private boolean recentlyRested(Player p) {
        return p.getStatistic(Statistic.TIME_SINCE_REST) < plugin.disallowSpawningFor;
    }

    private void targeting(Phantom phantom, Player newTarget, Cancellable e) {
        // Clean up old target
        Player old = phantomPlayerMap.remove(phantom);
        if (old != null) {
            playerPhantomMap.get(old).remove(phantom);
        }

        // get new target
        Player p;
        if (newTarget != null)
            p = newTarget;
        else if (phantom.getTarget() instanceof Player)
            p = (Player) phantom.getTarget();
        else
            return;

        boolean ignore = p.hasPermission(IGNORE_PERM);

        // If newly spawned phantom
        if (newPhantom.remove(phantom)) {
            if (ignore || p.hasPermission(DISALLOW_SPAWN_PERM) || recentlyRested(p)) {
                if (e != null)
                    e.setCancelled(true);
                phantom.remove();
                return;
            }
        }

        // If targeting is not allowed
        if (ignore || recentlyRested(p)) {
            if (e != null)
                e.setCancelled(true);
            else
                phantom.setTarget(null);

            if (!ignore && plugin.removeTargetingRested && phantom.getCustomName() == null)
                phantom.remove();

            return;
        }

        // Phantom target is legal
        playerPhantomMap.computeIfAbsent(p, k -> new LinkedHashSet<>()).add(phantom);
        phantomPlayerMap.put(phantom, p);
    }

    private void spawned(Phantom phantom) {
        newPhantom.add(phantom);
    }

    private void untarget(Player p, boolean sleeping) {
        Iterator<Phantom> i = playerPhantomMap.get(p).iterator();
        while(i.hasNext()) {
            Phantom phantom = i.next();
            if (phantom.getTarget() == p) {
                phantomPlayerMap.remove(phantom);
                phantom.setTarget(null);
            }

            if (sleeping && plugin.removeWhenSleeping)
                phantom.remove();
            i.remove();
        }
    }

    private void removePhantom(Phantom phantom) {
        Player p = phantomPlayerMap.remove(phantom);
        if (p == null)
            return;
        playerPhantomMap.get(p).remove(phantom);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        playerPhantomMap.put(e.getPlayer(), new LinkedHashSet<>());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        Set<Phantom> phantoms = playerPhantomMap.get(p);

        if (phantoms == null) {
            plugin.getLogger().warning("Phantom list for '" + p.getName() + "' was not initiated! Was there an error during login?");
            return;
        }

        for (Phantom phantom : phantoms) {
            if (phantom.getTarget() == p) {
                phantom.setTarget(null);
                phantomPlayerMap.remove(phantom);
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
        if (e.getWorld().getEnvironment() != World.Environment.NORMAL)
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
