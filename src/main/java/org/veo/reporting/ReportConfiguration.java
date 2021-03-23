/**
 * Copyright (c) 2021 Jochen Kemnade.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.veo.reporting;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The specification of a single report. This is used to define the basic
 * properties such as name and description as well as specify the URLS that are
 * queried to supply the dynamic content. <br>
 * The entities are read from JSON serializations that reside in the
 * <code>/reports</code> directory on the classpath.
 */

public class ReportConfiguration {

    @JsonCreator
    public ReportConfiguration(
            @JsonProperty(value = "name", required = true) Map<String, String> name,
            @JsonProperty(value = "description", required = true) Map<String, String> description,
            @JsonProperty(value = "templateFile", required = true) String templateFile,
            @JsonProperty(value = "templateType", required = true) String templateType,
            @JsonProperty(value = "outputTypes", required = true) List<String> outputTypes,
            @JsonProperty(value = "multipleTargetsSupported", defaultValue = "false") boolean multipleTargetsSupported,
            @JsonProperty(value = "targetTypes", required = true) List<EntityType> targetTypes,
            @JsonProperty(value = "data", required = true) Map<String, String> data) {
        this.name = name;
        this.description = description;
        this.templateFile = templateFile;
        this.templateType = templateType;
        this.outputTypes = outputTypes;
        this.multipleTargetsSupported = multipleTargetsSupported;
        this.targetTypes = targetTypes;
        this.data = data;
    }

    private final Map<String, String> name;

    private Map<String, String> description;

    private final String templateFile;

    private final String templateType;

    private final List<String> outputTypes;

    private final boolean multipleTargetsSupported;

    private final List<EntityType> targetTypes;

    private final Map<String, String> data;

    public Map<String, String> getName() {
        return name;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    @JsonIgnore
    public String getTemplateFile() {
        return templateFile;
    }

    @JsonIgnore
    public String getTemplateType() {
        return templateType;
    }

    public List<String> getOutputTypes() {
        return outputTypes;
    }

    public List<EntityType> getTargetTypes() {
        return targetTypes;
    }

    @JsonIgnore
    public Map<String, String> getData() {
        return data;
    }

    public boolean isMultipleTargetsSupported() {
        return multipleTargetsSupported;
    }

}
