package org.veo.reporting;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReportConfiguration {

    @JsonCreator
    public ReportConfiguration(
            @JsonProperty(value = "description", required = true) String description,
            @JsonProperty(value = "templateFile", required = true) String templateFile,
            @JsonProperty(value = "templateType", required = true) String templateType,
            @JsonProperty(value = "outputTypes", required = true) List<String> outputTypes,
            @JsonProperty(value = "multipleTargetsSupported", defaultValue = "false") boolean multipleTargetsSupported,
            @JsonProperty(value = "targetTypes", required = true) List<EntityType> targetTypes,
            @JsonProperty(value = "data", required = true) Map<String, String> data) {
        this.description = description;
        this.templateFile = templateFile;
        this.templateType = templateType;
        this.outputTypes = outputTypes;
        this.multipleTargetsSupported = multipleTargetsSupported;
        this.targetTypes = targetTypes;
        this.data = data;
    }

    private String description;

    private final String templateFile;

    private final String templateType;

    private final List<String> outputTypes;

    private final boolean multipleTargetsSupported;

    private final List<EntityType> targetTypes;

    private final Map<String, String> data;

    public String getDescription() {
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
