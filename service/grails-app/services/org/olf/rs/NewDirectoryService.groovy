package org.olf.rs

import groovy.util.logging.Slf4j
import com.k_int.okapi.OkapiClient

@Slf4j
public class NewDirectoryService {

    //@Autowired
    //OkapiClient okapiClient
    SimpleOkapiService simpleOkapiService;

    Object entriesBySymbol(String symbol) {
        def result = null
        log.debug("Attempting to retrieve directory entries for ${symbol}")
        try {
            String cqlParam = URLEncoder.encode("symbol any ${symbol}", "UTF-8");
            log.debug("Sending cql parameter: ${cqlParam}")
            //result = okapiClient.getSync("/directory/entries", [cql:cqlParam])
            result = simpleOkapiService.get("/directory/entries", [cql:cqlParam]);
            log.debug("Got directory entries ${result}");
        }
        catch ( Exception e ) {
            log.error("Problem connecting to directory ${e.toString()}");
            //log.debug("okapiClient: ${okapiClient} ${okapiClient?.inspect()}");
            e.printStackTrace();
        }
        return result;
    }

    Object institutionEntryBySymbol(String symbol) {
        if (!symbol) return null;
        def entries = entriesBySymbol(symbol);
        if (!entries || !(entries?.items in List) || entries?.items.size() < 1) {
            log.debug("No entries found for symbol ${symbol}, got ${entries.toString()}");
            return null;
        }
        return entries.items.find{ it.type == 'institution' }
    }

    Object branchEntryByNameAndParentSymbol(String name, String parentSymbol) {
        if (!parentSymbol || !name) return null;
        def entries = entriesBySymbol(parentSymbol);
        if (!entries || !(entries?.items in List) || entries?.items.size() < 1) {
            log.debug("No entries found for symbol ${parentSymbol}, got ${entries.toString()}");
            return null;
        }
        return entries.items.find{ it.name == name && it.type == 'branch' }
    }

    String shippingAddressForEntry(Map entry) {
        def addresses = entry?.addresses?.find{ it.type == 'Shipping' }?.addressComponents;
        if (!addresses) return null;
        def byType = addresses.groupBy { it.type }.collectEntries { k, v -> [(k): v*.value] };

        def cityLine = [];
        cityLine += byType['Locality'];
        cityLine += byType['AdministrativeArea'];
        cityLine += byType['PostalCode'];

        def lines = [];
        lines += byType['Other'] ?: [];
        lines += byType['Thoroughfare'] ?: [];
        lines << cityLine.join(', ');
        lines += byType['CountryCode'] ?: [];

        return lines.join('\n')
    }

    Map shippingAddressMapForEntry(Map entry, String line1 = null) {
        def addresses = entry?.addresses?.find{ it.type == 'Shipping' }?.addressComponents;
        if (!addresses) return null;
        def byType = addresses.groupBy { it.type }.collectEntries { k, v -> [(k): v*.value] };
        def result = [:];

        if (line1 || byType['Other']) {
            result.line1 = line1 ?: byType['Other']?.join(' / ');
            result.line2 = byType['Thoroughfare']?.join(' / ');
        } else {
            result.line1 = byType['Thoroughfare']?.join(' / ');
        }
        result.locality = byType['Locality']?.join(' / ')
        result.postalCode = byType['PostalCode']?.join(' / ')
        result.region = byType['AdministrativeArea']?.join(' / ')
        result.country = byType['CountryCode']?.join(' / ')

        return result;
    }
}
