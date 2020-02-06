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
package io.github.spleefx.sign;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

/**
 * Represents a location of a block
 */
public class BlockLocation {

    private String world;

    private int x, y, z;

    public BlockLocation(String world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockLocation at(Location location) {
        return new BlockLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public String getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public World world() {
        return Bukkit.getWorld(world);
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockLocation that = (BlockLocation) o;
        return that.x == x &&
                that.y == y &&
                that.z == z &&
                world.equals(that.world);
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s in world %s", x, y, z, world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }
}
