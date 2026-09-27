package xyz.jpenilla.wanderingtrades.listener;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import xyz.jpenilla.wanderingtrades.WanderingTrades;
import xyz.jpenilla.wanderingtrades.config.TraderSpawnNotificationOptions;
import xyz.jpenilla.wanderingtrades.util.Constants;

@NullMarked
public final class TraderSpawnListener implements Listener {
    private final WanderingTrades plugin;

    public TraderSpawnListener(final WanderingTrades plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityPortal(final EntityPortalEvent event) {
        if (event.getEntityType() == EntityType.WANDERING_TRADER) {
            event.getEntity().getPersistentDataContainer().set(Constants.TEMPORARY_BLACKLISTED, PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(final CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof final WanderingTrader trader)
            || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.MOUNT) {
            return;
        }
        if (trader.getPersistentDataContainer().has(Constants.TEMPORARY_BLACKLISTED, PersistentDataType.BYTE)) {
            trader.getPersistentDataContainer().remove(Constants.TEMPORARY_BLACKLISTED);
            return;
        }

        this.plugin.scheduler().runAtEntityDelayed(trader, () -> {
            if (!trader.isValid()) {
                return;
            }
            this.notifyPlayers(trader);
            final boolean worldMatches = this.plugin.config().traderWorldList().contains(trader.getWorld().getName());
            if (this.plugin.config().traderWorldWhitelist() == worldMatches) {
                this.plugin.tradeApplicator().addTrades(trader);
            }
        }, 1L);
    }

    private void notifyPlayers(final WanderingTrader entity) {
        final TraderSpawnNotificationOptions options = this.plugin.config().traderSpawnNotificationOptions();
        if (!options.enabled() || !entity.isValid()) {
            return;
        }
        final Snapshot snapshot = new Snapshot(
            entity.getWorld().getName(),
            entity.getLocation().getBlockX(),
            entity.getLocation().getBlockY(),
            entity.getLocation().getBlockZ(),
            entity.getUniqueId().toString()
        );

        for (final String command : options.commands()) {
            final String resolved = applySnapshotReplacements(command, null, "", snapshot);
            this.plugin.scheduler().runGlobal(() -> this.plugin.getServer().dispatchCommand(
                this.plugin.getServer().getConsoleSender(), resolved
            ));
        }

        for (final Player player : options.notifyPlayers().find(entity)) {
            this.plugin.scheduler().runAtEntity(player, () -> {
                if (!player.hasPermission(Constants.Permissions.TRADER_SPAWN_NOTIFICATIONS)) {
                    return;
                }
                final String distance = player.getWorld().getName().equals(snapshot.world())
                    ? String.valueOf(Math.round(player.getLocation().distance(snapshot.location(player.getWorld()))))
                    : "";
                for (final String command : options.perPlayerCommands()) {
                    final String resolved = applySnapshotReplacements(command, player.getName(), distance, snapshot);
                    this.plugin.scheduler().runGlobal(() -> this.plugin.getServer().dispatchCommand(
                        this.plugin.getServer().getConsoleSender(), resolved
                    ));
                }
            });
        }
    }

    private static String applySnapshotReplacements(
        String command,
        final @Nullable String player,
        final String distance,
        final Snapshot snapshot
    ) {
        if (player != null) {
            command = command.replace("{player}", player);
        }
        return command.replace("{distance}", distance)
            .replace("{world-name}", snapshot.world())
            .replace("{x-pos}", String.valueOf(snapshot.x()))
            .replace("{y-pos}", String.valueOf(snapshot.y()))
            .replace("{z-pos}", String.valueOf(snapshot.z()))
            .replace("{trader-uuid}", snapshot.uuid());
    }

    private record Snapshot(String world, int x, int y, int z, String uuid) {
        private Location location(final org.bukkit.World world) {
            return new Location(world, x + 0.5, y, z + 0.5);
        }
    }
}
