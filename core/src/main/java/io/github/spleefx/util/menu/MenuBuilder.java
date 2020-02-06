/*
 * * Copyright 2020 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.spleefx.util.menu;

import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A builder-styled class for {@link Menu}
 *
 * @see Menu
 */
public class MenuBuilder {

    /**
     * The name of the inventory. Used for checking
     */
    private final String name;

    /**
     * The inventory rows
     */
    private final int rows;

    /**
     * A map for all items and their slots
     */
    private final Map<Integer, ItemStack> items = new HashMap<>();

    /**
     * A map for all actions
     */
    private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();

    /**
     * Whether or not to cancel click events in the inventory
     */
    private boolean cancelClickEvents = false;

    /**
     * Whether or not to close the inventory when an item has been clicked
     */
    private boolean closeOnClick = false;

    /**
     * Creates a new {@link MenuBuilder} to have a menu of the specified name and rows
     *
     * @param name Name of the inventory
     * @param rows Rows that the inventory has
     */
    public MenuBuilder(String name, int rows) {
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.rows = rows;
    }

    /**
     * Adds an item to the slot, and adds an action to it
     *
     * @param item        Item to add
     * @param slot        Slot of the item
     * @param clickAction Action of this item when it is clicked
     * @return A reference to this builder
     */
    public MenuBuilder item(ItemStack item, int slot, Consumer<InventoryClickEvent> clickAction) {
        items.put(slot, item);
        actions.put(slot, clickAction);
        return this;
    }

    /**
     * Sets the menu to cancel any click event in this inventory
     *
     * @return A reference to this builder
     */
    public MenuBuilder cancelClickEvents() {
        cancelClickEvents = true;
        return this;
    }

    /**
     * Sets the menu to close when an item is clicked
     *
     * @return A reference to this builder
     */
    public MenuBuilder closeOnClick() {
        closeOnClick = true;
        return this;
    }

    /**
     * Constructs a {@link Menu} from this builder and registers it to be listened for
     *
     * @return The constructed menu
     */
    public Menu build() {
        return new Menu(rows, name, items, actions, cancelClickEvents, closeOnClick);
    }
}
