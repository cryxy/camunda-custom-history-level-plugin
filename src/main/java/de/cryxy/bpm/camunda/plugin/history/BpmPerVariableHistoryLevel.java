/* Licensed under the Apache License, Version 2.0 (the "License");
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

package de.cryxy.bpm.camunda.plugin.history;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.HistoryLevelFull;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

public class BpmPerVariableHistoryLevel extends HistoryLevelFull {

	/**
	 * history level name
	 */
	public static final String NAME = "bpm-per-variable";

	/**
	 * camunda property name in the model
	 */
	public static final String CAMUNDA_PROP_NAME = "variablesWithoutHistory";

	/**
	 * variables separator
	 */
	public static final String CAMUNDA_PROP_VAR_SEP = ",";

	public static final BpmPerVariableHistoryLevel INSTANCE = new BpmPerVariableHistoryLevel();

	protected Map<String, HistoryLevel> historyLevels = new HashMap<String, HistoryLevel>();
	protected Map<String, HistoryLevel> delegateHistoryLevelPerProcess = new HashMap<String, HistoryLevel>();

	public static BpmPerVariableHistoryLevel getInstance() {
		return INSTANCE;
	}

	public BpmPerVariableHistoryLevel() {
		historyLevels.put(HISTORY_LEVEL_NONE.getName(), HISTORY_LEVEL_NONE);
		historyLevels.put(HISTORY_LEVEL_ACTIVITY.getName(), HISTORY_LEVEL_ACTIVITY);
		historyLevels.put(HISTORY_LEVEL_AUDIT.getName(), HISTORY_LEVEL_AUDIT);
		historyLevels.put(HISTORY_LEVEL_FULL.getName(), HISTORY_LEVEL_FULL);
	}

	public void addHistoryLevels(List<HistoryLevel> historyLevels) {
		for (HistoryLevel historyLevel : historyLevels) {
			this.historyLevels.put(historyLevel.getName(), historyLevel);
		}
	}

	@Override
	public int getId() {
		return 12;
	}

	@Override
	public String getName() {
		return "bpm-per-variable";
	}

	@Override
	public boolean isHistoryEventProduced(HistoryEventType historyEventType, Object entity) {
		// If the entity is null the engine tests if the history level in general
		// handles such history events.
		if (entity == null) {
			return super.isHistoryEventProduced(historyEventType, entity);
		}

		// We are only interested in variable-instances
		if (historyEventType.getEntityType().equals(HistoryEventTypes.VARIABLE_INSTANCE_CREATE.getEntityType())) {
			return isBpmPerVariableHistoryLevelEventProduced(historyEventType, entity);
		}

		// delegate it to the super class
		return super.isHistoryEventProduced(historyEventType, entity);

	}

	protected boolean isBpmPerVariableHistoryLevelEventProduced(HistoryEventType historyEventType, Object entity) {
		if (entity instanceof VariableInstance) {
			VariableInstance variableInstance = ((VariableInstance) entity);

			String processInstanceId = variableInstance.getProcessInstanceId();
			String variableName = variableInstance.getName();

			Set<String> variableNamesWithoutHistory = getVariableNamesWithoutHistory(processInstanceId);

			return !variableNamesWithoutHistory.contains(variableName);
		}

		return false;

	}

	/**
	 * Extract the variables names from the camunda property.
	 */
	protected Set<String> getVariableNamesWithoutHistory(String processInstanceId) {

		Set<String> variablesNames = new HashSet<String>();

		// Load execution
		DbEntityManager dbEntityManager = Context.getCommandContext().getDbEntityManager();
		ExecutionEntity processInstance = dbEntityManager.selectById(ExecutionEntity.class, processInstanceId);

		// get camunda properties
		Collection<CamundaProperty> camundaProperties = getCamundaProperties(processInstance);
		if (camundaProperties != null) {

			// find the camunda property with the name CAMUNDA_PROP_NAME
			Optional<CamundaProperty> variablesWithoutHistoryProperty = camundaProperties.stream()
					.filter(property -> property.getCamundaName().equals(CAMUNDA_PROP_NAME)).findFirst();

			// if the CAMUNDA_PROP_NAME is pressent -> tokenize the value and extract the
			// variable names as set
			if (variablesWithoutHistoryProperty.isPresent()) {

				Set<String> collectVariableNames = Collections
						.list(new StringTokenizer(variablesWithoutHistoryProperty.get().getCamundaValue(),
								CAMUNDA_PROP_VAR_SEP))
						.stream().map(token -> (String) token).collect(Collectors.toSet());
				variablesNames.addAll(collectVariableNames);

			}

		}

		return variablesNames;
	}

	protected Collection<CamundaProperty> getCamundaProperties(ExecutionEntity execution) {
		Process process = (Process) execution.getBpmnModelInstance().getDefinitions()
				.getUniqueChildElementByType(Process.class);
		ExtensionElements extensionElements = process.getExtensionElements();
		if (extensionElements != null) {
			CamundaProperties properties = (CamundaProperties) extensionElements
					.getUniqueChildElementByType(CamundaProperties.class);
			if (properties != null) {
				return properties.getCamundaProperties();
			}
		}
		return null;
	}

}
