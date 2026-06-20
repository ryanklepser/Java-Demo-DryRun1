package ca.uhn.fhir.jpa.batch.config;

/*-
 * #%L
 * HAPI FHIR JPA Server - Batch Task Processor
 * %%
 * Copyright (C) 2014 - 2022 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;


public class NonPersistedBatchConfigurer {
	@Autowired
	@Qualifier("hapiTransactionManager")
	private PlatformTransactionManager myHapiPlatformTransactionManager;

	@Autowired
	@Qualifier(BatchConstants.JOB_LAUNCHING_TASK_EXECUTOR)
	private TaskExecutor myTaskExecutor;

	@Autowired(required = false)
	private DataSource myDataSource;

	private JobRepository myJobRepository;
	private JobExplorer myJobExplorer;
	private JobLauncher myJobLauncher;

	public PlatformTransactionManager getTransactionManager() {
		return myHapiPlatformTransactionManager;
	}

	public JobRepository getJobRepository() throws Exception {
		if (myJobRepository == null) {
			myJobRepository = createJobRepository();
		}
		return myJobRepository;
	}

	protected JobRepository createJobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setTransactionManager(getTransactionManager());
		if (myDataSource != null) {
			factory.setDataSource(myDataSource);
		}
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	public JobExplorer getJobExplorer() throws Exception {
		if (myJobExplorer == null) {
			myJobExplorer = createJobExplorer();
		}
		return myJobExplorer;
	}

	public JobExplorer createJobExplorer() throws Exception {
		JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
		if (myDataSource != null) {
			jobExplorerFactoryBean.setDataSource(myDataSource);
		}
		jobExplorerFactoryBean.setTransactionManager(getTransactionManager());
		jobExplorerFactoryBean.afterPropertiesSet();
		return jobExplorerFactoryBean.getObject();
	}

	public JobLauncher getJobLauncher() throws Exception {
		if (myJobLauncher == null) {
			myJobLauncher = createJobLauncher();
		}
		return myJobLauncher;
	}

	protected JobLauncher createJobLauncher() throws Exception {
		TaskExecutorJobLauncher launcher = new TaskExecutorJobLauncher();
		launcher.setTaskExecutor(myTaskExecutor);
		launcher.setJobRepository(getJobRepository());
		launcher.afterPropertiesSet();
		return launcher;
	}
}
