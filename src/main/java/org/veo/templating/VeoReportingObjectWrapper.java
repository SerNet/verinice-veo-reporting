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
package org.veo.templating;

import java.util.Map;

import org.veo.templating.adapters.VeoReportingEntityAdapter;
import org.veo.templating.adapters.VeoReportingLinkAdapter;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

public class VeoReportingObjectWrapper extends DefaultObjectWrapper {

    public VeoReportingObjectWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) obj;
            if (m.containsKey("id") && m.containsKey("type")) {
                // this is probably an entity
                return new VeoReportingEntityAdapter((Map<?, ?>) obj, this);
            } else if (m.containsKey("target") && m.containsKey("attributes")) {
                // this is probably a custom link
                return new VeoReportingLinkAdapter((Map<?, ?>) obj, this);
            }
        }
        return super.wrap(obj);
    }

}