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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;

public class BpmCustomHistoryLevelProcessEnginePlugin extends AbstractProcessEnginePlugin {

	@Override
	public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		List<HistoryLevel> customHistoryLevels = processEngineConfiguration.getCustomHistoryLevels();
		if (customHistoryLevels == null) {
			customHistoryLevels = new ArrayList<HistoryLevel>();
			processEngineConfiguration.setCustomHistoryLevels(customHistoryLevels);
		}
		customHistoryLevels.add(BpmPerVariableHistoryLevel.getInstance());
	}

	@Override
	public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
		BpmPerVariableHistoryLevel.getInstance().addHistoryLevels(processEngineConfiguration.getCustomHistoryLevels());
	}

}
