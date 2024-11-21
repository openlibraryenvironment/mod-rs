package org.olf.rs.sharedindex

import org.olf.rs.AvailabilityStatement
import org.olf.rs.SettingsService
import org.olf.rs.SharedIndexActions
import org.olf.rs.Z3950Service
import org.olf.rs.logging.DoNothingHoldingLogDetails
import org.olf.rs.logging.IHoldingLogDetails
import org.olf.rs.referenceData.SettingsData
import groovy.xml.XmlUtil

class ANBDSharedIndexService implements SharedIndexActions {

    Z3950Service z3950Service;
    SettingsService settingsService;
    private static IHoldingLogDetails defaultHoldingLogDetails = new DoNothingHoldingLogDetails();
    final String searchAttribute = "@attr 1=12";

    /**
     * findAppropriateCopies - Accept a map of name:value pairs that describe an instance and see if we can locate
     * any appropriate copies in the shared index.
     * @param description A Map of properies that describe the item. Currently understood properties:
     *                         systemInstanceIdentifier - Instance identifier for the title we want copies of
     *                         title - the title of the item
     * @return instance of SharedIndexAvailability which tells us where we can find the item.
     */
    @Override
    List<AvailabilityStatement> findAppropriateCopies(Map description) {
        List<AvailabilityStatement> result = [];
        String query = makeQuery(description);
        if (!query) {
            log.warn("Could not generate query from description Map");
        } else {
            def z_response = retrieveRecordsFromZ3950(query);
            List<Map> mapList = getHoldingsMaps(z_response);
            mapList.each({ map ->
                def statement = new AvailabilityStatement(
                        symbol: map.symbol,
                        instanceIdentifier: map.localIdentifier,
                        illPolicy: map.illPolicy ?: null
                );
                result.add(statement);
            });
        }
        return result;
    }

    List<Map> getHoldingsMaps(z_response) {
        def mapList = [];
        z_response?.records?.record?.each( { record ->
            record?.recordData?.record?.datafield?.findAll({ it.'@tag' == '850'})?.each({ field ->
                String circulationString = field?.subfield?.find({ it.'@code' == 'c' })?.text();
                boolean lendable = false;
                if (!circulationString ||
                        !(circulationString.toLowerCase().startsWith('held') || circulationString.toLowerCase().startsWith('online:'))) {
                    lendable = true;
                }
                def dataMap = [:];
                dataMap.symbol = field?.subfield?.find({ it.'@code' == 'a' })?.text();
                dataMap.localIdentifier = field?.subfield?.find({ it.'@code' == 'b' })?.text();
                if (lendable) {
                    dataMap.illPolicy = AvailabilityStatement.LENDABLE_POLICY;
                }
                mapList.add(dataMap);

            });
        });
        return mapList;
    }

    List<String> getRecords(z_response) {
        List<String> result = [];
        z_response?.records?.record?.each( { record ->
            record?.recordData?.record?.findAll()?.each({ rec ->
                XmlUtil xmlUtil = new XmlUtil();
                result.add(xmlUtil.serialize(rec));
            })
        });
        return result;
    }

    /**
     * fetch - Accept a map of name:value pairs that describe an instance and see if we can locate
     * any appropriate copies in the shared index. The returned string is OPAQUE to the application -
     * client UI elements should not assum any specific format (E.G. Json) or schema (E.G. FOLIO Inventory).
     * this is a text blob intended to aid problem solving and is specific to any shared index in use.
     *
     * In the future we MAY provide a canonical representation but it is important to preserve the raw record
     *
     * @param description See above.
     * @return List of records as returned by the shared index. TODO: standardise record format.
     */
    @Override
    List<String> fetchSharedIndexRecords(Map description) {
        String query = makeQuery(description);
        if (!query) {
            log.warn("Could not generate query from description Map ${description}");
        } else {
            def z_response = retrieveRecordsFromZ3950(query);
            return getRecords(z_response);
        }
        return [];
    }

    def String makeQuery(Map description) {
        if (description?.systemInstanceIdentifier != null) {
            def id_list;
            String result;
            if (description.systemInstanceIdentifier instanceof String) {
                id_list = [];
                id_list.add(description.systemInstanceIdentifier)
            } else if (description.systemInstanceIdentifier instanceof List) {
                id_list = description.systemInstanceIdentifier;
            } else {
                log.warn("systemInstanceIdentifier should be either a string or a list of strings");
                return null;
            }
            def termList = [];
            id_list.each({ id ->
                String term = "${searchAttribute} ${id}";
                termList.add(term);
            });

            return makeCompoundSearch(termList).join(" ");

        } else {
            log.warn("No systemInstanceIdentifier field provided to search for");
            return null;
        }
    }

    //recursive method to chain a bunch of terms together by @or for PQF
    def makeCompoundSearch(List<String> terms) {
        List<String> newStack = [];
        if (terms.size() < 2) {
            return terms;
        }
        while (!terms.isEmpty()) {
            String term1 = terms.pop();
            if (terms.isEmpty()) {
                newStack.push(term1);
                break;
            } else {
                String term2 = terms.pop();
                String orString = "@or ${term1} ${term2}";
                newStack.push(orString);
            }
        }
        return makeCompoundSearch(newStack);
    }

    def retrieveRecordsFromZ3950(String z_query) {
        //We will use the shared index url as the z-target
        int max_records = 100; //Need to find a good default value
        String schema = "marcxml"; //Will this ever change?

        String z3950_host = settingsService.getSettingValue(SettingsData.SETTING_SHARED_INDEX_BASE_URL);

        String z3950_username = settingsService.getSettingValue(SettingsData.SETTING_SHARED_INDEX_USER);
        String z3950_password = settingsService.getSettingValue(SettingsData.SETTING_SHARED_INDEX_PASS);

        String z3950_proxy = settingsService.getSettingValue(SettingsData.SETTING_Z3950_PROXY_ADDRESS);

        def z_response = z3950Service.executeQuery(z3950_host, z3950_proxy, z_query, max_records, schema,
            defaultHoldingLogDetails, z3950_username, z3950_password);

        return z_response;

    }
}
