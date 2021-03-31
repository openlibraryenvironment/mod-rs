package org.olf.rs

import com.k_int.okapi.OkapiClient
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import groovy.json.JsonSlurper

/**
 * This service has responsibility for taking a patron request and creating a viable list of
 * possible responders. 
 * 
 * this service hides the specific implementations of the configured local request routing strategy
 * this may be a looking in a shared index, a static list of responders or some other strategy
 */
public class RequestRouterService {
}
