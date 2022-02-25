package org.olf.rs.referenceData

import org.olf.rs.Counter;

import groovy.util.logging.Slf4j

@Slf4j
public class CounterData {

	public Counter ensureCounter(String context, String description, long value = 0) {
		Counter result = Counter.findByContext(context);
		if (result == null) {
			result = new Counter(
				context: context,
				value: value,
				description: description
			);
			result.save(flush:true, failOnError:true);
		}
		return result;
	}

	public void load() {
		log.info("Adding counter data to the database");

        ensureCounter('/activeLoans', 'Current (Aggregate) Lending Level');
        ensureCounter('/activeBorrowing', 'Current (Aggregate) Borrowing Level');
	}
	
	public static void loadAll() {
		(new CounterData()).load();
	}
}
