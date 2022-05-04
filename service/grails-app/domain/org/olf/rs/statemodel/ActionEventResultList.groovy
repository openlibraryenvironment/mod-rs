package org.olf.rs.statemodel;

import grails.gorm.MultiTenant;

/**
 * This class represents the list of possible outcomes for an action / event
 */

class ActionEventResultList implements MultiTenant<ActionEventResultList> {

    static public final String REQUESTER_AWAITING_RETURN_SHIPPING_ISO18626   = 'requesterAwaitingReturnShippingISO18626';
    static public final String REQUESTER_BORROWER_RETURNED_ISO18626          = 'requesterBorrowerReturnedISO18626';
    static public final String REQUESTER_BORROWING_LIBRARY_RECEIVED_ISO18626 = 'requesterBorrowingLibraryReceivedISO18626';
    static public final String REQUESTER_CANCEL_PENDING_ISO18626             = 'requesterCancelPendingISO18626';
    static public final String REQUESTER_CHECKED_IN_ISO18626                 = 'requesterCheckedInISO18626';
    static public final String REQUESTER_CONDITION_ANSWER_RECEIVED_ISO18626  = 'requesterConditionalAnswerReceivedISO18626';
    static public final String REQUESTER_EXPECTS_TO_SUPPLY_ISO18626          = 'requesterExpectsToSupplyISO18626';
    static public final String REQUESTER_OVERDUE_ISO18626                    = 'requesterOverdueISO18626';
    static public final String REQUESTER_RECALLED_ISO18626                   = 'requesterRecalledISO18626';
    static public final String REQUESTER_SENT_TO_SUPPLIER_ISO18626           = 'requesterSentToSupplierISO18626';
    static public final String REQUESTER_SHIPPED_ISO18626                    = 'requesterShippedISO18626';
    static public final String REQUESTER_SHIPPED_TO_SUPPLIER_ISO18626        = 'requesterShippedToSupplierISO18626';

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

    public ActionEventResult lookupResult(boolean successful, String qualifier) {
        // look through the results to see if we have an appropriate one
        return(results.find { result ->
            if (successful == result.result) {
                if (qualifier == null) {
                    return(result.qualifier == null);
                } else {
                    return(qualifier.equals(result.qualifier));
                }
            }
            return(false);
        });
    }

    public static ActionEventResultList ensure(String code, String description, List<ActionEventResult> results) {
        // We create ourselves a working list as we want to modify as we process it
        List<ActionEventResult> workingResults = ((results == null) ? null : results.collect());

        // Lookup to see if the code exists
        ActionEventResultList actionEventResultList = findByCode(code);

        // Did we find it
        if (actionEventResultList == null) {
            // No we did not, so create a new one
            actionEventResultList = new ActionEventResultList (
                code: code
            );
        } else {
            // Go through removing the items that no longer need to be there
            if (actionEventResultList.results.size() > 0) {
                // Process all the current records
                actionEventResultList.results.collect().each { result ->
                    // now look to see if it is in the working list
                    ActionEventResult foundResult = workingResults.find { workingResult -> workingResult.id.equals(result.id) };
                    if (foundResult == null) {
                        // no longer required so remove from the database
                        actionEventResultList.removeFromResults(result);
                    } else {
                        // Remove it from the working results as it is already in the database
                        workingResults.remove(workingResults.indexOf(foundResult));
                    }
                }
            }
        }

        // Now update the other fields in case they have changed
        actionEventResultList.description = description;

        // workingResults should only have the items in that need adding
        if (workingResults != null) {
            workingResults.each{ result ->
                // Just add the result as its not already there
                actionEventResultList.addToResults(result);
            }
        }

        // and save it
        actionEventResultList.save(flush:true, failOnError:true);

        // Return the result to the caller
        return(actionEventResultList);
    }

    public static ActionEventResultList lookup(String code) {
        ActionEventResultList actionEventResultList = null;
        if (code != null) {
            actionEventResultList = findByCode(code);
        }
        return(actionEventResultList);
    }
}


