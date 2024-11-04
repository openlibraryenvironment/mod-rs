# Swagger integration
This has been achieved by using the grails plugin [swagger-grails](https://github.com/steamcleaner/swagger-grails) as the grails 4.x version was no longer in any repositories we have built this version and put it in the k-int repository, the last commit / merge we have included occurred on 21/052021 at 04:32:56 with commit hash b2e8870e5b348d0aa3d70e01b23937c6ee5538b2.

## The API document
The endpoint for the api document can be found [here](http://localhost:8081/rs/swagger/api), a mapping hasn't been made so that it can be routed through okapi.
The http headers X-Okapi-Tenant and X-OKAPI-TOKEN are automatically added as parameters for each operation, X-OKAPI-TOKEN is not relevant until we route it through okapi.

## The Swagger UI
The swagger ui has been taken from [here](https://github.com/swagger-api/swagger-ui) and can be found in service/src/main/resources/public/swaggerUI/<version> 
the one file that we change is swagger-initializer.js where we change the url for the api document and have added functions to disable the top bar and the authorize button.
If the version is updated be sure to update index.gsp at grails-app/views/swaggerUI, this is essentially a copy of the index.html but has the paths changed to /static/swaggerUI/<version> for all link and element scripts. 


## The SwaggerUIController
Due to the generation of the document not being configurable we do some manipulation of the generated document before it is returned to the caller, which are
1. Remove certain controllers that we do not wish to expose (we do it by the controllers name, but we could just as as well do it using a path prefix as it would be much simpler.
2. Add the X-Okapi-Tenant and X-OKAPI-TOKEN to all operations to ensure we are talking to the right tenant and we are authorised, the X-OKAPI-TOKEN is only required if you are going through OKAPI

## Annotating the Code
To annotate a class use an annotation like the following
```
@Api(value = "/rs", tags = ["Available Action Controller"], description = "AvailableAction Api")
```
Value will be prefixed to the path of all operations specified in this class.
Tags will be associated with the operations so they can be grouped together
Description is a brief descrion of this class

For the methods within a class they will need to be annotated like the following
```
    @ApiOperation(
        value = "List the states that a request can end up in after the action has been performed",
        nickname = "availableAction/toStates/{stateModel}/{actionCode}",
        produces = "application/json",
        httpMethod = "GET"
    )
    @ApiResponses([
        @ApiResponse(code = 200, message = "Success")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "stateModel",
            paramType = "path",
            required = true,
            value = "The state model the action is applicable for",
            dataType = "string",
            defaultValue = "PatronRequest"
        ),
        @ApiImplicitParam(
            name = "actionCode",
            paramType = "path",
            required = true,
            value = "The action that you want to know which states a request could move onto after the action has been performed",
            dataType = "string"
        )
    ])
```
Hopefully everything is reasonably obvious as to what it is defining, see the AvailableActionController for examples.

The imports required for the above annotations are
```
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
```

At the time of writing only the AvailableActionController has had the annotations added to it, but over time they should appear on the other controllers as well, feel free to add that are missing or correct any that are wrong.
