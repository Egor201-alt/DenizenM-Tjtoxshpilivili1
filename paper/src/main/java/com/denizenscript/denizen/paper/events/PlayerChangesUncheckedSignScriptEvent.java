package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import io.papermc.paper.event.packet.UncheckedSignChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerChangesUncheckedSignScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player changes unchecked sign
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Cancellable true
    //
    // @Triggers when a player sends sign text for a sign the server does not believe they are editing - for example one that was broken or replaced
    // while the edit screen was open, or one they never opened at all. <@link event player changes sign> does not fire for these, so this is the event
    // to use for catching sign edits that arrive outside the normal flow.
    //
    // @Context
    // <context.location> returns the LocationTag of the sign being edited.
    // <context.side> returns an ElementTag of the side of the sign that was edited (FRONT or BACK).
    // <context.new> returns the text the player sent, as a ListTag of lines.
    // <context.old> returns the sign's current text, as a ListTag of lines. Returns nothing when the block is no longer a sign, which this event can
    // fire for by its nature - check it with <@link tag ObjectTag.exists> before relying on it.
    //
    // @Player Always.
    //
    // -->

    public PlayerChangesUncheckedSignScriptEvent() {
        registerCouldMatcher("player changes unchecked sign");
    }

    public UncheckedSignChangeEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, event.getPlayer().getLocation())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "location" -> {
                double x = event.getEditedBlockPosition().x();
                double y = event.getEditedBlockPosition().y();
                double z = event.getEditedBlockPosition().z();

                yield new LocationTag(event.getPlayer().getWorld(), x, y, z);
            }
            case "side" -> new ElementTag(event.getSide());
            case "new" -> {
                ListTag lines = new ListTag();
                for (Component line : event.lines()) {
                    String legacyText = LegacyComponentSerializer.legacySection().serialize(line);
                    lines.addObject(new ElementTag(legacyText));
                }
                yield lines;
            }
            case "old" -> {
                double x = event.getEditedBlockPosition().x();
                double y = event.getEditedBlockPosition().y();
                double z = event.getEditedBlockPosition().z();

                BlockState state = event.getPlayer().getWorld().getBlockAt((int) x, (int) y, (int) z).getState();
                if (state instanceof Sign sign) {
                    ListTag lines = new ListTag();
                    for (Component line : sign.lines()) {
                        String legacyText = LegacyComponentSerializer.legacySection().serialize(line);
                        lines.addObject(new ElementTag(legacyText));
                    }
                    yield lines;
                }
                yield null;
            }
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerUncheckedSignChange(UncheckedSignChangeEvent event) {
        if (EntityTag.isNPC(event.getPlayer())) {
            return;
        }
        this.event = event;
        fire(event);
    }
}
