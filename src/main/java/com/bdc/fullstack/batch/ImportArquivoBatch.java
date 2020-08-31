package com.bdc.fullstack.batch;

import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DefaultFieldSetFactory;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import com.bdc.fullstack.domain.ContaInput;
import com.bdc.fullstack.domain.ContaOutput;
import com.bdc.fullstack.ws.ReceitaService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableBatchProcessing
public class ImportArquivoBatch {

	private static final int MAX_ERRORS = 10;

	private static final int TRANSACTION_SIZE = 5;

	private List<String> headers = new ArrayList<>();

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	

	@Bean
	public Job importContasJob(JobCompletionNotificationListener listener, Step step1) {
		return jobBuilderFactory.get("importContasJob")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(step1)
				.end()
				.build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.<ContaInput, ContaOutput>chunk(TRANSACTION_SIZE)
				.reader(reader())
				.processor(new ContaItemProcessor())
				.writer(compositeItemWriter())
				.faultTolerant().skipLimit(MAX_ERRORS)
				.skip(RuntimeException.class)
				.build();
	}

	@Bean
	public FlatFileItemReader<ContaInput> reader() {

		FlatFileItemReader<ContaInput> itemReader = new FlatFileItemReader<ContaInput>();
		itemReader.setName("contaItemReader");
		itemReader.setResource(new FileSystemResource("C:\\Temp\\input\\contas_input.csv"));

		final DefaultLineMapper<ContaInput> lineMapper = new DefaultLineMapper<ContaInput>();
		final DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(";");
		lineTokenizer.setNames(new String[] { "agencia", "conta", "saldo", "status" });
		
		Locale locale = Locale.getDefault();
		DefaultFieldSetFactory fieldSetFactory = new DefaultFieldSetFactory();
		fieldSetFactory.setNumberFormat(NumberFormat.getInstance(locale));
		lineTokenizer.setFieldSetFactory(fieldSetFactory);
		
		lineMapper.setLineTokenizer(lineTokenizer);

		final BeanWrapperFieldSetMapper<ContaInput> fieldMapper = new BeanWrapperFieldSetMapper<>();
		fieldMapper.setTargetType(ContaInput.class);
		lineMapper.setFieldSetMapper(fieldMapper);
		itemReader.setLineMapper(lineMapper);
		itemReader.setLinesToSkip(1);
		
		itemReader.setSkippedLinesCallback(new LineCallbackHandler() {

	        public void handleLine(String line) {
	            headers.add(line);
	        }
	    });
		
		return itemReader;

	}

	public CompositeItemWriter<ContaOutput> compositeItemWriter() {
		CompositeItemWriter<ContaOutput> writer = new CompositeItemWriter<>();
		writer.setDelegates(Arrays.asList(new ReceitaItemWriter(), writerCsv()));
		
		return writer;
	}

	public class ContaItemProcessor implements ItemProcessor<ContaInput, ContaOutput> {

		@Override
		public ContaOutput process(final ContaInput contaInput) throws Exception {

			final ContaOutput contaOutput = new ContaOutput();

			contaOutput.setAgencia(contaInput.getAgencia());
			contaOutput.setConta(contaInput.getConta());
			contaOutput.setSaldo(contaInput.getSaldo());
			contaOutput.setStatus(contaInput.getStatus());

			log.info("Converting (" + contaInput + ") from (" + contaOutput + ")");

			return contaOutput;
		}
	}

	public class ReceitaItemWriter implements ItemWriter<ContaOutput> {

		@Override
		public void write(List<? extends ContaOutput> contasOutput) throws Exception {

			contasOutput.parallelStream().forEach(contaOutput -> {
				
				ReceitaService receitaService = new ReceitaService();
				
				try {
					double saldo = Double.valueOf(contaOutput.getSaldo().replace(",", "."));
					boolean result = receitaService.atualizarConta(contaOutput.getAgencia(), contaOutput.getConta().replace("-", ""), saldo,
							contaOutput.getStatus());
					
					contaOutput.setResultado(result ? "Sucess" : "Fail");
					
				} catch (RuntimeException e) {
					contaOutput.setResultado("Fail");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				log.info("ReceitaService write: {}", contaOutput);
			});
		}
	}
	
	@Bean
	public FlatFileItemWriter<ContaOutput> writerCsv() {
		
	    DelimitedLineAggregator<ContaOutput> myFileLineAggregator = new DelimitedLineAggregator<>();
	    myFileLineAggregator.setDelimiter(";");
	    myFileLineAggregator.setFieldExtractor(getMyFileFieldExtractor());
	    
		return new FlatFileItemWriterBuilder<ContaOutput>()
				.name("contaItemWriter")
				.resource(new FileSystemResource("C:\\Temp\\output\\contas_output.csv"))
				.delimited()
				.names(new String[] { "agencia", "conta", "saldo", "status", "resultado" })
				.lineAggregator(myFileLineAggregator)
				.transactional(true)
				.headerCallback(new FlatFileHeaderCallback() {
			        public void writeHeader(Writer writer) throws IOException {
			        	headers.add(";resultado");
			        	for (String header : headers) {
			                writer.write(header);
			            }
			        }
			    })
				.build();
	}
	
	private FieldExtractor<ContaOutput> getMyFileFieldExtractor() {
		final String[] fieldNames = new String[] { "agencia", "conta", "saldo", "status", "resultado" };  
		
		return item -> {
			BeanWrapperFieldExtractor<ContaOutput> extractor = new BeanWrapperFieldExtractor<>();
			extractor.setNames(fieldNames);
			return extractor.extract(item);
		};
	}
	

}
