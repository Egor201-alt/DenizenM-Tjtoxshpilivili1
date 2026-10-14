package com.denizenscript.denizen.utilities.placeholder;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class DenizenPlaceholderExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return AsyncPlaceholders.IDENTIFIER;
    }

    @Override
    public String getAuthor() {
        return "The DenizenScript Team";
    }

    @Override
    public String getVersion() {
        return "2.0.0-async";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        try {
            return TagManager.tag(params, new BukkitTagContext(PlayerTag.mirrorBukkitPlayer(player), null, null, false, null));
        }
        catch (Throwable ex) {
            Debug.echoError("Failed to read placeholder '%denizen_" + params + "%':");
            Debug.echoError(ex);
            return null;
        }
    }
}
