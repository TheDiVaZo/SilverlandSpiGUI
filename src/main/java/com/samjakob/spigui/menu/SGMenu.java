package com.samjakob.spigui.menu;

import com.samjakob.spigui.SpiGUI;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.toolbar.*;
import com.samjakob.spigui.util.SlotUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Consumer;

/**
 * SGMenu is used to implement the library's GUIs.
 * <br><br>
 * This is a Minecraft 'inventory' that contains items which can have
 * programmable actions performed when they are clicked. Additionally,
 * it automatically adds 'pagination' items if the menu overflows.
 * <br><br>
 * You do not instantiate this class when you need it - as you would
 * have done with the older version of the library - rather you make a
 * call to {@link SpiGUI#create(String, int)} (or {@link SpiGUI#create(String, int, String, boolean)})
 * from your plugin's {@link SpiGUI} instance.
 * <br><br>
 * This creates an inventory that is already associated with your plugin.
 * The reason for this is explained in the {@link SpiGUI#SpiGUI(JavaPlugin)}
 * class constructor implementation notes.
 */
public class SGMenu implements InventoryHolder {

    private final HashMap<UUID, SGMenu> currentMenus = new HashMap<>();

    /**
     * The plugin (owner of the SpiGUI instance) that created this inventory.
     */
    private final JavaPlugin owner;
    /**
     * The SpiGUI instance that created this inventory.
     */
    private final SpiGUI spiGUI;

    /**
     * The title of the inventory.
     */
    private String name;
    /**
     * A tag that may be used to identify the type of inventory.
     */
    private String tag;
    /**
     * The number of rows to display per page.
     */
    private int rowsPerPage;

    private int startRow = 0;

    private int maxPage;

    private boolean isAutomaticMaxPage;

    /**
     * The map of items in the inventory.
     */
    private final Map<Integer, SGButton> items;

    private final Map<Integer, SGButton> viewItems;
    /**
     * The set of sticky slots (that should remain when the page is changed).
     */
    private final HashSet<Integer> stickiedSlots;

    /**
     * The currently selected page of the inventory.
     */
    private int currentPage;
    /**
     * Whether the "default" behaviors and interactions should be permitted or
     * blocked. (True prevents default behaviors such as moving items in the
     * inventory, false allows them).
     */
    private boolean blockDefaultInteractions;
    /**
     * Whether the pagination functionality should be enabled. (True adds
     * pagination buttons when they're needed, false does not).
     */
    private boolean enableAutomaticPagination;

    /**
     * The toolbar builder used to render this GUI's toolbar.
     */
    private SGToolbarBuilder toolbarBuilder;

//    private SGBarTemplate barTemplate;
    private List<SGBarTemplate> barTemplateList = new ArrayList<>();
    /**
     * The action to be performed on close.
     */
    private Consumer<SGMenu> onClose;
    /**
     * The action to be performed on page change.
     */
    private Consumer<SGMenu> onPageChange;

    /**
     * <b>Intended for internal use only. Use {@link SpiGUI#create(String, int)} or {@link SpiGUI#create(String, int, String, boolean)}!</b><br>
     * Used by the library internally to construct an SGMenu.
     * <br>
     * The name parameter is color code translated.
     *
     * @param owner       The plugin the inventory should be associated with.
     * @param spiGUI      The SpiGUI that created this inventory.
     * @param name        The display name of the inventory.
     * @param rowsPerPage The number of rows per page.
     * @param tag         The inventory's tag.
     * @see SpiGUI#create(String, int)
     * @see SpiGUI#create(String, int, String, boolean)
     */
    public SGMenu(JavaPlugin owner, SpiGUI spiGUI, String name, int rowsPerPage, String tag, boolean isAutomaticMaxPage) {
        this.owner = owner;
        this.spiGUI = spiGUI;
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.rowsPerPage = rowsPerPage;
        this.tag = tag;

        this.items = new HashMap<>();
        this.viewItems = Collections.unmodifiableMap(items);
        this.stickiedSlots = new HashSet<>();

        this.currentPage = 0;

        this.isAutomaticMaxPage = isAutomaticMaxPage;
    }

    public Map<Integer, SGButton> getViewItems() {
        return viewItems;
    }

    /// INVENTORY SETTINGS ///

