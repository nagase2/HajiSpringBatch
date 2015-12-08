package hello;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * For starters, the @EnableBatchProcessing annotation adds many critical beans that support jobs and saves you a lot of leg work. 
 * This example uses a memory-based database (provided by @EnableBatchProcessing), 
 * meaning that when it’s done, the data is gone.
 */
@Configuration
@EnableBatchProcessing
@Slf4j
public class BatchConfiguration {
  
  /*
   *  The first chunk of code defines the input, processor, and output.
   *   - reader() creates an ItemReader. It looks for a file called sample-data.csv and parses each line item with 
   *   enough information to turn it into a Person. - processor() creates an instance of our PersonItemProcessor you defined earlier,
   *    meant to uppercase the data. - write(DataSource) creates an ItemWriter. 
   *    This one is aimed at a JDBC destination and automatically gets a copy of the dataSource created by @EnableBatchProcessing.
   *   It includes the SQL statement needed to insert a single Person driven by Java bean properties.
   */
  
    /**
     * Inputクラス
     * reader() creates an ItemReader. It looks for a file called sample-data.csv and parses each line item with enough 
     * information to turn it into a Person. - processor() creates an instance of our PersonItemProcessor you defined earlier, 
     * meant to uppercase the data. 
     * @return
     */
    @Bean
    public ItemReader<PersonIn> reader() {
      log.info("CSVからデータを読み出します。");
        FlatFileItemReader<PersonIn> reader = new FlatFileItemReader<PersonIn>();
        reader.setResource(new ClassPathResource("sample-data.csv"));
        reader.setLineMapper(new DefaultLineMapper<PersonIn>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "firstName", "lastName" });
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<PersonIn>() {{
                setTargetType(PersonIn.class);
            }});
        }});
        return reader;
    }

    @Bean
    public ItemProcessor<PersonIn, PersonOut> processor() {
        return new PersonItemProcessor();
    }
    
    /**
     * write(DataSource) creates an ItemWriter. This one is aimed at a JDBC destination and automatically
     * gets a copy of the dataSource created by @EnableBatchProcessing. 
     * It includes the SQL statement needed to insert a single Person driven by Java bean properties.
     */
    @Bean
    public ItemWriter<PersonOut> writer(DataSource dataSource) {
        JdbcBatchItemWriter<PersonOut> writer = new JdbcBatchItemWriter<PersonOut>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<PersonOut>());
        writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
        writer.setDataSource(dataSource);
        return writer;
    }
  /*
   * The first method defines the job
   */
    @Bean
    public Job importUserJob(JobBuilderFactory jobs, Step s1, JobExecutionListener listener) {
      
        return jobs.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                //The listener() method lets you hook into the engine and detect when the job is complete, triggering the verification of results.
                .listener(listener)
                .flow(s1)
                .end()
                .build();
    }
    
    /*
     * the second one defines a single step. Jobs are built from steps, 
     * where each step can involve a reader, a processor, and a writer.
     */
    @Bean
    public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<PersonIn> reader,
            ItemWriter<PersonOut> writer, ItemProcessor<PersonIn, PersonOut> processor) {
      log.info("テスト　Step1!");
      
        return stepBuilderFactory.get("step1") //<-これはなに？
               //In the step definition, you define how much data to write at a time. In this case, it writes up to ten records at a time. 
                .<PersonIn, PersonOut> chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
    
//    @Bean
//    public Step step2(StepBuilderFactory stepBuilderFactory, ItemReader<PersonIn> reader,
//            ItemWriter<PersonOut> writer, ItemProcessor<PersonIn, PersonOut> processor) {
//      log.info("テスト　Step2");
//      
//        return stepBuilderFactory.get("step2") //<-これはなに？
//               //In the step definition, you define how much data to write at a time. In this case, it writes up to ten records at a time. 
//                .<PersonIn, PersonOut> chunk(10)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .build();
//    }
    
    /*
     * For demonstration purposes, there is code to create a JdbcTemplate, 
     * query the database, and print out the names of people the batch job inserts.
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
