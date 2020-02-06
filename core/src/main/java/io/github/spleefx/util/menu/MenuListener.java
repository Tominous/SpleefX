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

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class MenuListener implements Listener {

    /**
     * Callback event method when an inventory is clicked
     *
     * @param event Event instance
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Menu menu = Menu.MENUS.get(event.getView().getTitle());
        if (menu == null) return; // There is no such menu.
        Player player = ((Player) event.getWhoClicked());
        Consumer<InventoryClickEvent> action = menu.getAction(event.getRawSlot());
        if (action == null) return;
        action.accept(event);
        event.setCancelled(menu.cancelClickEvents());
        if (menu.closeOnClick()) player.closeInventory();
    }
}
