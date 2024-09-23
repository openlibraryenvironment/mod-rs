package org.olf.rs

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult;
/**
 * Handle creating a new request from an existing one
 */

public class RerequestService {

    SharedIndexService sharedIndexService;

    public static List<String> preserveFields = ['author', 'authorOfComponent', 'copyrightType', 'edition', 'isbn', 'isRequester', 'issn', 'issue', 'neededBy', 'numberOfPages', 'oclcNumber', 'pagesRequested', 'patronEmail', 'patronGivenName', 'patronIdentifier', 'patronNote', 'patronReference', 'patronSurname', 'patronType', 'pickLocation', 'pickupLocationSlug', 'placeOfPublication', 'publicationDate', 'publisher', 'requestingInstitutionSymbol', 'serviceType', 'sponsoringBody', 'startPage', 'stateModel', 'subtitle', 'systemInstanceIdentifier', 'title', 'titleOfComponent', 'volume'];

    public createNewRequestFromExisting(PatronRequest originalRequest, List<String> copyFields, Map<String, Object> changeSet, Boolean updateBibRecords) {
        PatronRequest newRequest = new PatronRequest();
        copyFields.each {
            if (changeSet.containsKey(it)) {
                newRequest[it] = changeSet.get(it);
            } else {
                newRequest[it] = originalRequest[it];
            }
        }
        originalRequest.succeededBy = newRequest;
        newRequest.precededBy = originalRequest;

        if (updateBibRecords) {
            List<String> bibRecords = sharedIndexService.getSharedIndexActions().fetchSharedIndexRecords([systemInstanceIdentifier: newRequest.systemInstanceIdentifier]);
            if (bibRecords.size() == 1) { //Only continue if we've got 1 and only 1 result
                def bibRecord = bibRecords[0];
                def bibObject = parseBibRecordXML(bibRecord);
                if (bibObject) {
                    bibObject.each { k, v ->
                        if (v) {
                            newRequest."${k}" = v;
                        }
                    }
                }
            }
        }
        newRequest.save();
        originalRequest.save();

        return newRequest;
    }

    /*
    Title
   245 a b n
Author
   100 a b c d / 110 a b n d c/ 111 a n d c / else supply  "undefined"
Edition
   250 a
ISBN
   020 a  (only first occurrence)
ISSN
   022 a  (only first occurrence)
Publisher
   260 b / 264 b
Date of publication
   260 c / 264 c
     */
    public Map bibMap = [
            "title" : [
                    "fields" : [
                            [
                                    "field" : "245",
                                    "subfields" : [ 'a', 'b', 'n'],
                            ]
                    ],
                    "separator" : " "
            ],
            "author" : [
                    "fields" : [
                            [
                                    "field" : "100",
                                    "subfields" : [ 'a', 'b', 'c', 'd' ]
                            ],
                            [
                                    "field" : "110",
                                    "subfields": [ 'a', 'b', 'n', 'd', 'c' ]
                            ],
                            [
                                    "field" : "111",
                                    "subfields" : [ 'a', 'n', 'd', 'c' ]
                            ]
                    ],
                    "separator" : " "
            ],
            "edition" : [
                    "fields" : [
                            [
                                    "field" : "250",
                                    "subfields" : [ 'a' ]
                            ]
                    ],
                    "separator" : " "
            ],
            "isbn" : [
                    "fields" : [
                            [
                                    "field" : "020",
                                    "subfields" : [ 'a' ]
                            ]
                    ],
                    "separator" : " "
            ],
            "issn" : [
                    "fields" : [
                            [
                                    "field" : "022",
                                    "subfields" : [ 'a' ]
                            ]
                    ],
                    "separator" : " "
            ],
            "publisher" : [
                    "fields" : [
                            [
                                    "field" : "260",
                                    "subfields" : [ 'b' ]
                            ],
                            [
                                    "field" : "264",
                                    "subfields" : [ 'c' ]
                            ]
                    ],
                    "separator" : " "
            ],
            "publicationDate" : [
                    "fields" : [
                            [
                                    "field" : "260",
                                    "subfields" : [ 'c' ]
                            ],
                            [
                                    "field" : "264",
                                    "subfields" : [ 'c' ]
                            ]
                    ],
                    "separator" : " "
            ]
    ]

    public Map parseBibRecordXML(String xmlString) {
        Map result = [:];
        def xml = new XmlSlurper().parseText(xmlString);
        def metadata = xml.GetRecord.record.metadata;


        bibMap.each{ k, v ->
            for ( field in v.fields ) {
                List<String> valueList;
                GPathResult record = metadata.record;
                def tagMatch = { node -> def tag = node.@tag; tag == field.field }
                // def tagMatch = { node -> true }
                GPathResult datafield = record.datafield.find(tagMatch)
                if (!datafield.isEmpty()) {
                    valueList = datafield.subfield.findAll(node -> node.@code in field.subfields).collect(node -> node.text());
                }
                if (valueList?.size() > 0) {
                    result.put(k, valueList.join(v.separator))
                    break;
                }
            }
        }
        return result;

    }

}
