/*
 *  Copyright (C) 2010 anti
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.ksi.jinfer.twostep.processing.automatonmergingstate.regexping.stateremoval.ordered.properties;

import cz.cuni.mff.ksi.jinfer.base.interfaces.PropertiesPanelProvider;
import cz.cuni.mff.ksi.jinfer.base.objects.AbstractPropertiesPanel;
import cz.cuni.mff.ksi.jinfer.base.objects.VirtualCategoryPanel;
import cz.cuni.mff.ksi.jinfer.base.utils.ModuleSelectionHelper;
import cz.cuni.mff.ksi.jinfer.twostep.processing.automatonmergingstate.regexping.stateremoval.ordered.StateRemovalOrderedFactory;
import cz.cuni.mff.ksi.jinfer.twostep.processing.automatonmergingstate.regexping.stateremoval.ordered.ordering.OrdererFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.openide.util.lookup.ServiceProvider;

/**
 * Properties panel provider for module selection category.
 * @author anti
 */
@ServiceProvider(service = PropertiesPanelProvider.class)
public class StateRemovalOrderedPropertiesPanelProviderImpl implements PropertiesPanelProvider {

  private static final int PANEL_PRIORITY = 400000;
  private static final String MERGING_STATE_REGEXP_AUTOMATON_SIMPLIFIER = "RegexpAutomatonSimplifier";

  @Override
  public AbstractPropertiesPanel getPanel(final Properties properties) {
    return new StateRemovalOrderedPropertiesPanel(properties);
  }

  @Override
  public String getName() {
    return StateRemovalOrderedFactory.NAME;
  }

  @Override
  public String getDisplayName() {
    return StateRemovalOrderedFactory.DISPLAY_NAME;
  }

  @Override
  public int getPriority() {
    return PANEL_PRIORITY;
  }

  @Override
  public String getParent() {
    return MERGING_STATE_REGEXP_AUTOMATON_SIMPLIFIER;
  }

  @Override
  public List<VirtualCategoryPanel> getSubCategories() {
    final List<VirtualCategoryPanel> result = new ArrayList<VirtualCategoryPanel>();
    result.add(new VirtualCategoryPanel("ordering", "State Ordering", ModuleSelectionHelper.lookupImpls(OrdererFactory.class)));

    return result;

  }
}
