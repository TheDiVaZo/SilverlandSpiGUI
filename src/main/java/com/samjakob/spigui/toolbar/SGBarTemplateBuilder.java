package com.samjakob.spigui.toolbar;

import com.samjakob.spigui.SpiGUI;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface SGBarTemplateBuilder {
    SGButton buildBarTemplateButton(int slot, int page, SGMenu menu, SpiGUI spiGUI, Player player);
}
