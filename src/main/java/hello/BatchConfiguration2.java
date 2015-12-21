package hello;

import javax.sql.DataSource;

import org.slf4j.Logger;
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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * どうやってJobやReaderなどが関連付けられているのか？
 * どうやったら、Jobを追加できるのか？
 * このページが非常に参考になる。
 * http://kagamihoge.hatenablog.com/entry/2015/02/14/144238
 */
/**
 * For starters, the @EnableBatchProcessing annotation adds many critical beans that support jobs and saves you a lot of leg work. 
 * This example uses a memory-based database (provided by @EnableBatchProcessing), 
 * meaning that when it’s done, the data is gone.
 */
@Configuration
@EnableBatchProcessing
@Import(DataSourceConfiguration.class)
public class BatchConfiguration2 {

  @Autowired
  private StepBuilderFactory steps;
  
  private static final Logger log = org.slf4j.LoggerFactory.getLogger(BatchConfiguration2.class);
  
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
      log.info("Readerが生成されました");
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

    @Bean(name="ip1")
    public ItemProcessor<PersonIn, PersonOut> processor1() {
      log.info("Item Processesor1が呼ばれませした ");
      return new PersonItemProcessor();
    }
    @Bean(name="ip2")
    public ItemProcessor<PersonIn, PersonOut> processor2() {
      log.info("Item Processesor2が呼ばれませした ");
      return new PersonItemProcessor2();
    }
    
    /**
     * write(DataSource) creates an ItemWriter. This one is aimed at a JDBC destination and automatically
     * gets a copy of the dataSource created by @EnableBatchProcessing. 
     * It includes the SQL statement needed to insert a single Person driven by Java bean properties.
     */
    @Bean(name="iw1")
    public ItemWriter<PersonOut> writer(DataSource dataSource) {
        log.info("item write1が呼ばれました");
        JdbcBatchItemWriter<PersonOut> writer = new JdbcBatchItemWriter<PersonOut>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<PersonOut>());
        writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
        writer.setDataSource(dataSource);
        return writer;
    }
    @Bean(name="iw2")
    public ItemWriter<PersonOut> writer2(DataSource dataSource) {
      log.info("item writer2が呼ばれました");
        JdbcBatchItemWriter<PersonOut> writer = new JdbcBatchItemWriter<PersonOut>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<PersonOut>());
        writer.setSql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)");
        writer.setDataSource(dataSource);
        return writer;
    }
    
  /*
   * The first method defines the job
   * Jobはうえから順番に実行される。（xxxxxx
   * Q:どうやって、JobBuilderFactoryとか、Stepに引数が渡されるのですか？
   * A:JobExecutionListenerとJobBuilderは＠EnableBatchProcessingが解釈された時、Stepはメソッドに書かれている＠Beanにより、スキャンの対象となります。
   * メソッドが呼ばれた時に、対象のオブジェクトが自動的にセットされます。nameが設定されている場合、引数の変数名と一致しているものを取得する仕様となっています。
   */
    @Bean(name="job1")
    public Job importUserJob(JobBuilderFactory jobs, Step s1,Step s2,JobExecutionListener listener) {
      log.info("-------------------job1を開始--------------------------------");
        return jobs.get("job1")
                .incrementer(new RunIdIncrementer())
                //The listener() method lets you hook into the engine and detect when the job is complete, triggering the verification of results.
                .listener(listener)
                .start(s1)
                .next(s2)
                .build();
    }
    @Bean(name="job2")
    public Job importUserJob2(JobBuilderFactory jobs, Step s1,Step s2,JobExecutionListener listener) {
       log.info("------------job2を開始------------------------------------");
        return jobs.get("job2")
                .incrementer(new RunIdIncrementer())
                //The listener() method lets you hook into the engine and detect when the job is complete, triggering the verification of results.
                .listener(listener)
                .start(step3())
                .next(step4())   //引数から呼んでも良い。
                //.next(step3()) //メソッドを直接呼んでも良い。
                .build();
    }
 

    @Bean
    public String createStr(){
    	return "fixedName";
    }
    @Bean(name="fixedStr")
    public String createStr2(){
    	return "fixedName";
    }
    
    /*
     * the second one defines a single step. Jobs are built from steps, 
     * where each step can involve a reader, a processor, and a writer.
     */
    @Bean(name="s1")
    public Step step1(StepBuilderFactory stepBuilderFactory, ItemReader<PersonIn> reader,
            ItemWriter<PersonOut> iw1, ItemProcessor<PersonIn, PersonOut> ip1,String fixedStr) {
      log.info("テスト　Step1!が呼ばれました。"+fixedStr);
      
        return stepBuilderFactory.get("step1") //<-これはなに？
               //In the step definition, you define how much data to write at a time. In this case, it writes up to ten records at a time. 
                .<PersonIn, PersonOut> chunk(1)
                .reader(reader)
                .processor(ip1)
                .writer(iw1)
                .build();
    }
    
    @Bean(name="s2")
    public Step step2(StepBuilderFactory stepBuilderFactory2, ItemReader<PersonIn> reader,
            ItemWriter<PersonOut> iw2, ItemProcessor<PersonIn, PersonOut> ip2) {
      log.info("テスト　Step2が呼ばれました。");
      
        return stepBuilderFactory2.get("step2") //<-これはなに？
               //In the step definition, you define how much data to write at a time. In this case, it writes up to ten records at a time. 
                .<PersonIn, PersonOut> chunk(10)
                .reader(reader)
                .processor(ip2)
                .writer(iw2)
                .build();
    }
    
    @Bean(name = "s3")
    public Step step3() {
        return steps.get("step3").tasklet((stepContribution, chunkContext) -> {
            System.out.println("step３ が実行(job2)");
            
            return RepeatStatus.FINISHED;
        }).build();
    }
    @Bean(name = "s4")
    public Step step4() {
        return steps.get("step4").tasklet((stepContribution, chunkContext) -> {
            System.out.println("step4 が実行(job2)");
            
            return RepeatStatus.FINISHED;
        }).build();
    }
    
    /*
     * For demonstration purposes, there is code to create a JdbcTemplate, 
     * query the database, and print out the names of people the batch job inserts.
     */
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

}
