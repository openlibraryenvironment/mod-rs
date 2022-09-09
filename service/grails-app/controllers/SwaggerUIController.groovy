
import groovy.json.JsonOutput;
import groovy.json.JsonSlurper;
import swagger.grails.SwaggerService;

/**
 * Swagger UI Controller
 */
class SwaggerUIController {
    SwaggerService swaggerService

    /** The controllers we want to ignore, which are defined in the tags */
    private static final String[] tagsToIgnore = [
        "External Api Controller",
        "Has Hidden Record Controller",
        "Login Controller",
        "Logout Controller",
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
}
