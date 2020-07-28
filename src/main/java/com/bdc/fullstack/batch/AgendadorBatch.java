package com.bdc.fullstack.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AgendadorBatch {

	private static final long HOUR = 3600000;
	private static final long MINUTE = 60000;
	private static final long SECONDS = 30000;

	private boolean runJobSingleton;

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	ApplicationContext context;

//	@Scheduled(fixedDelay = MINUTE)
	public void agendadorImportContasJob() {
		executarBatch("importContasJob");
	}

	public boolean isRunJobSingleton() {
		return runJobSingleton;
	}

	public void setRunJobSingleton(boolean runJobSingleton) {
		this.runJobSingleton = runJobSingleton;
	}

	public void executarBatch(String jobId) {
		try {
			JobParameters params = new JobParametersBuilder()
					.addLong("ID", System.currentTimeMillis())
					.toJobParameters();
			
			Job job = (Job) context.getBean(jobId);
			jobLauncher.run(job, params);

		} catch (Exception e) {
			log.error("erro ao executar batch", e);
		}
	}

}
