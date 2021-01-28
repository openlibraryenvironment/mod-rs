package org.olf.rs
import grails.gorm.MultiTenant

class Token implements MultiTenant<Token> {
  String id
  String token
  String previewValue

  static belongsTo = [ owner: TokenSection ]

  static mapping = {
              id column: 'tk_id', generator: 'uuid2', length:36
         version column: 'tk_version'
           token column: 'tk_token'
    previewValue column: 'tk_preview_value'
           owner column: 'tk_owner_fk'
  }

  static constraints = {
		 owner(nullable:false, blank:false);
	}
}