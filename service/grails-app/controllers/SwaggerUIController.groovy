
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import swagger.grails.SwaggerService;

/**
 * Swagger UI Controller
 */
class SwaggerUIController {
    private static final String RESPONSE_CREATED = "201";
    private static final String RESPONSE_DELETED = "204";
    private static final String RESPONSE_SUCCESS = "200";

    private static final String OPERATION_DELETE = "delete";
    private static final String OPERATION_POST   = "post";

    SwaggerService swaggerService

    /** The controllers we want to ignore, which are defined in the tags */
    private static final String[] tagsToIgnore = [
        "External Api Controller",
        "Has Hidden Record Controller",
        "Login Controller",
        "Logout Controller",
        "Okapi Tenant Aware Swagger Controller",
        "Resource Proxy Controller",
        "Tenant Controller"
    ];

    /** The HTTP operations we check the tags for */
    private static final String[] operationTypes = [
        "get",
        "post",
        "put",
        "delete"
    ];

    /** The parameter definition for the X-Okapi-Tenant header */
    private static final Map parameterOkapiTenant = [
        name : "X-Okapi-Tenant",
        in : "header",
        description : "The tenant the request is for",
        required : true,
        type : "string",
        default : "diku"
    ];

    /** The parameter definition for the X-OKAPI-TOKEN header */
    private static final Map parameterOkapiToken = [
        name : "X-OKAPI-TOKEN",
        in : "header",
        description : "The authentication token to use with this request",
        required : false,
        type : "string"
    ];

    /**
     * Provides the Swagger UI interface, the index.gsp file
     * @return
     */
    def index() {
    }

    /**
     * Generates the api documentation, as we do not want all package being documented and there is no way to configure
     * which packages we want or to exclude, we do not use the default SwaggerController api call, but simulate it here
     * and then manipulate what is returned, it is hard coded.
     * @return The api document in json form
     */
    def api() {
        // Generate the document
        String docApiAsString = swaggerService.generate();

        // Now remove the controllers we are not interested in
        Map docApiAsJson = (new JsonSlurper()).parseText(docApiAsString);
        Map paths = docApiAsJson.paths;
        if (paths != null) {
            // Remove the paths we are not interested in
            tagsToIgnore.each{ tag -> removePathsWithTag(paths, tag) };

            // Now add the tenant and token parameters to all the operations
            addParametersToOperations(paths, [ parameterOkapiTenant, parameterOkapiToken ]);

            // We don't want a 200 response if we have a created response
            remove200ResponsesWhenAlternativeExist(paths, OPERATION_POST, RESPONSE_CREATED);

            // We do not want a 200 response if we have a deleted response
            remove200ResponsesWhenAlternativeExist(paths, OPERATION_DELETE, RESPONSE_DELETED);
        }

        // We should now just have the collers we are interested in
        header("Access-Control-Allow-Origin", request.getHeader('Origin'))
        render(status: 200, contentType: "application/json", text: JsonOutput.toJson(docApiAsJson));
    }

    /**
     * Removes a path it a tag associated with an operation has the specified tag
     * @param paths The paths that we need to check
     * @param tag The tag that specifies that a path should be removed if it is contained in the list of tags for a path operation
     */
    private void removePathsWithTag(Map paths, String tag) {
        paths.removeAll {
            for (int i = 0; i < operationTypes.size(); i++) {
                def operationDefinition = it.value[operationTypes[i]];
                if (operationDefinition != null) {
                    if (operationDefinition?.tags.contains(tag)) {
                        return(true);
                    }
                }
            }

            // Obviously one we want to keep
            return(false);
        }
    }

    /**
     * Adds the supplied parameters to all operations
     * @param paths The paths that need the parameters adding to them
     * @param parameters The parameters that need adding
     */
    private void addParametersToOperations(Map paths, List<Map> parameters) {
        paths.each { path, pathDetails ->
            if (pathDetails != null) {
                pathDetails.each { operation, operationDetails ->
                    if (operationDetails.parameters == null) {
                        operationDetails.parameters = [ ];
                    }

                    // Now for each parameter we have been passed, add it to the list
                    parameters.each { additionalParameter ->
                        operationDetails.parameters.add(additionalParameter);
                    }
                }
            }
        }
    }

    /**
     * Remove the 200 response if an alternative positive response exists
     * @param paths The paths that need the 200 potentially removing
     * @param operation The operation that we need to check for the 200 to remove
     * @param alternativeResponse The alternative positive response
     */
    void remove200ResponsesWhenAlternativeExist(Map paths, String operation, String alternativeResponse) {
        paths.each { path, pathDetails ->
            if (pathDetails != null) {
                // Does this path have the operation we are interested in
                Map operationDetails = pathDetails[operation];

                // Did we find some operation details
                if (operationDetails != null) {
                    // It does, so we need to look at the responses
                    if (operationDetails.responses != null) {
                        // So do we have the 200 response as well as the alternative response
                        if ((operationDetails.responses[RESPONSE_SUCCESS] != null) &&
                            (operationDetails.responses[alternativeResponse] != null)) {
                            // We do so we need to remove the 200 response
                            operationDetails.responses.remove(RESPONSE_SUCCESS);
                        }
                    }
                }
            }
        }
    }
}
