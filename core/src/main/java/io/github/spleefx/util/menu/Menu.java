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

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a menu.
 * <p>
 * To construct, use {@link MenuBuilder}
 */
public class Menu {

    /**
     * A map which contains all menus.
     */
    static final Map<String, Menu> MENUS = new LinkedHashMap<>();

    /**
     * The inventory name
     */
    private final String name;

    /**
     * The inventory rows
     */
    private final int rows;

    private final Map<Integer, ItemStack> items;

    /**
     * A map for all actions
     */
    private final Map<Integer, Consumer<InventoryClickEvent>> actions;

    /**
     * Whether or not to cancel the click events
     */
    private final boolean cancelClickEvents;

    /**
     * Whether or not to close the inventory when an item is clicked
     */
    private final boolean closeOnClick;

    Menu(int rows, String name, Map<Integer, ItemStack> items, Map<Integer, Consumer<InventoryClickEvent>> actions, boolean cancelClickEvents, boolean closeOnClick) {
        this.rows = rows;
        this.name = name;
        this.items = items;
        this.actions = actions;
        this.cancelClickEvents = cancelClickEvents;
        this.closeOnClick = closeOnClick;
        MENUS.put(name, this);
    }

    /**
     * Returns the inventory name
     *
     * @return The inventory name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the action
     *
     * @param slot The slot to get the action for
     * @return The action associated with this slot
     */
    Consumer<InventoryClickEvent> getAction(int slot) {
        return actions.get(slot);
    }

    /**
     * Whether or not to cancel click events done inside the inventory
     *
     * @return ^
     */
    public boolean cancelClickEvents() {
        return cancelClickEvents;
    }

    /**
     * Whether or not to close when the inventory is clicked
     *
     * @return ^
     */
    public boolean closeOnClick() {
        return closeOnClick;
    }

    /**
     * Creates a copy of this inventory. Any changes to this copy will not alter
     * the original one.
     *
     * @return The new inventory
     */
    private Inventory createInventory() {
        Inventory i = Bukkit.createInventory(null, rows * 9, name);
        items.forEach(i::setItem);
        return i;
    }

    /**
     * Displays this menu for the specified {@link HumanEntity}
     *
     * @param entity Entity to display for
     * @return This menu instance
     */
    public Inventory display(HumanEntity entity) {
        Inventory i = createInventory();
        entity.openInventory(createInventory());
        return i;
    }

}
