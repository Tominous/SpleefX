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
package io.github.spleefx.vault;

import io.github.spleefx.SpleefX;
import io.github.spleefx.util.plugin.PluginSettings;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.plugins.Economy_SpleefX;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

import static org.bukkit.Bukkit.getServer;

public class VaultHandler {

    /**
     * The server economy
     */
    private Economy economy;

    /**
     * Registers the economy
     */
    public VaultHandler(SpleefX plugin) {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        if (vault != null) {
            SpleefX.logger().info("Vault found. Handling hooks...");
            Economy e = new Economy_SpleefX(plugin.getDataProvider());

            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (PluginSettings.ECO_HOOK_INTO_VAULT.get()) {
                SpleefX.logger().info("SpleefX vault hook is enabled in the config. Hooking into vault");
                Bukkit.getServicesManager().register(Economy.class, economy = e, vault, ServicePriority.Normal);
            } else if (rsp.getProvider() == null && ((boolean) PluginSettings.ECO_USE_VAULT.get())) {
                SpleefX.logger().warning("Vault hook is enabled, however no Vault-supported economy plugin is found. Defaulting to SpleefX economy.");
                economy = e;
            } else {
                economy = rsp.getProvider();
                SpleefX.logger().warning("Using \"" + economy.getName() + "\" economy system.");
            }
        }
    }

    public double getCoins(OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public void add(OfflinePlayer player, int amount) {
        economy.depositPlayer(player, amount);
    }

    public void withdraw(OfflinePlayer player, int amount) {
        economy.withdrawPlayer(player, amount);
    }

}