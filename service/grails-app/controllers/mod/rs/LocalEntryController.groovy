package mod.rs

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import grails.converters.JSON;

import grails.gorm.multitenancy.Tenants
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.rs.DirectoryEntryService
import org.olf.rs.SettingsService
import org.olf.rs.referenceData.SettingsData

import org.olf.okapi.modules.directory.Symbol;

@Api(value = "/rs/localEntries", tags=["Local Entry Controller"], description="Local Entry Api")
class LocalEntryController {
    static final String OKAPITENANTHEADER = "X-Okapi-Tenant"
    SettingsService settingsService;

    @ApiOperation(
            value = "",
            nickname = "/",
            produces =  "application/json",
            httpMethod = "GET"
    )

    @ApiResponses([
            @ApiResponse(code=200, message = "Success")
    ])

    @ApiImplicitParams([
            @ApiImplicitParam(
                    name = "type",
                    paramType = "query",
                    required = false,
                    value = "The type of directory entries you want to filter on",
                    dataType = "string"
            )
    ])
    def index() {
        String tenant = request.getHeader(OKAPITENANTHEADER);
        List result;

        Tenants.withId(tenant.toLowerCase()+'_mod_rs', {
            String localSymbolsString = settingsService.getSettingValue(SettingsData.SETTING_LOCAL_SYMBOLS);
            List<DirectoryEntry> entries = [];
            List<Symbol> localSymbols = DirectoryEntryService.resolveSymbolsFromStringList(localSymbolsString);
            for (Symbol sym : localSymbols) {
                DirectoryEntry entry = DirectoryEntry.get(sym.owner.id);
                entries.add(entry);
            }
            if (params.type) {
                List <DirectoryEntry> filteredEntries = [];
                for ( DirectoryEntry de : entries ) {
                    if (de.type?.value == params.type) {
                        filteredEntries.add(de);
                    }
                }
                entries = filteredEntries;
            }
            //result = [ "localSymbols" : localSymbolsString, "test" : "testvalue" ];
            result = entries;
        });

        render result as JSON;
    }
}
