package org.olf.rs.statemodel;

public class Actions {

    // Both Requester and Responder actions
    static public final String ACTION_INCOMING_ISO18626     = 'ISO18626';
    static public final String ACTION_ISO18626_NOTIFICATION = "ISO18626Notification";
    static public final String ACTION_MANUAL_CLOSE          = "manualClose";
    static public final String ACTION_MESSAGE               = "message";
    static public final String ACTION_MESSAGES_ALL_SEEN     = "messagesAllSeen";
    static public final String ACTION_MESSAGE_SEEN          = "messageSeen";

	// Responder actions
    static public final String ACTION_RESPONDER_ISO18626_CANCEL                              = "ISO18626Cancel";
    static public final String ACTION_RESPONDER_ISO18626_RECEIVED                            = "ISO18626Received";
    static public final String ACTION_RESPONDER_ISO18626_SHIPPED_RETURN                      = "ISO18626ShippedReturn";
    static public final String ACTION_RESPONDER_ISO18626_STATUS_REQUEST                      = "ISO18626StatusRequest";
	static public final String ACTION_RESPONDER_ITEM_RETURNED                   			 = "itemReturned";
	static public final String ACTION_RESPONDER_LOCAL_NOTE                                   = "localNote";
	static public final String ACTION_RESPONDER_RESPOND_YES                     			 = "respondYes";
	static public final String ACTION_RESPONDER_SUPPLIER_ADD_CONDITION          			 = "supplierAddCondition";
	static public final String ACTION_RESPONDER_SUPPLIER_ADD_URL_TO_DOCUMENT				 = "supplierAddURLToDocument";
	static public final String ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY          			 = "supplierCannotSupply";
	static public final String ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE     			 = "supplierCheckInToReshare";
	static public final String ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE    			 = "supplierCheckOutOfReshare";
	static public final String ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY     			 = "supplierConditionalSupply";
	static public final String ACTION_RESPONDER_SUPPLIER_MANUAL_CHECKOUT        			 = "supplierManualCheckout";
	static public final String ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED 			 = "supplierMarkConditionsAgreed";
	static public final String ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED           			 = "supplierMarkShipped";
	static public final String ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP        			 = "supplierPrintPullSlip";
	static public final String ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL      			 = "supplierRespondToCancel";
	static public final String ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE_AND_MARK_SHIPPED = "supplierCheckInToReshareAndSupplierMarkShipped"

	// Non-Returnable Responder Actions
	static public final String ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_ADD_URL_TO_DOCUMENT	 = "supplierAddURLToDocument";
	static public final String ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_PRINT_PULL_SLIP       = "supplierPrintPullSlip";
	static public final String ACTION_NONRETURNABLE_RESPONDER_RESPOND_YES                    = "respondYes";
	public static final String ACTION_NONRETURNABLE_RESPONDER_SUPPLIER_CANNOT_SUPPLY         = "supplierCannotSupply";
	public static final String ACTION_NONRETURNABLE_RESPONDER_ADD_CONDITION		     		 = 'supplierAddCondition';

	// CDL Responder Actions
	static public final String ACTION_RESPONDER_SUPPLIER_FILL_DIGITAL_LOAN = "supplierFillDigitalLoan";

	// SLNP Responder Actions
	static public final String ACTION_SLNP_RESPONDER_ABORT_SUPPLY                    = "slnpAbortSupply";
	static public final String ACTION_SLNP_RESPONDER_RESPOND_YES                     = "slnpRespondYes";
	static public final String ACTION_SLNP_RESPONDER_SUPPLIER_FILL_AND_MARK_SHIPPED  = "slnpSupplierFillAndMarkShipped";
	static public final String ACTION_SLNP_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE    = "slnpSupplierCheckOutOfReshare";

	// SLNP non returnable responder actions
	static public final String ACTION_SLNP_RESPONDER_SUPPLIER_SUPPLIES_DOCUMENT            = "slnpSupplierSuppliesDocument";
	static public final String ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_SUPPLIED = "manuallyMarkSupplied";
	static public final String ACTION_SLNP_NON_RETURNABLE_REQUESTER_MANUALLY_MARK_AVAILABLE = "manuallyMarkAvailable";