    /**
     * This is a per-inventory version of {@link SpiGUI#setBlockDefaultInteractions(boolean)}.
     *
     * @param blockDefaultInteractions Whether the default behavior of click events should be cancelled.
     * @see SpiGUI#setBlockDefaultInteractions(boolean)
     */
    public void setBlockDefaultInteractions(boolean blockDefaultInteractions) {
        this.blockDefaultInteractions = blockDefaultInteractions;
    }

    /**
     * This is a per-inventory version of {@link SpiGUI#areDefaultInteractionsBlocked()}.
     *
     * @return Whether the default behavior of click events should be cancelled.
     * @see SpiGUI#areDefaultInteractionsBlocked()
     */
    public Boolean areDefaultInteractionsBlocked() {
        return blockDefaultInteractions;
    }

    /**
     * This is a per-inventory version of {@link SpiGUI#setEnableAutomaticPagination(boolean)}.
     * If this value is set, it overrides the per-plugin option set in {@link SpiGUI}.
     *
     * @param enableAutomaticPagination Whether pagination buttons should be automatically added.
     * @see SpiGUI#setEnableAutomaticPagination(boolean)
     */
    public void setAutomaticPaginationEnabled(boolean enableAutomaticPagination) {
        this.enableAutomaticPagination = enableAutomaticPagination;
    }

    /**
     * This is a per-inventory version of {@link SpiGUI#isAutomaticPaginationEnabled()}.
     *
     * @return Whether pagination buttons should be automatically added.
     * @see SpiGUI#isAutomaticPaginationEnabled()
     */
    public Boolean isAutomaticPaginationEnabled() {
        return enableAutomaticPagination;
    }

    public boolean isAutomaticMaxPage() {
        return isAutomaticMaxPage;
    }

    public void setAutomaticMaxPage(boolean automaticMaxPage) {
        isAutomaticMaxPage = automaticMaxPage;
    }

    /**
     * This is a per-inventory version of ({@link SpiGUI#setDefaultToolbarBuilder(SGToolbarBuilder)}).
     *
     * @param toolbarBuilder The default toolbar builder used for GUIs.
     * @see SpiGUI#setDefaultToolbarBuilder(SGToolbarBuilder)
     */
    public void setToolbarBuilder(SGToolbarBuilder toolbarBuilder) {
        this.toolbarBuilder = toolbarBuilder;
    }

//    public void setBarTemplate(SGBarTemplate barTemplate) {
//        this.barTemplate = barTemplate;
////        setStartRow(getStartRow() + 1);
//        setStartRow(1);
//    }

    public void setBarTemplateList(List<SGBarTemplate> barTemplateList) {
        this.barTemplateList = barTemplateList;
//        setStartRow(getStartRow() + 1);
        setStartRow(barTemplateList.size());
    }

//    public SGBarTemplateBuilder getBarTemplateBuilder() {
//        return barTemplate.getBuilder();
//    }

//    public SGBarTemplate getBarTemplate() {
//        return barTemplate;
//    }

    public List<SGBarTemplate> getBarTemplateList() {
        return barTemplateList;
    }

    /**
     * This is a per-inventory version of ({@link SpiGUI#getDefaultToolbarBuilder()}).
     *
     * @return The default toolbar builder used for GUIs.
     * @see SpiGUI#getDefaultToolbarBuilder()
     */
    public SGToolbarBuilder getToolbarBuilder() {
        return this.toolbarBuilder;
    }

    /// INVENTORY OWNER ///

    /**
     * Returns the plugin that the inventory is associated with.
     * As this field is final, this would be the plugin that created
     * the inventory.
     *
     * @return The plugin the inventory is associated with.
     */
    public JavaPlugin getOwner() {
        return owner;
    }

    /// INVENTORY SIZE ///

    /**
     * Returns the number of rows (of 9 columns) per page of the inventory.
     * If you want the total number of slots on a page, you should use {@link #getPageSize()}
     * instead.
     *
     * @return The number of rows per page.
     */
    public int getRowsPerPage() {
        return rowsPerPage;
    }

