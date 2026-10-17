package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PaperPostEffects {

    private static Method PLAYER_POST_EFFECTS, VALUES, SET, ADD, REMOVE, CLEAR;

    public static boolean isSupported() {
        try {
            Class<?> effectsClass = Class.forName("io.papermc.paper.entity.PlayerPostEffects");
            PLAYER_POST_EFFECTS = Player.class.getMethod("postEffects");
            VALUES = effectsClass.getMethod("values");
            SET = effectsClass.getMethod("set", java.util.SequencedCollection.class);
            ADD = effectsClass.getMethod("add", Key.class);
            REMOVE = effectsClass.getMethod("remove", Key.class);
            CLEAR = effectsClass.getMethod("clear");
            return true;
        }
        catch (ClassNotFoundException | NoSuchMethodException ex) {
            return false;
        }
    }

    private static Object effectsOf(Player player) throws ReflectiveOperationException {
        return PLAYER_POST_EFFECTS.invoke(player);
    }

    private static Key parseKey(String input) {
        try {
            return Key.key(input.toLowerCase());
        }
        catch (RuntimeException ex) {
            return null;
        }
    }

    public static void register() {
        if (!isSupported()) {
            return;
        }

        // <--[tag]
        // @attribute <PlayerTag.post_effects>
        // @returns ListTag
        // @mechanism PlayerTag.post_effects
        // @group paper
        // @Plugin Paper
        // @description
        // Returns the list of post effect IDs (like "namespace:effect") currently applied to the player, in application order.
        // Post effects are applied by the client on top of the rendered frame, and are defined by resource packs under "assets/<namespace>/post_effect/<name>.json".
        // Available only on Paper for MC 26.3 and above.
        // -->
        PlayerTag.registerOnlineOnlyTag(ListTag.class, "post_effects", (attribute, object) -> {
            try {
                List<?> keys = (List<?>) VALUES.invoke(effectsOf(object.getPlayerEntity()));
                ListTag result = new ListTag();
                for (Object key : keys) {
                    result.add(((Key) key).asString());
                }
                return result;
            }
            catch (ReflectiveOperationException ex) {
                Debug.echoError(ex);
                return null;
            }
        });

        // <--[mechanism]
        // @object PlayerTag
        // @name post_effects
        // @input ListTag
        // @Plugin Paper
        // @group paper
        // @description
        // Sets the full ordered list of post effects applied to the player, replacing any existing ones, in a single update.
        // Effects are applied in list order, each one processing the result of the previous.
        // Provide no input (or an empty list) to remove all post effects.
        // Available only on Paper for MC 26.3 and above.
        // @tags
        // <PlayerTag.post_effects>
        // -->
        PlayerTag.registerOnlineOnlyMechanism("post_effects", (object, mechanism) -> {
            List<Key> keys = new ArrayList<>();
            if (mechanism.hasValue()) {
                for (String entry : mechanism.valueAsType(ListTag.class)) {
                    Key key = parseKey(entry);
                    if (key == null) {
                        mechanism.echoError("Invalid post effect ID '" + entry + "': must be a valid namespaced key.");
                        return;
                    }
                    keys.add(key);
                }
            }
            try {
                Object effects = effectsOf(object.getPlayerEntity());
                if (keys.isEmpty()) {
                    CLEAR.invoke(effects);
                }
                else {
                    SET.invoke(effects, keys);
                }
            }
            catch (ReflectiveOperationException ex) {
                mechanism.echoError("Failed to update post effects: " + (ex instanceof InvocationTargetException ? ex.getCause() : ex));
            }
        });

        // <--[mechanism]
        // @object PlayerTag
        // @name add_post_effect
        // @input ElementTag
        // @Plugin Paper
        // @group paper
        // @description
        // Adds a post effect (like "namespace:effect") to the end of the player's post effect list, if it is not already present.
        // Available only on Paper for MC 26.3 and above.
        // @tags
        // <PlayerTag.post_effects>
        // -->
        PlayerTag.registerOnlineOnlyMechanism("add_post_effect", ElementTag.class, (object, mechanism, input) -> {
            Key key = parseKey(input.asString());
            if (key == null) {
                mechanism.echoError("Invalid post effect ID '" + input + "': must be a valid namespaced key.");
                return;
            }
            try {
                ADD.invoke(effectsOf(object.getPlayerEntity()), key);
            }
            catch (ReflectiveOperationException ex) {
                mechanism.echoError("Failed to update post effects: " + (ex instanceof InvocationTargetException ? ex.getCause() : ex));
            }
        });

        // <--[mechanism]
        // @object PlayerTag
        // @name remove_post_effect
        // @input ElementTag
        // @Plugin Paper
        // @group paper
        // @description
        // Removes a post effect (like "namespace:effect") from the player, if present.
        // Available only on Paper for MC 26.3 and above.
        // @tags
        // <PlayerTag.post_effects>
        // -->
        PlayerTag.registerOnlineOnlyMechanism("remove_post_effect", ElementTag.class, (object, mechanism, input) -> {
            Key key = parseKey(input.asString());
            if (key == null) {
                mechanism.echoError("Invalid post effect ID '" + input + "': must be a valid namespaced key.");
                return;
            }
            try {
                REMOVE.invoke(effectsOf(object.getPlayerEntity()), key);
            }
            catch (ReflectiveOperationException ex) {
                mechanism.echoError("Failed to update post effects: " + (ex instanceof InvocationTargetException ? ex.getCause() : ex));
            }
        });

        // <--[mechanism]
        // @object PlayerTag
        // @name clear_post_effects
        // @input None
        // @Plugin Paper
        // @group paper
        // @description
        // Removes all post effects from the player.
        // Available only on Paper for MC 26.3 and above.
        // @tags
        // <PlayerTag.post_effects>
        // -->
        PlayerTag.registerOnlineOnlyMechanism("clear_post_effects", (object, mechanism) -> {
            try {
                CLEAR.invoke(effectsOf(object.getPlayerEntity()));
            }
            catch (ReflectiveOperationException ex) {
                mechanism.echoError("Failed to update post effects: " + (ex instanceof InvocationTargetException ? ex.getCause() : ex));
            }
        });
    }
}
