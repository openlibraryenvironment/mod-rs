package org.olf.rs

/**
 * Handle creating a new request from an existing one
 */


public class RerequestService {
    public static List<String> preserveFields = ['author','edition','isbn','isRequester','issn','issue','neededBy','numberOfPages','oclcNumber','patronEmail','patronGivenName','patronIdentifier','patronNote','patronReference','patronSurname','patronType','pickLocation','pickupLocationSlug','placeOfPublication','publicationDate','publisher','requestingInstitutionSymbol','sponsoringBody','startPage','stateModel','subtitle','systemInstanceIdentifier','title','volume', 'serviceType'];

    public createNewRequestFromExisting(PatronRequest originalRequest, List<String> copyFields, Map<String, Object> changeSet) {
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
        newRequest.save();
        originalRequest.save();

        return newRequest;
    }

}
