

The format of each entry is

## INITIAL_STATE - description
###action
####Possible RESULT_STATE (Given action scenario a)
####Possible RESULT_STATE (Given action scenario b)
####Possible RESULT_STATE (Given action scenario c)

# Requester

## REQ_IDLE - A request has been received, but not yet validated
### validate
#### REQ_VALIDATED
#### REQ_INVALID_PATRON

##REQ_VALIDATED - The request was valid
### sourceItem
#### REQ_SOURCING_ITEM - We weren't able to automatically find a copy of this item, and there was an error we did not expect
#### REQ_SUPPLIER_IDENTIFIED - We located at least one possible supplier
#### REQ_END_OF_ROTA - We were not able to locate an item

## REQ_INVALID_PATRON - Patron not valid or not in good standing

## REQ_SOURCING_ITEM - currently searching for the item - requests should NOT get stuck in this state

## REQ_SUPPLIER_IDENTIFIED - at least one possible supplier identified
### sendToNextLender
#### REQ_REQUEST_SENT_TO_SUPPLIER
#### REQ_END_OF_ROTA

## REQ_REQUEST_SENT_TO_SUPPLIER
## REQ_UNABLE_TO_CONTACT_SUPPLIER
## REQ_ITEM_SHIPPED
## REQ_BORROWING_LIBRARY_RECEIVED
## REQ_AWAITING_RETURN_SHIPPING
## REQ_BORROWER_RETURNED
## REQ_REQUEST_COMPLETE
## REQ_PENDING
## REQ_WILL_SUPPLY
## REQ_EXPECTS_TO_SUPPLY
## REQ_UNFILLED
## REQ_SHIPPED
## REQ_CHECKED_IN
## REQ_AWAIT_RETURN_SHIPPING
## REQ_END_OF_ROTA
## REQ_CANCELLED
## REQ_ERROR


# Responder / Supplier

## RES_IDLE - A request has been received, but is not yet in process
### message
#### RES_IDLE
### respondYes
#### RES_NEW_AWAIT_PULL_SLIP
### supplierCannotSupply
#### RES_UNFILLED
### dummyAction
#### RES_IDLE

## RES_NEW_AWAIT_PULL_SLIP - An item has been located and we expect to supply, waiting to print pull slip
## supplierPrintPullSlip
### RES_AWAIT_PICKING
## message
### RES_NEW_AWAIT_PULL_SLIP

## RES_AWAIT_PICKING - Pull slip printed, waiting to be picked
### supplierCheckInToReshare
#### RES_CHECKED_IN_TO_RESHARE
#### RES_AWAIT_LMS_CHECKOUT
#### RES_AWAIT_PROXY_BORROWER

## RES_AWAIT_PROXY_BORROWER - Unable to complete check-in to reshare, no PROXY borrower for borrowing library

## RES_CHECKED_IN_TO_RESHARE - Item checked out of host LMS and into Re:Share

## RES_AWAIT_LMS_CHECKOUT - There was a problem checking the item out of the host LMS

## RES_AWAIT_SHIP - Item picked and is awaiting shipping

## RES_HOLD_PLACED - The item was not availabe on the shelf, but a hold has been placed

## RES_UNFILLED - The request could not be fulfilled, we answered no

## RES_NOT_SUPPLIED - This seems the same as UNFILLED

## RES_ITEM_SHIPPED - The item has been shipped to the borrowing library

## RES_ITEM_RETURNED - The item has been received back from the borrowing library

## RES_COMPLETE - The request is complete

## RES_ERROR - There was an unexpected error that probably needs support to resolve

