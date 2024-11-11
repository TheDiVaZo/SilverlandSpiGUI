package com.samjakob.spigui.util;

import com.samjakob.spigui.buttons.SGButton;

import java.util.Map;

/**
 * @author TheDiVaZo
 * created on 10.11.2024
 **/
public final class SlotUtil {
    private SlotUtil() {
    }

    public static SGButton getButton(Map<Integer, SGButton> buttons, int page, int slot, int pageSize) {
        if (slot < 0 || slot > pageSize)
            return null;

        return buttons.get((page * pageSize) + slot);
    }

    public static SGButton getButton(Map<Integer, SGButton> buttons, int slot, int highestFilledSlot) {
        if (slot < 0 || slot > highestFilledSlot)
            return null;

        return buttons.get(slot);
    }
}
