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

import static de.cryxy.bpm.camunda.plugin.history.Constants.VAR_ACTION;
import static de.cryxy.bpm.camunda.plugin.history.Constants.VAR_CAMUNDA;
import static de.cryxy.bpm.camunda.plugin.history.Constants.VAR_FOO;
import static de.cryxy.bpm.camunda.plugin.history.Constants.VAR_HELLO;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class UpdateVariablesDelegate implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		execution.setVariable(VAR_FOO, "bar2");
		execution.setVariable(VAR_HELLO, "world2");
		execution.setVariable(VAR_ACTION, "important2");
		execution.setVariable(VAR_CAMUNDA, "rocks2");
	}
}
