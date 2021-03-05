package org.veo.reporting;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateReport {

    @NotNull(message = "Access token not specified.")
    private String token;

    @NotNull(message = "Output type not specified.")
    private String outputType;

    @NotNull(message = "Targets not specified.")
    @Size(min = 1, max = 1)
    // multiple targets are not supported yet
    private List<TargetSpecification> targets;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public List<TargetSpecification> getTargets() {
        return targets;
    }

    public void setTargets(List<TargetSpecification> targets) {
        this.targets = targets;
    }

    public static class TargetSpecification {

        public TargetSpecification(EntityType entityType, String id) {
            this.type = entityType;
            this.id = id;
        }

        @NotNull(message = "Entity type not specified.")
        public final EntityType type;
        @NotNull(message = "ID not specified.")
        public final String id;
    }

}
