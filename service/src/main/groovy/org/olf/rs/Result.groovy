package org.olf.rs

import org.olf.rs.statemodel.ActionResult;

/**
 * Holds the outcome of some processing that went on in a service, that the controller can pass back to the caller
 */
public class Result {
    /** The result of performing what was requested */
    public ActionResult result = ActionResult.SUCCESS;

    /** The id that was acted upon or the result of any processing, if any */
    public String id;

    /** If we were unsuccessful, the error code to be returned (not HTTP) */
    public String errorCode;

    /** Any messages that will be returned to the caller */
    public List<String> messages = new ArrayList<String>();

    /** Contains data that will be passed back to the caller */
    public Map responseResult = [ : ];
}
