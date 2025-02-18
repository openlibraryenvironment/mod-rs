package org.olf.rs.patronRequest


import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.DirectoryEntryService;
import org.olf.rs.PatronRequest
import org.olf.rs.SettingsService
import org.olf.rs.referenceData.SettingsData;

/**
 * Deals with all things to do with a pickup location
 * @author Chas
 */
public class PickupLocationService {

    /**
     * Checks the pickup location slug on the request and update the pickup location and resolved pickup location
     * @param request The patron request that needs to have its pickup location checked
     */
    SettingsService settingsService;

    public void check(PatronRequest request) {
        // Cannot do anything if we havn't been passed a pickup location
        if (request != null) {
            // If we were supplied a pickup location, attempt to resolve it here
            DirectoryEntry pickupLoc;
            if (request.pickupLocationSlug) {
                pickupLoc = DirectoryEntry.findBySlug(request.pickupLocationSlug);
            } else if (request.pickupLocationCode) { // deprecated
                pickupLoc = DirectoryEntry.find("from DirectoryEntry de where de.lmsLocationCode=:code and de.status.value='managed'", [code: request.pickupLocationCode]);
            }

            // Did we determine the pickup location
            if (pickupLoc == null) {
                // We did not so ensured the fields are cleared as we maybe editing
                request.resolvedPickupLocation = null;
                request.pickupLocation = null;
            } else {
                // We know about the supplied pickup location
                request.resolvedPickupLocation = pickupLoc;
                List pickupSymbols  = pickupLoc?.symbols?.findResults { symbol ->
                    symbol?.priority == 'shipping' ? symbol?.authority?.symbol + ':' + symbol?.symbol : null
                }

                // Set the descriptive part of the pickup location on the request
                request.pickupLocation = pickupSymbols.size() > 0 ? "${pickupLoc.name} --> ${pickupSymbols [0]}" : pickupLoc.name;
            }
        } else {
            log.warn("No request passed into PickupLocationService.check");
        }
    }

    void checkByName(PatronRequest request) {
        if (request.pickupLocation) {
            //DirectoryEntry pickupLoc = DirectoryEntry.find("from DirectoryEntry de where de.name=:name and de.status.value='managed'", [name: request.pickupLocation])
            DirectoryEntry pickupLoc = DirectoryEntry.find("from DirectoryEntry de where de.name=:name", [name: request.pickLocation]);
            List<Symbol> localSymbols = DirectoryEntryService.resolveSymbolsFromStringList(
                    settingsService.getSettingValue(SettingsData.SETTING_LOCAL_SYMBOLS));
            Boolean localMatch = false;
            for (Symbol sym : localSymbols) {
                if (sym.owner == pickupLoc) {
                    localMatch = true;
                    break;
                }
            }
            if (pickupLoc && localMatch) {
                request.resolvedPickupLocation = pickupLoc
                request.pickupLocationSlug = pickupLoc.slug
            }
        }
    }
}
