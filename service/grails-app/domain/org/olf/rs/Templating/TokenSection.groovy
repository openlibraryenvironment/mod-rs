package org.olf.rs
import grails.gorm.MultiTenant

import org.olf.rs.Token

class TokenSection implements MultiTenant<TokenSection> {
  String id
  String name

  static hasMany = [
    tokens: Token
  ]

  static mapping = {
        id column: 'tks_id', generator: 'uuid2', length:36
   version column: 'tks_version'
      name column: 'tks_name'
    tokens cascade: 'all-delete-orphan'
  }

  static constraints = {
    name(nullable:false, blank:false, unique: true)
  }
}