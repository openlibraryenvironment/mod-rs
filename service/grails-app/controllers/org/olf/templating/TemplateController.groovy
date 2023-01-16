package org.olf.templating

import com.k_int.okapi.OkapiTenantAwareController;

import grails.gorm.multitenancy.CurrentTenant;
import groovy.util.logging.Slf4j;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Slf4j
@CurrentTenant
@Api(value = "/rs/template", tags = ["Template Container Controller"], description = "API for all things to do with template containers")
class TemplateController extends OkapiTenantAwareController<TemplateContainer>  {

  TemplateController() {
    super(TemplateContainer)
  }

  /*
   * Deletes the template if it is not currently in use
   */
    @ApiOperation(
        value = "Deletes the specified template container",
        nickname = "/{id}",
        httpMethod = "DELETE"
    )
    @ApiResponses([
        @ApiResponse(code = 204, message = "No Content Success"),
        @ApiResponse(code = 400, message = "Bsd Request the template is in use")
    ])
    @ApiImplicitParams([
        @ApiImplicitParam(
            name = "id",
            paramType = "path",
            required = true,
            value = "The id of the template container to delete",
            dataType = "string"
        )
    ])
    def delete() {
      // Before deletion, check this template container isn't in use as a value from any template setting
      if (!TemplatingService.usedInAppSettings(params.id)) {
        super.delete()
      } else {
        response.sendError(400, "That template is in use on one or more AppSettings")
      }
    }
}