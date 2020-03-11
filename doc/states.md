

The format of each entry is

*  INITIAL_STATE - description
    * action
        * Possible RESULT_STATE (Given action scenario a)
        * Possible RESULT_STATE (Given action scenario b)
        * Possible RESULT_STATE (Given action scenario c)

# Requester

* REQ_IDLE - A request has been received, but not yet validated
    * [SYSTEM_PROCESS] validate
        * REQ_VALIDATED
        * REQ_INVALID_PATRON

* REQ_VALIDATED - The request was valid
    * [SYSTEM_PROCESS] sourceItem
        * REQ_SOURCING_ITEM - We weren't able to automatically find a copy of this item, and there was an error we did not expect
        * REQ_SUPPLIER_IDENTIFIED - We located at least one possible supplier
        * REQ_END_OF_ROTA - We were not able to locate an item

* REQ_INVALID_PATRON - Patron not valid or not in good standing

* REQ_SOURCING_ITEM - currently searching for the item - requests should NOT get stuck in this state

* REQ_SUPPLIER_IDENTIFIED - at least one possible supplier identified
    * [SYSTEM_PROCESS] sendToNextLender
        * REQ_REQUEST_SENT_TO_SUPPLIER
        * REQ_END_OF_ROTA

* REQ_REQUEST_SENT_TO_SUPPLIER
    * [PROTOCOL_EVENT] incomingProtocolMessage - StatusChange
        * REQ_UNFILLED - statusInfo.status = Unfilled
        * REQ_EXPECTS_TO_SUPPLY - statusInfo.status = ExpectToSupply

* REQ_OVERDUE
* REQ_RECALLED

* REQ_UNABLE_TO_CONTACT_SUPPLIER

* REQ_ITEM_SHIPPED
    * requesterReceived
        * REQ_BORROWING_LIBRARY_RECEIVED

* REQ_BORROWING_LIBRARY_RECEIVED
    * requesterManualCheckIn
        * REQ_CHECKED_IN

* REQ_AWAITING_RETURN_SHIPPING
    * shippedReturn
        * 

* REQ_BORROWER_RETURNED
* REQ_REQUEST_COMPLETE
* REQ_PENDING
* REQ_WILL_SUPPLY
* REQ_EXPECTS_TO_SUPPLY
    * [PROTOCOL_EVENT] incomingProtocolMessage - StatusChange:Loaned
        * REQ_SHIPPED

* REQ_UNFILLED - A supplier responded unfilled 
    * [SYSTEM_PROCESS] sendToNextLender - Try the next lender in the rota, or mark END_OF_ROTA
        * REQ_REQUEST_SENT_TO_SUPPLIER
        * REQ_END_OF_ROTA

* REQ_SHIPPED
* REQ_CHECKED_IN
    * patronReturnedItem
        * REQ_AWAITING_RETURN_SHIPPING

* REQ_AWAITING_RETURN_SHIPPING
* REQ_END_OF_ROTA
* REQ_CANCELLED
* REQ_ERROR


# Responder / Supplier

* RES_IDLE - A request has been received, but is not yet in process
    * [Manual] message
        * RES_IDLE
    * [Manual] respondYes
        * RES_NEW_AWAIT_PULL_SLIP
    * [Manual] supplierCannotSupply
        * RES_UNFILLED
    * [SYSTEM_PROCESS] dummyAction
        * RES_IDLE
    * [SYSTEM_PROCESSS] autoRespond - If configured, the system will attempt to auto respond to an incoming request
        * RES_NEW_AWAIT_PULL_SLIP - We located a copy, updated the location and are awaitng pull-slip printing
        * RES_UNFILLED - Unable to locate a copy

* RES_NEW_AWAIT_PULL_SLIP - An item has been located and we expect to supply, waiting to print pull slip
    * [Manual] supplierPrintPullSlip
        * RES_AWAIT_PICKING
* message
    * RES_NEW_AWAIT_PULL_SLIP

* RES_AWAIT_PICKING - Pull slip printed, waiting to be picked
    * [Manual] supplierCheckInToReshare
        * RES_CHECKED_IN_TO_RESHARE
        * RES_AWAIT_LMS_CHECKOUT
        * RES_AWAIT_PROXY_BORROWER

* RES_AWAIT_PROXY_BORROWER - Unable to complete check-in to reshare, no PROXY borrower for borrowing library

* RES_CHECKED_IN_TO_RESHARE - Item checked out of host LMS and into Re:Share
    * supplierMarkShipped
        * RES_ITEM_SHIPPED

* RES_AWAIT_LMS_CHECKOUT - There was a problem checking the item out of the host LMS

* RES_AWAIT_SHIP - Item picked and is awaiting shipping
    * supplierMarkShipped
        * RES_ITEM_SHIPPED

* RES_HOLD_PLACED - The item was not availabe on the shelf, but a hold has been placed

* RES_UNFILLED - The request could not be fulfilled, we answered no

* RES_NOT_SUPPLIED - This seems the same as UNFILLED

* RES_ITEM_SHIPPED - The item has been shipped to the borrowing library

* RES_ITEM_RETURNED - The item has been received back from the borrowing library
    * supplierCheckout
        * RES_COMPLETE

* RES_COMPLETE - The request is complete

* RES_ERROR - There was an unexpected error that probably needs support to resolve

