package org.olf.rs.statemodel;

import grails.gorm.MultiTenant;

/**
 * This class represents the list of possible outcomes for an action / event
 */

class ActionEventResultList implements MultiTenant<ActionEventResultList> {

    /** The id of the list */
    String id;

    /** The code we have given to this result list */
    String code;

    /** A description of this result list */
    String description;

    static hasMany = [
        results: ActionEventResult
    ];

    static mappedBy = [
        results: 'none'
    ];

    static constraints = {
                    code (nullable: false, blank:false, unique: true)
             description (nullable: false, blank:false)
    }

    static mapping = {
                      id column : 'aerl_id', generator: 'uuid2', length: 36
                 version column : 'aerl_version'
                    code column : 'aerl_code', length: 64
             description column : 'aerl_description'
    }

    public static ActionEventResultList ensure(String code, String description, List<ActionEventResult> results) {
        // Lookup to see if the code exists
        ActionEventResultList actionEventResultList = findByCode(code);

        // Did we find it
        if (actionEventResultList == null) {
            // No we did not, so create a new one
            actionEventResultList = new ActionEventResultList (
                code: code,
                description: description
            );

            // Add each potential result we have been passed to the list
            if (results != null) {
                results.each{ result ->
                    actionEventResultList.addToResults(result);
                }
            }

            // and save it
            actionEventResultList.save(flush:true, failOnError:true);
        }

        // Return the result to the caller
        return(actionEventResultList);
    }
}


