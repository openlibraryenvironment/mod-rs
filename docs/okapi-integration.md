# Built in tooling for OKAPI integration
The starter comes preinstalled with some useful OKAPI integration. 

## The OKAPI Controller
This module provides an OKAPI controller, but no route is provided in the mappings by default. In order to allow OKAPI to notify the application, when a new tenant
is created, you will have to add aan entry into the URLMappings.groovy file for the tenant path ('_/tenant').

``` Groovy
"/_/tenant" (controller: 'okapi', action:'tenant')
```

## Integration with Spring security
Any permissions detailed in the permissionDesired when supplying a module descriptor to OKAPI are sent through to in the request from OKAPI if the user has them.
See the [authorization section](https://github.com/folio-org/okapi/blob/294a4328f542c5df8fc9d2b03ab3ed9474ac5006/doc/security.md#authorization) of the OKAPI docs.
When the above permissions come through the module will make these available for you to use throughout the application asauthorities within spring security. All
the permissions from OKAPI will be prefixed with 'folio.', to avoid any collisions with any other authorities you might assign.

You can use these to secure roots:
``` YML
plugin:
  springsecurity:
    ...  
    controllerAnnotations:
      staticRules:
        -
          pattern: '/application/**'
          access: 
            - 'permitAll'
        -
          pattern: '/**'
          access:
            - 'hasAuthority("folio.resource-sharing.user")'
```
Helper methods are added to the OKAPI aware controllers to allow you to easily access the 'patron' (the user based on the headers from OKAPI) and
the current patrons granted authorities.
Secure single method stubs, or use helpers present on the OKAPI controllers to do different things when different authorities are granted.

``` Groovy
@CurrentTenant
class SomeController extends OkapiTenantAwareController<SomeMultiTenantDomain> {
  
  RequestController() {
    super(SomeMultiTenantDomain)
  }
  
  @Override
  def index() {
    
    if (patron && hasAuthority('folio.my.super.permisison')) {
      // Do something...
    } else {
      // Do something else...
    }
  }
  
  @Secured('hasAuthority("folio.my.special.permission")')
  def securedEntryPoint() {
    // Do something ...
  }
}