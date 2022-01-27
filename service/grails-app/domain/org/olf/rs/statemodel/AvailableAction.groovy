package org.olf.rs.statemodel

import org.apache.commons.logging.LogFactory;

import grails.gorm.MultiTenant;
import grails.util.Holders;;
/**
 *
 */
class AvailableAction implements MultiTenant<AvailableAction> {
  private static final logger = LogFactory.getLog(this);
	
  public static String TRIGGER_TYPE_MANUAL = "M";
  public static String TRIGGER_TYPE_SYSTEM = "S";
  
  String id
  StateModel model
  Status fromState
  String actionCode

  // [S]ystem / [M]anual
  String triggerType

  // [S]ervice / [C]losure / [N]one
  String actionType

  String actionBody

  /** Holds map of the action to the bean that will do the processing for this action */
  static Map serviceActions = [ : ];

  static constraints = {
             model (nullable: false)
         fromState (nullable: false)
        actionCode (nullable: false, blank:false)
       triggerType (nullable: true, blank:false)
        actionType (nullable: true, blank:false)
        actionBody (nullable: true, blank:false)
  }

  static mapping = {
                     id column : 'aa_id', generator: 'uuid2', length:36
                version column : 'aa_version'
                  model column : 'aa_model'
              fromState column : 'aa_from_state'
             actionCode column : 'aa_action_code'
            triggerType column : 'aa_trigger_type'
             actionType column : 'aa_action_type'
             actionBody column : 'aa_action_body'
  }


  public static AvailableAction ensure(String model, String state, String action, String triggerType=null, String actionType=null, String actionBody=null) {

    AvailableAction result = null;

    StateModel sm = StateModel.findByShortcode(model);
    if ( sm ) {
      Status s = Status.findByOwnerAndCode(sm, state);
      if ( s ) {
        result = AvailableAction.findByModelAndFromStateAndActionCode(sm,s,action) ?: 
                      new AvailableAction(
                                          model:sm, 
                                          fromState:s, 
                                          actionCode:action,
                                          triggerType: triggerType,
                                          actionType: actionType,
                                          actionBody: actionBody).save(flush:true, failOnError:true);
      }
    }
    return result;

  }

  	static AbstractAction getServiceAction(String actionCode, boolean isRequester) {
		// Get gold of the state model
		StateModel stateModel = StateModel.getStateModel(isRequester);

		// Determine the bean name, if we had a separate action table we could store it as a transient against that		
		String beanName = "action" + stateModel.shortcode.capitalize() + actionCode.capitalize() + "Service";

		// Get hold of the bean and store it in our map, if we previously havn't been through here
		if (serviceActions[beanName] == null) {
			// Now setup the link to the service action that actually does the work
			try {
				serviceActions[beanName] = Holders.grailsApplication.mainContext.getBean(beanName);
			} catch (Exception e) {
				logger.error("Unable to locate action bean: " + beanName);
			}
		}
		return(serviceActions[beanName]);
	}


  public String toString() {
    return "AvailableAction(${id}) ${actionCode} ${triggerType} ${actionType} ${actionBody?.take(40)}".toString()
  }
}


