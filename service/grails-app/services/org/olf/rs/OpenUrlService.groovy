package org.olf.rs;

import org.olf.okapi.modules.directory.DirectoryEntry;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.statemodel.ActionResult;

/**
 * Handles all things to do with an OpenUrl
 *
 */
public class OpenUrlService {

    // The different sections of a context record
    private static final String OPEN_URL_10_SECTION_REQ = 'req';
    private static final String OPEN_URL_10_SECTION_RFT = 'rft';
    private static final String OPEN_URL_10_SECTION_SVC = 'svc';

    // The parameters we know about from the req section of the context object
    private static final String OPEN_URL_10_REQ_PATRON_EMAIL      = 'emailAddress';
    private static final String OPEN_URL_10_REQ_PATRON_IDENTIFIER = 'id';

    // The parameters we know about from the rft section of the context object
    private static final String OPEN_URL_10_RFT_ARTICLE_NUMBER               = 'artnum';
    private static final String OPEN_URL_10_RFT_ARTICLE_TITLE                = 'atitle';
    private static final String OPEN_URL_10_RFT_AUTHOR                       = 'au';
    private static final String OPEN_URL_10_RFT_AUTHOR_FIRST                 = 'aufirst';
    private static final String OPEN_URL_10_RFT_AUTHOR_FIRST_INITIAL         = 'auinitl';
    private static final String OPEN_URL_10_RFT_AUTHOR_FIRST_MIDDLE_INITIALS = 'auinit';
    private static final String OPEN_URL_10_RFT_AUTHOR_MIDDLE_INITIAL        = 'auinitm';
    private static final String OPEN_URL_10_RFT_AUTHOR_LAST                  = 'aulast';
    private static final String OPEN_URL_10_RFT_BICI                         = 'bici';
    private static final String OPEN_URL_10_RFT_BOOK_TITLE                   = 'btitle';
    private static final String OPEN_URL_10_RFT_CODEN                        = 'coden';
    private static final String OPEN_URL_10_RFT_CREATOR                      = 'creator';
    private static final String OPEN_URL_10_RFT_EDITION                      = 'edition';
    private static final String OPEN_URL_10_RFT_EISSN                        = 'eissn';
    private static final String OPEN_URL_10_RFT_GENRE                        = 'genre';
    private static final String OPEN_URL_10_RFT_IDENTIFIER                   = 'id';
    private static final String OPEN_URL_10_RFT_IDENTIFIER_ILLIAD            = 'identifier.illiad';
    private static final String OPEN_URL_10_RFT_ISBN                         = 'isbn';
    private static final String OPEN_URL_10_RFT_ISSN                         = 'issn';
    private static final String OPEN_URL_10_RFT_ISSUE                        = 'issue';
    private static final String OPEN_URL_10_RFT_JOURNAL_TITLE                = 'jtitle';
    private static final String OPEN_URL_10_RFT_PAGE_END                     = 'epage';
    private static final String OPEN_URL_10_RFT_PAGE_START                   = 'spage';
    private static final String OPEN_URL_10_RFT_PAGES                        = 'pages';
    private static final String OPEN_URL_10_RFT_PART                         = 'part';
    private static final String OPEN_URL_10_RFT_PUBLICATION_DATE             = 'date';
    private static final String OPEN_URL_10_RFT_PUBLICATION_PLACE            = 'place';
    private static final String OPEN_URL_10_RFT_PUBLISHER                    = 'pub';
    private static final String OPEN_URL_10_RFT_QUARTER                      = 'quarter';
    private static final String OPEN_URL_10_RFT_SICI                         = 'sici';
    private static final String OPEN_URL_10_RFT_SSN                          = 'ssn';
    private static final String OPEN_URL_10_RFT_TITLE                        = 'title';
    private static final String OPEN_URL_10_RFT_VOLUME                       = 'volume';

    // The parameters we know about from the svc section of the context object
    private static final String OPEN_URL_10_SVC_NEED_BY           = 'neededBy';
    private static final String OPEN_URL_10_SVC_PATRON_NOTE       = 'note';
    private static final String OPEN_URL_10_SVC_PICKUP_LOCATION   = 'pickupLocation';
    private static final String OPEN_URL_10_SVC_SERVICE_TYPE      = 'id';

    // These 2 are not used yet, as I couldn't see that they were mapped anywhere
    private static final String OPEN_URL_10_REFERRER_ID      = 'rfr_id';
    private static final String OPEN_URL_10_REFERRER_DATA_ID = 'rfe_id';

