package com.denizenscript.denizen.utilities.placeholder;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.events.ExpansionRegisterEvent;
import me.clip.placeholderapi.events.ExpansionUnregisterEvent;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.manager.LocalExpansionManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AsyncPlaceholders implements Listener {

    public static final String IDENTIFIER = "denizen";

    public static DenizenPlaceholderExpansion expansion;

    public static void init() {
        expansion = new DenizenPlaceholderExpansion();
        Bukkit.getPluginManager().registerEvents(new AsyncPlaceholders(), Denizen.getInstance());
        takeOver();
    }

    public static void takeOver() {
        if (expansion == null) {
            return;
        }
        LocalExpansionManager manager = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager();
        PlaceholderExpansion current = manager.getExpansion(IDENTIFIER);
        if (current == expansion) {
            return;
        }
        if (current != null) {
            String owner = current.getClass().getName();
            if (!manager.unregister(current)) {
                Debug.echoError("Could not take the '%" + IDENTIFIER + "_...%' placeholders over from " + owner + " - they will stay sync-only.");
                return;
            }
            Debug.log("Took the '%" + IDENTIFIER + "_...%' placeholders over from <A>" + owner + "<W>, so they can be read off the main thread. Disable 'Scripts.Placeholders.Async parsing' in the Denizen config to leave them alone.");
        }
        else {
            Debug.log("Registered the '%" + IDENTIFIER + "_...%' placeholders, readable off the main thread.");
        }
        if (!manager.register(expansion)) {
            Debug.echoError("Failed to register Denizen's own '%" + IDENTIFIER + "_...%' placeholder expansion - placeholders will not work until the server restarts.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExpansionRegister(ExpansionRegisterEvent event) {
        PlaceholderExpansion registering = event.getExpansion();
        if (registering == expansion || !IDENTIFIER.equalsIgnoreCase(registering.getIdentifier())) {
            return;
        }
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(Denizen.getInstance(), AsyncPlaceholders::takeOver);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExpansionUnregister(ExpansionUnregisterEvent event) {
        if (event.getExpansion() != expansion) {
            return;
        }
        Bukkit.getScheduler().runTask(Denizen.getInstance(), AsyncPlaceholders::takeOver);
    }
}
