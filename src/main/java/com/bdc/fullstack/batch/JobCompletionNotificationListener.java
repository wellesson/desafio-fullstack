package com.bdc.fullstack.batch;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("JOB CONCLUIDO");
		}
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		log.info("inciando Job {}", jobExecution.getJobInstance().getJobName());
	}

}