    // The OpenUrl 0.1 parameters
    private static final String OPEN_URL_01_ARTICLE_NUMBER               = 'artnum';
    private static final String OPEN_URL_01_AUTHOR_FIRST                 = 'aufirst';
    private static final String OPEN_URL_01_AUTHOR_FIRST_INITIAL         = 'auinitl';
    private static final String OPEN_URL_01_AUTHOR_FIRST_MIDDLE_INITIALS = 'auinit';
    private static final String OPEN_URL_01_AUTHOR_LAST                  = 'aulast';
    private static final String OPEN_URL_01_AUTHOR_MIDDLE_INITIAL        = 'auinitm';
    private static final String OPEN_URL_01_BICI                         = 'bici';
    private static final String OPEN_URL_01_CODEN                        = 'coden';
    private static final String OPEN_URL_01_GENRE                        = 'genre';
    private static final String OPEN_URL_01_ISSN                         = 'issn';
    private static final String OPEN_URL_01_EISSN                        = 'eissn';
    private static final String OPEN_URL_01_ISBN                         = 'isbn';
    private static final String OPEN_URL_01_ISSUE                        = 'issue';
    private static final String OPEN_URL_01_PAGE_END                     = 'epage';
    private static final String OPEN_URL_01_PAGE_START                   = 'spage';
    private static final String OPEN_URL_01_PAGES                        = 'pages';
    private static final String OPEN_URL_01_PART                         = 'part';
    private static final String OPEN_URL_01_PICKUP_LOCATION              = 'pickupLocation';
    private static final String OPEN_URL_01_PUBLICATION_DATE             = 'date';
    private static final String OPEN_URL_01_QUARTER                      = 'quarter';
    private static final String OPEN_URL_01_SEASON                       = 'ssn';
    private static final String OPEN_URL_01_SICI                         = 'sici';
    private static final String OPEN_URL_01_TITLE                        = 'title';
    private static final String OPEN_URL_01_TITLE_ABREVIATED             = 'stitle';
    private static final String OPEN_URL_01_TILE_ARTICLE                 = 'atitle';
    private static final String OPEN_URL_01_VOLUME                       = 'volume';

    // the different types of GENRE
    private static final String GENRE_ARTICLE    = 'article';
    private static final String GENRE_BOOK       = 'book';
    private static final String GENRE_BOOK_ITEM  = 'bookitem';
    private static final String GENRE_CONFERENCE = 'conference';
    private static final String GENRE_JOURNAL    = 'journal';
    private static final String GENRE_PREPRINT   = 'preprint';
    private static final String GENRE_PRECEEDING = 'preceeding';

