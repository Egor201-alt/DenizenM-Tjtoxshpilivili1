package com.denizenscript.denizen.paper.datacomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;

public class CreativeSlotLockAdapter extends DataComponentAdapter.NonValued {

    // <--[property]
    // @object ItemTag
    // @name creative_slot_lock
    // @input ElementTag(Boolean)
    // @description
    // Controls whether an item is locked in its slot while a player is in creative mode, preventing it from being moved or removed, see <@link language Item Components>.
    // @mechanism
    // Provide no input to reset the item to its default value.
    // -->

    public CreativeSlotLockAdapter() {
        super(DataComponentTypes.CREATIVE_SLOT_LOCK, "creative_slot_lock");
    }
}
