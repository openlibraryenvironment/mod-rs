package org.olf.rs.statemodel.actions;

import org.olf.rs.DirectoryEntryService;
import org.olf.rs.HostLMSService
import org.olf.rs.PatronRequest;
import org.olf.rs.RequestVolume;
import org.olf.rs.lms.HostLMSActions;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.custprops.CustomProperty;
import com.k_int.web.toolkit.refdata.RefdataValue;
import com.k_int.web.toolkit.settings.AppSetting;

/**
 * Action that occurs when the responder checks the item into reshare from the LMS
 * @author Chas
 *
 */
public class ActionResponderSupplierCheckInToReshareAndSupplierMarkShippedService extends ActionResponderService {

    private static final String VOLUME_STATUS_AWAITING_LMS_CHECK_OUT = 'awaiting_lms_check_out';

    private static final String REASON_SPOOFED = 'spoofed';

    private static final String[] TO_STATES = [
        Status.RESPONDER_ITEM_SHIPPED,
    ];

    HostLMSService hostLMSService;
    DirectoryEntryService directoryEntryService;

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE_AND_MARK_SHIPPED);
    }

    @Override
    String[] toStates() {
        return(TO_STATES);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        
    }
}