    /**
     * Parses out the parameters of a request to create a request
     * Note: The original parser can be found at https://github.com/openlibraryenvironment/listener-openurl/blob/master/src/ReshareRequest.js
     * which this was originally based on
     * @param parameters The parameters in the request
     * @return A structure saying whether we were successful or not along with any appropriate messages and data to inform the caller
     */
    public Result mapToRequest(Map parameters) {
        Result result = new Result();
        PatronRequest request = new PatronRequest();

        // We only create requests for a requester
        request.isRequester = true;

        // If we have been supplied any any rft. parameters then we will assume it is an OpenURL 1.0 request
        if (parameters[OPEN_URL_10_SECTION_RFT]) {
            // Extract the information from an OpenURL 1.0
            mapReqToRequest(request, result, parameters[OPEN_URL_10_SECTION_REQ]);
            mapRftToRequest(request, result, parameters[OPEN_URL_10_SECTION_RFT]);
            mapSvcToRequest(request, result, parameters[OPEN_URL_10_SECTION_SVC]);
        } else {
            // We will assume it is a OpenURL 0.1
            mapOpenURL01ToRequest(request, result, parameters);
        }

        // Check we have valid locations
        if (verifyLocations(request, result)) {
            // All our checks have passed so try and save this request
            if (request.save(flush:true, failOnError:false)) {
                // Successful
                result.id = request.id;
                result.messages.add('Successfully created request');
            } else {
                // Failed to save the record
                result.result = ActionResult.ERROR;
                request.errors.allErrors.each { error ->
                    result.messages.add(error.toString());
                }
            }
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Looks up the specified setting in tthe OpenURL to see if it exists and trims it if it does
     * @param map The map that contains the parameters
     * @param setting The openURL value you are interested in
     * @return The value for the setting or null if it dosn't exist or is just contains whitespaces
     */
    private String defaultToNullIfEmpty(Map map, String setting) {
        String realValue = null;
        def value = map[setting];
        if (value != null) {
            // How do we need to convert it into a string
            if (value instanceof String) {
                // No conversion required
                realValue = value;
            } else if ((value instanceof List) && value) {
                // Turn it into a comma separated list
                realValue = value.join(',');
            } else  {
                // Just assume everything else has a toString method
                realValue = value.toString();
            }

            // Remove any leading or trailing whitespaces
            realValue = realValue.trim();

            // If it is empty or blank set it to null
            if (realValue.isEmpty() || realValue.isBlank()) {
                realValue = null;
            }
        }
        return(realValue);
    }

    /**
     * Concatenates 2 strings together
     * @param source The source string, can be null
     * @param valueToAppend The value to append
     * @param concatenator The concatenator to combine the strings
     * @return The combined string
     */
    private String appendStringIfNotNull(String source, String valueToAppend, String concatenator = " ") {
        String result = source;

        // Have we been supplied a string to append
        if (valueToAppend) {
            // Do we currently have a value
            if (result) {
                // We do so concatenate the values
                result += (concatenator + valueToAppend);
            } else {
                // We do not, so just the result to the value to append
                result = valueToAppend;
            }
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Maps all the values we expect to find in an OpenURL 1.0 req section to a request object
     * @param request The request we are populating
     * @param result The result object that is passed back to the caller
     * @param req The req section of the OpenURL 1.0 request
     */
    private void mapReqToRequest(PatronRequest request, Result result, Map req) {

        // Cannot do anything if the map is null
        if (req != null) {
            request.patronIdentifier = defaultToNullIfEmpty(req, OPEN_URL_10_REQ_PATRON_IDENTIFIER);
            request.patronEmail = defaultToNullIfEmpty(req, OPEN_URL_10_REQ_PATRON_EMAIL);
        }
    }

    /**
     * Maps all the values we expect to find in an OpenURL 1.0 rft section to a request object
     * @param request The request we are populating
     * @param result The result object that is passed back to the caller
     * @param rft The rft section of the OpenURL 1.0 request
     */
    private void mapRftToRequest(PatronRequest request, Result result, Map rft) {

        // Cannot do anything if the map is null
        if (rft != null) {
            request.publicationType = ProtocolReferenceDataValue.lookupPublicationType(defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_GENRE));
            request.systemInstanceIdentifier = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_IDENTIFIER);

            // Title can come from 1 of 3 fields
            request.title = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_TITLE);
            if (!request.title) {
                request.title = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_BOOK_TITLE);
                if (!request.title) {
                    request.title = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_JOURNAL_TITLE);
                }
            }
            request.stitle = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_BOOK_TITLE);
            request.titleOfComponent = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_ARTICLE_TITLE);

            // Author can come from multiple places
            request.author = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_AUTHOR);
            if (!request.author) {
                request.author = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_CREATOR);
                if (!request.author) {
                    // Not explicitly been given the authors full name, so we need to attempt to build it
                    generateAuthor(
                        request,
                        rft,
                        OPEN_URL_10_RFT_AUTHOR_FIRST,
                        OPEN_URL_10_RFT_AUTHOR_LAST,
                        OPEN_URL_10_RFT_AUTHOR_FIRST_INITIAL,
                        OPEN_URL_10_RFT_AUTHOR_MIDDLE_INITIAL,
                        OPEN_URL_10_RFT_AUTHOR_FIRST_MIDDLE_INITIALS
                    );
                }
            }

            // The other fields we are interested in
            request.publisher = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_PUBLISHER);
            request.placeOfPublication = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_PUBLICATION_PLACE);
            request.volume = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_VOLUME);
            request.issue = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_ISSUE);
            request.publicationDate = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_PUBLICATION_DATE);
            request.edition = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_EDITION);
            request.issn = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_ISSN);
            request.isbn = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_ISBN);
            request.coden = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_CODEN);
            request.sici = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_SICI);
            request.bici = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_BICI);
            request.eissn = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_EISSN);
            request.part = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_PART);
            request.artnum = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_ARTICLE_NUMBER);
            request.ssn = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_SSN);
            request.quarter = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_QUARTER);

            // Lets deal with the page stuff now
            request.startPage = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_PAGE_START);
            request.numberOfPages = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_PAGES);
            checkNumberOfPages(request, defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_PAGE_END), result);

            // The illiad identifier
            String illiadId = defaultToNullIfEmpty(rft, OPEN_URL_10_RFT_IDENTIFIER_ILLIAD);
            if (illiadId) {
                // We do have one, so add it as an identifier
                request.addToRequestIdentifiers(new RequestIdentifier(identifierType: IdentifierType.ILLIAD, identifier: illiadId));
            }
        }
    }

    /**
     * Maps all the values we expect to find in an OpenURL 1.0 svc section to a request object
     * @param request The request we are populating
     * @param result The result object that is passed back to the caller
     * @param svc The svc section of the OpenURL 1.0 request
     */
    private void mapSvcToRequest(PatronRequest request, Result result, Map svc) {

        // Cannot do anything if the map is null
        if (svc != null) {
            request.patronNote = defaultToNullIfEmpty(svc, OPEN_URL_10_SVC_PATRON_NOTE);
            request.pickupLocationSlug = defaultToNullIfEmpty(svc, OPEN_URL_10_SVC_PICKUP_LOCATION);
            request.neededBy = defaultToNullIfEmpty(svc, OPEN_URL_10_SVC_NEED_BY);
            request.serviceType = defaultToNullIfEmpty(svc, OPEN_URL_10_SVC_SERVICE_TYPE);
        }
    }

    /**
     * Maps all the values we expect to find in an OpenURL 0.1 to a request object
     * @param request The request we are populating
     * @param result The result object that is passed back to the caller
     * @param parameters The values supplied in the OpenURL
     */
    private void mapOpenURL01ToRequest(PatronRequest request, Result result, Map parameters) {
        // Cannot do anything if the map is null
        if (parameters != null) {
            // Author is a little convoluted, so it has its own method
            generateAuthor(
                request,
                parameters,
                OPEN_URL_01_AUTHOR_FIRST,
                OPEN_URL_01_AUTHOR_LAST,
                OPEN_URL_01_AUTHOR_FIRST_INITIAL,
                OPEN_URL_01_AUTHOR_MIDDLE_INITIAL,
                OPEN_URL_01_AUTHOR_FIRST_MIDDLE_INITIALS
            );

            // Now for the more general fields
            request.artnum = defaultToNullIfEmpty(parameters, OPEN_URL_01_ARTICLE_NUMBER);
            request.bici = defaultToNullIfEmpty(parameters, OPEN_URL_01_BICI);
            request.issn = defaultToNullIfEmpty(parameters, OPEN_URL_01_ISSN);
            request.eissn = defaultToNullIfEmpty(parameters, OPEN_URL_01_EISSN);
            request.coden = defaultToNullIfEmpty(parameters, OPEN_URL_01_CODEN);
            request.isbn = defaultToNullIfEmpty(parameters, OPEN_URL_01_ISBN);
            request.issue = defaultToNullIfEmpty(parameters, OPEN_URL_01_ISSUE);
            request.startPage = defaultToNullIfEmpty(parameters, OPEN_URL_01_PAGE_START);
            request.numberOfPages = defaultToNullIfEmpty(parameters, OPEN_URL_01_PAGES);
            checkNumberOfPages(request, defaultToNullIfEmpty(parameters, OPEN_URL_01_PAGE_END), result);
            request.part = defaultToNullIfEmpty(parameters, OPEN_URL_01_PART);
            request.publicationDate = defaultToNullIfEmpty(parameters, OPEN_URL_01_PUBLICATION_DATE);
            request.quarter = defaultToNullIfEmpty(parameters, OPEN_URL_01_QUARTER);
            request.sici = defaultToNullIfEmpty(parameters, OPEN_URL_01_SICI);
            request.title = defaultToNullIfEmpty(parameters, OPEN_URL_01_TITLE);
            request.titleOfComponent = defaultToNullIfEmpty(parameters, OPEN_URL_01_TILE_ARTICLE);
            request.stitle = defaultToNullIfEmpty(parameters, OPEN_URL_01_TITLE_ABREVIATED);
            request.volume = defaultToNullIfEmpty(parameters, OPEN_URL_01_VOLUME);

            // Pickup location is specific to us
            request.pickupLocationSlug = defaultToNullIfEmpty(parameters, OPEN_URL_01_PICKUP_LOCATION);
        }
    }

    /**
     * Takes all the values that can be supplied for an author and updates the request appropriately
     * @param request The request that needs updating
     * @param parameters The parameters that we will be grabbing the values from
     * @param firstName The key to obtain the first name of the author
     * @param lastName The key to obtain the last name of the author
     * @param firstInitial The key to obtain the first initial of the author
     * @param middleInitial The key to obtain the middle initial of the author
     * @param firstMiddleInitial The key to obtain the first and middle initials of the author
     */
    private void generateAuthor(
        PatronRequest request,
        Map parameters,
        String firstName,
        String lastName,
        String firstInitial,
        String middleInitial,
        String firstMiddleInitial) {

        // Author is made up of multiple fields
        request.author = defaultToNullIfEmpty(parameters, firstName);
        if (request.author == null) {
            // No first name so grab the initials
            request.author = defaultToNullIfEmpty(parameters, firstMiddleInitial);
            if (request.author == null) {
                // No first and middle initial, so see if there is just a first initial
                request.author = defaultToNullIfEmpty(parameters, firstInitial);
            }
        } else {
            // Concatenate the middle initial
            request.author = appendStringIfNotNull(request.author, defaultToNullIfEmpty(parameters, middleInitial));
        }

        // Append the last name
        request.author = appendStringIfNotNull(request.author, defaultToNullIfEmpty(parameters, lastName));
    }

    /**
     * Calculates the number of pages if it has not been supplied
     * @param request The request that needs updating
     * @param endPage The end page that the user is interested in
     * @param result The result object that is passed back to the caller
     */
    private void checkNumberOfPages(PatronRequest request, String endPage, Result result) {
        if (request.startPage && endPage && (request.numberOfPages == null)) {
            try {
                int intPageStart = request.startPage.toInteger();
                int intPageEnd = endPage.toInteger();
                if (intPageEnd >= intPageStart) {
                    // We can calculate the number of pages
                    request.numberOfPages = intPageEnd - intPageStart + 1;
                }
            } catch (Exception e) {
                String message = 'Exception thrown when trying to determine the number of pages from page start and page end, page start: "' + request.startPage + '", page end: "' + request.endPage + '"';
                result.messages.add(message);
                log.info(message, e);
            }
        }
    }

    /**
     * Checks that the location fields have sensible values and populates the request where required
     * @param request The request that needs updating
     * @param result The result object that is passed back to the caller
     * @return true if the location fields have sensible values otherwise false
     */
    private boolean verifyLocations(PatronRequest request, Result result) {
        // Do we have a pickup location slug
        if (request.pickupLocationSlug) {
            // Check that the pickup location is a valid slug
            DirectoryEntry pickupEntry = DirectoryEntry.findBySlug(request.pickupLocationSlug);
            if (pickupEntry == null) {
                result.result = ActionResult.ERROR;
                result.messages.add("Invalid pickup location supplied");
            } else {
                // We can now determine the institution from the pickup location
                DirectoryEntry institutionEntry = findInstitution(pickupEntry);;

                if (institutionEntry == null) {
                    result.result = ActionResult.ERROR;
                    result.messages.add("No institution located");
                } else {
                    if (institutionEntry.symbols) {
                        // We just take the first one, should probably do something cleverer
                        Symbol institutionSymbol = institutionEntry.symbols[0];
                        request.requestingInstitutionSymbol = institutionSymbol.authority.symbol + ':' + institutionSymbol.symbol
                    } else {
                        result.result = ActionResult.ERROR;
                        result.messages.add("No symbols found for institution");
                    }
                }
            }
        } else {
            result.result = ActionResult.ERROR;
            result.messages.add("No pickup location supplied");
        }
        return(result.result == ActionResult.SUCCESS);
    }

    /**
     * Looks up the institution entry for the supplied directory entry
     * @param entry The entry record we are starting from
     * @return The directory emtry for the institution or null if none was found
     */
    private DirectoryEntry findInstitution(DirectoryEntry entry) {
        DirectoryEntry potentialInstitution = entry;
        boolean institutionFound = false;

        // Loop until we have no more parents or we have found the institution
        while ((potentialInstitution != null) && !institutionFound) {
            // Is this entry the institution
            if (potentialInstitution.type.value == 'institution') {
                // We have found the institution
                institutionFound = true;
            } else {
                // Move onto the parent
                potentialInstitution = potentialInstitution.parent;
            }
        }

        // Return the institution entry
        return(potentialInstitution);
    }
}
