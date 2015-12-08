package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

/**
 * intermediate processor
 * Transformerクラス
 * @author ac12955
 *
 */
public class PersonItemProcessor implements ItemProcessor<PersonIn, PersonOut> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

   // @Override
    public PersonOut process(final PersonIn person) throws Exception {
      
      log.info("取得した名称を大文字にします。");
        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();

        final PersonOut transformedPerson = new PersonOut(firstName, lastName);

        log.info("Converting (" + person + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }

}