	// Requester Actions
	static public final String ACTION_REQUESTER_BORROWER_CHECK               	 = "borrowerCheck";
	static public final String ACTION_REQUESTER_BORROWER_CHECK_OVERRIDE      	 = "borrowerCheckOverride";
	static public final String ACTION_REQUESTER_CANCEL_LOCAL                 	 = "cancelLocal";
    static public final String ACTION_REQUESTER_EDIT                             = "edit";
	static public final String ACTION_REQUESTER_FILL_LOCALLY                 	 = "fillLocally";
    static public final String ACTION_REQUESTER_ISO18626_CANCEL_RESPONSE         = "ISO18626CancelResponse";
    static public final String ACTION_REQUESTER_ISO18626_RENEW_RESPONSE          = "ISO18626RenewResponse";
    static public final String ACTION_REQUESTER_ISO18626_REQUEST_RESPONSE        = "ISO18626RequestResponse";
    static public final String ACTION_REQUESTER_ISO18626_STATUS_CHANGE           = "ISO18626StatusChange";
    static public final String ACTION_REQUESTER_ISO18626_STATUS_REQUEST_RESPONSE = "ISO18626StatusRequestResponse";
	static public final String ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY 	 = "localSupplierCannotSupply";
	static public final String ACTION_REQUESTER_PATRON_RETURNED_ITEM         	 = "patronReturnedItem";
	static public final String ACTION_REQUESTER_REQUESTER_AGREE_CONDITIONS   	 = "requesterAgreeConditions";
	static public final String ACTION_REQUESTER_REQUESTER_CANCEL             	 = "requesterCancel";
	static public final String ACTION_REQUESTER_APPROVE_DUPLICATE                = "requesterApproveDuplicate";
	static public final String ACTION_REQUESTER_FORCE_VALIDATE                   = "requesterForceValidate";
	static public final String ACTION_REQUESTER_BYPASS_VALIDATION				 = "requesterBypassValidation";
	static public final String ACTION_REQUESTER_CANCEL_DUPLICATE                 = "requesterCancelDuplicate";
	static public final String ACTION_REQUESTER_RETRY_REQUEST			         = "requesterRetryRequest";
	static public final String ACTION_REQUESTER_RETRY_VALIDATION			     = "requesterRetryValidation";
	static public final String ACTION_REQUESTER_MARK_END_OF_ROTA_REVIEWED        = "requesterMarkEndOfRotaReviewed";
	static public final String ACTION_REQUESTER_REQUESTER_MANUAL_CHECKIN     	 = "requesterManualCheckIn";
	static public final String ACTION_REQUESTER_REQUESTER_RECEIVED           	 = "requesterReceived";
	static public final String ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS  	 = "requesterRejectConditions";
	static public final String ACTION_REQUESTER_REREQUEST                        = "rerequest";
	static public final String ACTION_REQUESTER_SHIPPED_RETURN               	 = "shippedReturn";
	static public final String ACTION_REQUESTER_PATRON_RETURNED_ITEM_AND_SHIPPED = "patronReturnedItemAndShippedReturn";
	static public final String ACTION_REQUESTER_COMPLETE_REQUEST				 = "requesterCompleteRequest";

	// Nonreturnable Requester Actions
	static public final String ACTION_NONRETURNABLE_REQUESTER_BYPASS_VALIDATION  = "requesterBypassValidation";
	static public final String ACTION_NONRETURNABLE_REQUESTER_RETRY_VALIDATION   = "requesterRetryValidation";
	static public final String ACTION_NONRETURNABLE_REQUESTER_REQUESTER_CANCEL   = "requesterCancel";
	static public final String ACTION_NONRETURNABLE_REQUESTER_COMPLETE_REQUEST   = "requesterCompleteRequest";
	static public final String ACTION_NONRETURNABLE_REQUESTER_MARK_END_OF_ROTA_REVIEWED = "requesterMarkEndOfRotaReviewed";
	static public final String ACTION_NONRETURNABLE_REQUESTER_REREQUEST          = "rerequest";
	static public final String ACTION_NONRETURNABLE_REQUESTER_FILL_LOCALLY       = "fillLocally";
	static public final String ACTION_NONRETURNABLE_REQUESTER_CANCEL_LOCAL       = "cancelLocal";
	static public final String ACTION_NONRETURNABLE_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY = "localSupplierCannotSupply";

	// SLNP Requester Actions
	static public final String ACTION_SLNP_REQUESTER_HANDLE_ABORT                       = "slnpHandleAbort";
	static public final String ACTION_SLNP_REQUESTER_REQUESTER_RECEIVED                 = "slnpRequesterReceived";
	static public final String ACTION_SLNP_REQUESTER_MARK_ITEM_LOST                     = "slnpMarkItemLost";
	static public final String ACTION_SLNP_NON_RETURNABLE_REQUESTER_REQUESTER_RECEIVED  = "slnpNonReturnableRequesterReceived";

    // A special action for both sides that allows us to undo am action if the action allows us to
    static public final String ACTION_UNDO = "undo";
}
