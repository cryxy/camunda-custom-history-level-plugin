package de.cryxy.bpm.camunda.plugin.history;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.execute;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.job;
import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test the {@link BpmPerVariableHistoryLevel}.
 *
 */
public class BpmPerVariableHistoryLevelTest {

	@Rule
	public ProcessEngineRule processEngineRule = new ProcessEngineRule("camunda-per-process.cfg.xml");

	protected RepositoryService repositoryService;
	protected RuntimeService runtimeService;
	protected HistoryService historyService;

	@Before
	public void getEngineServices() {
		repositoryService = processEngineRule.getRepositoryService();
		runtimeService = processEngineRule.getRuntimeService();
		historyService = processEngineRule.getHistoryService();
	}

	@Test
	@Deployment(resources = { "process-without-custom-property.bpmn" })
	public void testProcessHistoryFull() {
		runtimeService.startProcessInstanceByKey("process-without-custom-property");


		// assert that full history was written
		assertEquals(4, historyService.createHistoricVariableInstanceQuery().count());
	}

	@Test
	@Deployment(resources = { "process-history-per-variable.bpmn" })
	public void testProcessHistoryPerVariable() {
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process-history-per-variable");
		assertThat(processInstance).isStarted();

		// Service Task 1
		execute(job());
		// assert that only two variables were written
		assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());
		
		// Service Task 2
		execute(job());
		// assert that only two variables were written
		assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());
		
		assertThat(processInstance).isEnded();
	}

}
