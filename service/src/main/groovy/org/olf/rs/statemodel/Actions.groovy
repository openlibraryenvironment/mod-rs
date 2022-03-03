package org.olf.rs.statemodel;

public class Actions {

	// Responder actions
	static public final String ACTION_RESPONDER_ITEM_RETURNED                   = "itemReturned";
	static public final String ACTION_RESPONDER_RESPOND_YES                     = "respondYes";
	static public final String ACTION_RESPONDER_SUPPLIER_ADD_CONDITION          = "supplierAddCondition";
	static public final String ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY          = "supplierCannotSupply";
	static public final String ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE     = "supplierCheckInToReshare";
	static public final String ACTION_RESPONDER_SUPPLIER_CHECKOUT_OF_RESHARE    = "supplierCheckOutOfReshare";
	static public final String ACTION_RESPONDER_SUPPLIER_CONDITIONAL_SUPPLY     = "supplierConditionalSupply";
	static public final String ACTION_RESPONDER_SUPPLIER_MANUAL_CHECKOUT        = "supplierManualCheckout";
	static public final String ACTION_RESPONDER_SUPPLIER_MARK_CONDITIONS_AGREED = "supplierMarkConditionsAgreed";
	static public final String ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED           = "supplierMarkShipped";
	static public final String ACTION_RESPONDER_SUPPLIER_PRINT_PULL_SLIP        = "supplierPrintPullSlip";
	static public final String ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL      = "supplierRespondToCancel";
	
	// Requester Actions
	static public final String ACTION_REQUESTER_BORROWER_CHECK               = "borrowerCheck";
	static public final String ACTION_REQUESTER_BORROWER_CHECK_OVERRIDE      = "borrowerCheckOverride";
	static public final String ACTION_REQUESTER_CANCEL_LOCAL                 = "cancelLocal";
	static public final String ACTION_REQUESTER_FILL_LOCALLY                 = "fillLocally";
	static public final String ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY = "localSupplierCannotSupply";
	static public final String ACTION_REQUESTER_PATRON_RETURNED_ITEM         = "patronReturnedItem";
	static public final String ACTION_REQUESTER_REQUESTER_AGREE_CONDITIONS   = "requesterAgreeConditions";
	static public final String ACTION_REQUESTER_REQUESTER_CANCEL             = "requesterCancel";
	static public final String ACTION_REQUESTER_REQUESTER_MANUAL_CHECKIN     = "requesterManualCheckIn";
	static public final String ACTION_REQUESTER_REQUESTER_RECEIVED           = "requesterReceived";
	static public final String ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS  = "requesterRejectConditions";
	static public final String ACTION_REQUESTER_SHIPPED_RETURN               = "shippedReturn";

	// Both Requester and Responder actions	
	static public final String ACTION_MANUAL_CLOSE      = "manualClose";
	static public final String ACTION_MESSAGE           = "message";
	static public final String ACTION_MESSAGES_ALL_SEEN = "messagesAllSeen";
	static public final String ACTION_MESSAGE_SEEN      = "messageSeen";
}