    /**
     * Returns the number of slots per page of the inventory. This would be
     * associated with the Bukkit/Spigot APIs inventory 'size' parameter.
     * <br>
     * So for example if {@link #getRowsPerPage()} was 3, this would be 27,
     * as Minecraft Chest inventories have rows of 9 columns.
     *
     * @return The number of inventory slots per page.
     */
    public int getPageSize() {
        return rowsPerPage * 9;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    /**
     * Sets the number of rows per page of the inventory.
     * <br>
     * There is no way to set the number of slots per page directly, so if
     * you need to do that, you'll need to divide the number of slots by 9
     * and supply the result to this parameter to achieve that.
     *
     * @param rowsPerPage The number of rows per page.
     */
    public void setRowsPerPage(int rowsPerPage) {
        this.rowsPerPage = rowsPerPage;
    }

    /// INVENTORY TAG ///

    /**
     * This returns the GUI's tag.
     * <br><br>
     * The tag is used when getting all open inventories ({@link SpiGUI#findOpenWithTag(String)}) with your chosen tag.
     * An example of where this might be useful is with a permission GUI - when
     * the permissions are updated by one user in the GUI, it would be desirable to
     * refresh the state of the permissions GUI for all users observing the GUI.
     *
     * @return The GUI's tag.
     */
    public String getTag() {
        return tag;
    }

    /**
     * This sets the GUI's tag.
     *
     * @param tag The GUI's tag.
     * @see #getTag()
     * @see SpiGUI#findOpenWithTag(String)
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /// INVENTORY NAME ///

    /**
     * This sets the inventory's display name.
     * <br><br>
     * The name parameter is color code translated before the value is set.
     * If you want to avoid this behavior, you should use {@link #setRawName(String)}
     * which sets the inventory's name directly.
     *
     * @param name The display name to set. (and to be color code translated)
     */
    public void setName(String name) {
        this.name = ChatColor.translateAlternateColorCodes('&', name);
    }

    /**
     * This sets the inventory's display name <b>without</b> first translating
     * color codes.
     *
     * @param name The display name to set.
     */
    public void setRawName(String name) {
        this.name = name;
    }

    /**
     * This returns the inventory's display name.
     * <br><br>
     * Note that if you used {@link #setName(String)}, this will have been
     * color code translated already.
     *
     * @return The inventory's display name.
     */
    public String getName() {
        return name;
    }

    /// BUTTONS ///

    /**
     * Adds the provided {@link SGButton}.
     *
     * @param button The button to add.
     */
    public void addButton(SGButton button) {
        // If slot 0 is empty, but it's the 'highest filled slot', then set slot 0 to contain button.
        // (This is an edge case for when the whole inventory is empty).
        if (getHighestFilledSlot() == 0 && getButton(0) == null) {
            setButton(0, button);
            return;
        }

        // Otherwise, add one to the highest filled slot, then use that slot for the new button.
        setButton(getHighestFilledSlot() + 1, button);
    }

    /**
     * Adds the specified {@link SGButton}s consecutively.
     *
     * @param buttons The buttons to add.
     */
    public void addButtons(SGButton... buttons) {
        for (SGButton button : buttons) addButton(button);
    }

    /**
     * Adds the provided {@link SGButton} at the position denoted by the
     * supplied slot parameter.
     * <br>
     * If you specify a value larger than the value of the first page,
     * pagination will be automatically applied when the inventory is
     * rendered. An alternative to this is to use {@link #setButton(int, int, SGButton)}.
     *
     * @param slot   The desired location of the button.
     * @param button The button to add.
     * @see #setButton(int, int, SGButton)
     */
    public void setButton(int slot, SGButton button) {
        items.put(slot, button);
    }

    /**
     * Adds the provided {@link SGButton} at the position denoted by the
     * supplied slot parameter <i>on the page denoted by the supplied page parameter</i>.
     * <br>
     * This is an alias for {@link #setButton(int, SGButton)}, however one where the slot
     * value is mapped to the specified page. So if page is 2 (the third page) and the
     * inventory row count was 3 (so a size of 27), a supplied slot value of 3 would actually map to
     * a slot value of (2 * 27) + 3 = 54. The mathematical formula for this is <code>(page * pageSize) + slot</code>.
     * <br>
     * If the slot value is out of the bounds of the specified page, this function will do nothing.
     *
     * @param page   The page to which the button should be added.
     * @param slot   The position on that page the button should be added at.
     * @param button The button to add.
     * @see #setButton(int, SGButton)
     */
    public void setButton(int page, int slot, SGButton button) {
        if (slot < 0 || slot > getPageSize())
            return;

        setButton((page * getPageSize()) + slot, button);
    }

    /**
     * Removes a button from the specified slot.
     *
     * @param slot The slot containing the button you wish to remove.
     */
    public void removeButton(int slot) {
        items.remove(slot);
    }

    /**
     * An alias for {@link #removeButton(int)} to remove a button from the specified
     * slot on the specified page.
     * <br>
     * If the slot value is out of the bounds of the specified page, this function will do nothing.
     *
     * @param page The page containing the button you wish to remove.
     * @param slot The slot, of that page, containing the button you wish to remove.
     */
    public void removeButton(int page, int slot) {
        if (slot < 0 || slot > getPageSize())
            return;

        removeButton((page * getPageSize()) + slot);
    }

    /**
     * Returns the {@link SGButton} in the specified slot.
     * <br>
     * If you attempt to get a slot less than 0 or greater than the slot containing
     * the button at the greatest slot value, this will return null.
     *
     * @param slot The slot containing the button you wish to get.
     * @return The {@link SGButton} that was in that slot or null if the slot was invalid or if there was no button that slot.
     */
    public SGButton getButton(int slot) {
        return SlotUtil.getButton(viewItems, slot, getHighestFilledSlot());
    }

    /**
     * This is an alias for {@link #getButton(int)} that allows you to get a button
     * contained by a slot on a given page.
     *
     * @param page The page containing the button.
     * @param slot The slot, on that page, containing the button.
     * @return The {@link SGButton} that was in that slot or null if the slot was invalid or if there was no button that slot.
     */
    public SGButton getButton(int page, int slot) {
        return SlotUtil.getButton(viewItems, page, slot, getPageSize());
    }

    /// PAGINATION ///

    /**
     * Returns the current page of the inventory.
     * This is the page that will be displayed when the inventory is opened and
     * displayed to a player (i.e. rendered).
     *
     * @return The current page of the inventory.
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Sets the page of the inventory that will be displayed when the inventory is
     * opened and displayed to a player (i.e. rendered).
     *
     * @param page The new current page of the inventory.
     */
    public void setCurrentPage(int page) {
        this.currentPage = page;
        if (this.onPageChange != null) this.onPageChange.accept(this);
    }

    /**
     * Gets the page number of the final page of the GUI.
     *
     * @return The highest page number that can be viewed.
     */
    public int getMaxPage() {
        if (isAutomaticMaxPage) {
            return (int) Math.ceil(((double) getHighestFilledSlot() + 1) / ((double) getPageSize()));
        }
        return maxPage;
    }

    public void setMaxPage(int maxPage) {
        this.maxPage = maxPage;
    }

    /**
     * Returns the slot number of the highest filled slot.
     * This is mainly used to calculate the number of pages there needs to be to
     * display the GUI's contents in the rendered inventory.
     *
     * @return The highest filled slot's number.
     */
    public int getHighestFilledSlot() {
        int slot = 0;

        for (int nextSlot : items.keySet()) {
            if (items.get(nextSlot) != null && nextSlot > slot)
                slot = nextSlot;
        }

        return slot;
    }

    /**
     * Increments the current page.
     * This will automatically refresh the inventory by calling {@link #refreshInventory(HumanEntity)} if
     * the page was changed.
     *
     * @param viewer The {@link HumanEntity} viewing the inventory.
     * @return Whether the page could be changed (false means the max page is currently open).
     */
    public boolean nextPage(HumanEntity viewer) {
        if (currentPage < getMaxPage() - 1) {
            currentPage++;
            refreshInventory(viewer);
            if (this.onPageChange != null) this.onPageChange.accept(this);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Decrements the current page.
     * This will automatically refresh the inventory by calling {@link #refreshInventory(HumanEntity)} if
     * the page was changed.
     *
     * @param viewer The {@link HumanEntity} viewing the inventory.
     * @return Whether the page could be changed (false means the first page is currently open).
     */
    public boolean previousPage(HumanEntity viewer) {
        if (currentPage > 0) {
            currentPage--;
            refreshInventory(viewer);
            if (this.onPageChange != null) this.onPageChange.accept(this);
            return true;
        } else {
            return false;
        }
    }


    /// STICKY SLOTS ///

    /**
     * Marks a slot as 'sticky', so that when the page is changed,
     * the slot will always display the value on the first page.
     * <br>
     * This is useful for implementing things like 'toolbars', where
     * you have a set of common items on every page.
     * <br>
     * If the slot is out of the bounds of the first page (i.e. less
     * than 0 or greater than {@link #getPageSize()} - 1) this method
     * will do nothing.
     *
     * @param slot The slot to mark as 'sticky'.
     */
    public void stickSlot(int slot) {
        if (slot < 0 || slot >= getPageSize())
            return;

        this.stickiedSlots.add(slot);
    }

    /**
     * Un-marks a slot as sticky - thereby meaning that slot will display
     * whatever its value on the current page is.
     *
     * @param slot The slot to un-mark as 'sticky'.
     * @see #stickSlot(int)
     */
    public void unstickSlot(int slot) {
        this.stickiedSlots.remove(slot);
    }

    /**
     * This clears all the 'stuck' slots - essentially un-marking all
     * stuck slots.
     *
     * @see #stickSlot(int)
     */
    public void clearStickiedSlots() {
        this.stickiedSlots.clear();
    }

    /**
     * This checks whether a given slot is sticky.
     * If the slot is out of bounds of the first page (as defined by
     * the same parameters as {@link #stickSlot(int)}), this will return
     * false.
     *
     * @param slot The slot to check.
     * @return True if the slot is sticky, false if it isn't or the slot was out of bounds.
     * @see #stickSlot(int)
     */
    public boolean isStickiedSlot(int slot) {
        if (slot < 0 || slot >= getPageSize())
            return false;

        return this.stickiedSlots.contains(slot);
    }

    /**
     * This clears all slots in the inventory, except those which
     * have been marked as 'sticky'.
     *
     * @see #stickSlot(int)
     */
    public void clearAllButStickiedSlots() {
        this.currentPage = 0;
        items.entrySet().removeIf(item -> !isStickiedSlot(item.getKey()));
    }

    /// EVENTS ///

    /**
     * @return The action to be performed on close.
     * @see #setOnClose(Consumer)
     */
    public Consumer<SGMenu> getOnClose() {
        return this.onClose;
    }

    /**
     * Used to set an action to be performed on inventory close without
     * registering an {@link org.bukkit.event.inventory.InventoryCloseEvent} specifically
     * for this inventory.
     *
     * @param onClose The action to be performed on close.
     */
    public void setOnClose(Consumer<SGMenu> onClose) {
        this.onClose = onClose;
    }

    /**
     * @return The action to be performed on page change.
     * @see #setOnPageChange(Consumer)
     */
    public Consumer<SGMenu> getOnPageChange() {
        return this.onPageChange;
    }

    /**
     * Used to set an action to be performed on inventory page change.
     *
     * @param onPageChange The action to be performed on page change.
     */
    public void setOnPageChange(Consumer<SGMenu> onPageChange) {
        this.onPageChange = onPageChange;
    }

    /// INVENTORY API ///

    /**
     * Refresh an inventory that is currently open for a given viewer.
     * <br>
     * This method checks if the specified viewer is looking at an
     * {@link SGMenu} and, if they are, it refreshes the inventory for them.
     *
     * @param viewer The viewer of the open inventory.
     */
    public void refreshInventory(HumanEntity viewer) {
        // If the open inventory isn't an SGMenu - or if it isn't this inventory, do nothing.
        if (
                !(viewer.getOpenInventory().getTopInventory().getHolder() instanceof SGMenu)
                        || viewer.getOpenInventory().getTopInventory().getHolder() != this
        ) return;

        // If the new size is different, we'll need to open a new inventory.
        if (viewer.getOpenInventory().getTopInventory().getSize() != getPageSize() + (getMaxPage() > 0 ? 9 : 0)) {
            viewer.openInventory(getInventoryForPlayer((Player) viewer));
            return;
        }

        // If the name has changed, we'll need to open a new inventory.
        String newName = name.replace("{currentPage}", String.valueOf(currentPage + 1))
                .replace("{maxPage}", String.valueOf(getMaxPage()));
        if (!viewer.getOpenInventory().getTitle().equals(newName)) {
            viewer.openInventory(getInventoryForPlayer((Player) viewer));
            return;
        }

        // Otherwise, we can refresh the contents without re-opening the inventory.
        viewer.getOpenInventory().getTopInventory().setContents(getInventoryForPlayer((Player) viewer).getContents());
    }

    /**
     * Returns the Bukkit/Spigot {@link Inventory} that represents the GUI.
     * This is shown to a player using {@link HumanEntity#openInventory(Inventory)}.
     *
     * @return The created inventory used to display the GUI.
     */
    @Override
    public Inventory getInventory() {
        Inventory inventoryForPlayer = getInventoryForPlayer(null);
        return inventoryForPlayer;
    }

    public Inventory getInventoryForPlayer(Player player) {
        boolean isAutomaticPaginationEnabled = spiGUI.isAutomaticPaginationEnabled();
//        SGBarTemplate barTemplate = this.getBarTemplate();
        List<SGBarTemplate> barTemplateRowsList = this.getBarTemplateList();
//        boolean isTemplateBar = Objects.nonNull(barTemplate);
        boolean isTemplateBar = !barTemplateRowsList.isEmpty();
        if (isAutomaticPaginationEnabled() != null) {
            isAutomaticPaginationEnabled = isAutomaticPaginationEnabled();
        }

        boolean needsPagination = getMaxPage() > 0 && isAutomaticPaginationEnabled;

        Inventory inventory = Bukkit.createInventory(this, prepareLines(needsPagination, barTemplateRowsList),
                name.replace("{currentPage}", String.valueOf(currentPage + 1))
                        .replace("{maxPage}", String.valueOf(getMaxPage()))
        );


        if (isTemplateBar) {
            barTemplateRowsList.forEach(bar -> {
                int pageSize = getPageSize();
                if (SGBarTemplateDirectionType.DOWN.equals(bar.getDirectionType())) {
                    for (int i = pageSize; i < pageSize + 9; i++) {
                        int offset = i - pageSize;
                        setTemplateItems(bar, offset, inventory, i, player);
                    }
                }
                if (SGBarTemplateDirectionType.UP.equals(bar.getDirectionType())) {
                    for (int i = 0; i < 9; i++) {
                        setTemplateItems(bar, i, inventory, i, player);
                    }
                }
            });
        }

        // Add the main inventory items.
        for (int key = (currentPage * getPageSize()) - (getStartRow() * 9); key < (currentPage + 1) * getPageSize(); key++) {
            // If we've already reached the maximum assigned slot, stop assigning
            // slots.
            if (key > getHighestFilledSlot()) break;

            if (items.containsKey(key)) {
                int position;
                if (isTemplateBar) {
                    position = key - (currentPage * getPageSize()) + 9;
                } else {
                    position = key - (currentPage * getPageSize());
                }
                inventory.setItem(position, items.get(key).getIcon());
            }
        }

        // Update the stickied slots.
        for (int stickiedSlot : stickiedSlots) {
            inventory.setItem(stickiedSlot, items.get(stickiedSlot).getIcon());
        }

        // Render the pagination items.
        if (needsPagination) {
            SGToolbarBuilder toolbarButtonBuilder = spiGUI.getDefaultToolbarBuilder();
            if (getToolbarBuilder() != null) {
                toolbarButtonBuilder = getToolbarBuilder();
            }

            int pageSize = getPageSize();
            if (isTemplateBar) {
                pageSize += 9;
            }

            for (int i = pageSize; i < pageSize + 9; i++) {
                int offset = i - pageSize;

                SGButton paginationButton = toolbarButtonBuilder.buildToolbarButton(
                        offset, getCurrentPage(), SGToolbarButtonType.getDefaultForSlot(offset), this
                );
                inventory.setItem(i, paginationButton != null ? paginationButton.getIcon() : null);
            }
        }

        if (player != null) {
            currentMenus.put(player.getUniqueId(), this);
        }

        return inventory;
    }

    private void setTemplateItems(SGBarTemplateBuilder barTemplateBuilder, int offset, Inventory inventory, int i, Player player) {
        SGButton button = barTemplateBuilder.buildBarTemplateButton(offset, getCurrentPage(), this, spiGUI, player);
        inventory.setItem(i, button != null ? button.getIcon() : null);
    }

    private int prepareLines(boolean needPagination, List<SGBarTemplate> needBarTemplate) {
        int lines = getPageSize();
        if (needPagination) {
            lines += 9;
        }
        if (!needBarTemplate.isEmpty()) {
            lines += 9 * needBarTemplate.size();
        }
        return lines;

    }

}
