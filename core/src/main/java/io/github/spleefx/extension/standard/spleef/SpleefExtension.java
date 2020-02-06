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
package io.github.spleefx.extension.standard.spleef;

import com.google.gson.annotations.Expose;
import io.github.spleefx.extension.GameExtension;
import io.github.spleefx.util.Percentage;

public class SpleefExtension extends GameExtension {

    @Expose
    private SnowballSettings snowballSettings = new SnowballSettings();

    public SnowballSettings getSnowballSettings() {
        return snowballSettings;
    }

    public static class SnowballSettings {

        @Expose
        private boolean removeSnowballsGraduallyOnMelting = true;

        @Expose
        private Percentage removalChance = new Percentage(50);

        @Expose
        private int removedAmount = 1;

        @Expose
        private boolean allowThrowing = true;

        public boolean removeSnowballsGraduallyOnMelting() {
            return removeSnowballsGraduallyOnMelting;
        }

        public Percentage getRemovalChance() {
            return removalChance;
        }

        public boolean getAllowThrowing() {
            return allowThrowing;
        }

        public int getRemovedAmount() {
            return removedAmount;
        }
    }

}