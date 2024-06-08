package org.galerka_auth.justauth;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.time.Duration;
import java.time.Instant;


public class AuthChecker implements Listener {

    private boolean needToCancel(Player player) {
        return player.getPersistentDataContainer().has(AuthMeHandler.TAG_KEY);
    }

    private void cancel(Cancellable event, Entity entity) {
        if (entity instanceof Player player && needToCancel(player)) {
            if (player.getPersistentDataContainer().get(AuthMeHandler.TIME_TO_KICK_KEY, PersistentDataType.LONG) + 120 < Instant.now().getEpochSecond()) {
                player.kick();
            }
            event.setCancelled(true);
            sendTitle(player);
        }
    }

    private <T extends PlayerEvent & Cancellable> void cancel(T event) {
        cancel(event, event.getPlayer());
    }

    private <T extends EntityEvent & Cancellable> void cancel(T event) {
        cancel(event, event.getEntity());
    }

    private <T extends InventoryEvent & Cancellable> void cancel(T event) {
        cancel(event, event.getView().getPlayer());
    }

    private void sendTitle(Player player) {
        Title.Times times = Title.Times.times(Duration.ofMillis(10 * 50), Duration.ofMillis(70 * 50), Duration.ofMillis(20 * 50));
        Title authTitle = Title.title(
                Component.text(JustAuth.getInstance().getConfig().getString("title")),
                Component.text(JustAuth.getInstance().getConfig().getString("subtitle")),
                times
        );
        player.showTitle(authTitle);
    }

    @EventHandler
    private void event(PlayerPickupArrowEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(PlayerPickupExperienceEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(EntityPickupItemEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(PlayerDropItemEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(PlayerInteractEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(BlockBreakEvent ev) {
        cancel(ev, ev.getPlayer());
    }

    @EventHandler
    private void event(BlockPlaceEvent ev) {
        cancel(ev, ev.getPlayer());
    }

    @EventHandler
    private void event(EntityDamageEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(FoodLevelChangeEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(EntityDamageByEntityEvent ev) {
        cancel(ev, ev.getDamager());
    }

    @EventHandler
    private void event(PlayerArmorStandManipulateEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(PlayerMoveEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(InventoryClickEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(AsyncPlayerChatEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(PlayerCommandPreprocessEvent ev) {
        cancel(ev);
    }

    @EventHandler
    private void event(PlayerJoinEvent ev) {
        ev.getPlayer().getPersistentDataContainer().remove(AuthMeHandler.TAG_KEY);
        ev.getPlayer().getPersistentDataContainer().remove(AuthMeHandler.TIME_TO_KICK_KEY);
    }
}

