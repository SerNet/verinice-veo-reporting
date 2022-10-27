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
package org.veo.templating.methods;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.veo.templating.VeoReportingObjectWrapper;

import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public abstract class VeoTemplateMethod implements TemplateMethodModelEx {
  private final Map<?, ?> m;
  private final VeoReportingObjectWrapper ow;

  protected static final Logger logger = LoggerFactory.getLogger(VeoTemplateMethod.class);

  protected VeoTemplateMethod(Map<?, ?> m, VeoReportingObjectWrapper ow) {
    this.m = m;
    this.ow = ow;
  }

  protected Object getProperty(String name) {
    return m.get(name);
  }

  protected String getLabel(String key) {
    return ow.getLabel(key);
  }

  @Override
  public final Object exec(List arguments) throws TemplateModelException {
    logger.debug("execute {} with arguments {}", getClass().getName(), arguments);
    return ow.wrap(doExec(arguments));
  }

  protected abstract Object doExec(List arguments) throws TemplateModelException;

  protected String asString(Object arg) throws TemplateModelException {
    if (!(arg instanceof SimpleScalar)) {
      throw new TemplateModelException("Expecting a String argument but got " + arg.getClass());
    }
    return ((SimpleScalar) arg).getAsString();
  }

  protected Number asNumber(Object arg) throws TemplateModelException {
    if (!(arg instanceof SimpleNumber)) {
      throw new TemplateModelException("Expecting a Number argument but got " + arg.getClass());
    }
    return ((SimpleNumber) arg).getAsNumber();
  }
}
