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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.veo.fileconverter.FileConverter;
import org.veo.reporting.controllers.ReportController;

/**
 * The DTO for a report creation. It specifies the entities that the report is
 * run upon and the desired output format.
 * 
 * @see {@link FileConverter}
 * @see {@link ReportEngine}
 * @see {@link ReportController}
 */
public class CreateReport {

    @NotEmpty(message = "Output type not specified.")
    private String outputType;

    @NotNull(message = "Targets not specified.")
    @Size(min = 1, max = 1)
    // multiple targets are not supported yet
    private List<TargetSpecification> targets;

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
