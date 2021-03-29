package org.olf.rs

import grails.gorm.MultiTenant;

/**
 * Counters to track various system states - Specifically, current loan and borrow levels, but perhaps
 * other things too
 */
class Counter implements MultiTenant<Counter> {

  String id
  String context
  String description
  Long value
  
  static constraints = {
        context (nullable : false, blank: false, unique: true)
    description (nullable : true,  blank: false)
          value (nullable : false)
  }

  static mapping = {
    id                     column : 'ct_id', generator: 'uuid2', length:36
    version                column : 'ct_version'
    context                column : 'ct_context'
    description            column : 'ct_description'
    value                  column : 'ct_value'
  }

  public static Long lookupValue(String context) {
    Long result = null;
    def ctr = Counter.findByContext(context)
    if ( ctr ) {
      result = ctr.value;
    }
    return result;
  }
}
