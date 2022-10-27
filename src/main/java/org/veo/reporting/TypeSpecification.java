/*******************************************************************************
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
 ******************************************************************************/
package org.veo.reporting;

import java.util.Optional;
import java.util.Set;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class TypeSpecification {

  @JsonCreator
  public TypeSpecification(
      @JsonProperty(value = "modelType", required = true) EntityType modelType,
      @JsonProperty(value = "subTypes", required = false) Set<String> subTypes) {
    this.modelType = modelType;
    this.subTypes = Optional.ofNullable(subTypes).map(Set::copyOf).orElse(null);
  }

  EntityType modelType;

  Set<String> subTypes;

  public @NotNull(message = "Entity type not specified.") EntityType getModelType() {
    return modelType;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Set<String> getSubTypes() {
    return subTypes;
  }

  @Override
  public String toString() {
    return "TypeSpecification [modelType=" + modelType + ", subTypes=" + subTypes + "]";
  }
}
