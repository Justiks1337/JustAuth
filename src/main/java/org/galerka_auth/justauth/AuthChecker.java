package org.galerka_auth.justauth;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.InventoryBlockStartEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;


public class AuthChecker implements Listener {

    private boolean needToCancel(Player player) {
        return player.getPersistentDataContainer().has(AuthMeHandler.TAG_KEY);
    }

    private void cancel(Cancellable event, Entity entity) {
        if (entity instanceof Player player && needToCancel(player)) {
            event.setCancelled(true);
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
}

