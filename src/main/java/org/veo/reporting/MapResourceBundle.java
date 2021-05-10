/**
 * verinice.veo reporting
 * Copyright (C) 2021  Jochen Kemnade
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.reporting;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * A {@link ResourceBundle} implementation that is backed by a {@link Map}.
 */
public class MapResourceBundle extends ResourceBundle {

    private final Map<String, Object> map;

    public MapResourceBundle(Map<String, Object> map) {
        this.map = new HashMap<>(map);
    }

    @Override
    protected Object handleGetObject(String key) {
        return map.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(map.keySet());
    }

    /**
     * Creates a merged bundle from a base bundle and a set of additional
     * entries. This can be used to dynamically overlay a set of translations
     * over an existing bundle.
     */
    public static MapResourceBundle createMergedBundle(ResourceBundle base,
            Map<String, Object> dynamicAdditions) {
        Set<String> baseKeys = base.keySet();

        Map<String, Object> mergedBundleContent = new HashMap<>(
                baseKeys.size() + dynamicAdditions.size());
        for (String key : base.keySet()) {
            mergedBundleContent.put(key, base.getObject(key));
        }

        for (Entry<String, ?> e : dynamicAdditions.entrySet()) {
            Object prev = mergedBundleContent.put(e.getKey(), e.getValue());
            if (prev != null) {
                throw new IllegalStateException("Conflicting entries for " + e.getKey());
            }
        }
        return new MapResourceBundle(mergedBundleContent);

    }
}
