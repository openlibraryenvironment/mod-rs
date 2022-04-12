package org.olf.rs;

import org.olf.okapi.modules.directory.Symbol;

import com.k_int.web.toolkit.custprops.CustomProperty

import org.olf.okapi.modules.directory.DirectoryEntry;
import groovy.util.logging.Slf4j


@Slf4j
/* This service will hold some statics and some methods dedicated to parsing information out of a DirectoryEntry */
public class DirectoryEntryService {
  /* This collection of statics allows us to update in one place when things change.
   * TODO we probably ought to collect some of these keys/values here if they're likely to change.
   * The change from "ill.loan_policy" to "policy.ill.loan_policy" broke some things.
   */
  static final ill_policy_custprop_key = 'policy.ill.loan_policy'



  boolean directoryEntryIsLending(DirectoryEntry dirEnt) {
    def entry_loan_policy = parseCustomPropertyValue(dirEnt, ill_policy_custprop_key)

    log.debug("directoryEntry(${dirEnt}) loan_policy : ${entry_loan_policy}");
    return (
      entry_loan_policy == null ||
      entry_loan_policy == 'lending_all_types' ||
      entry_loan_policy == 'lending_physical_only'
    )
  }

  String parseCustomPropertyValue(DirectoryEntry dirEnt, String key) {
    String returnVal = null;
    if (dirEnt && key) {
      returnVal = dirEnt.customProperties?.value?.find { it.definition.name==key }?.value
    }

    returnVal    
  }

  // Methods to parse a string Symbol representation and return the Symbol itself
  public Symbol resolveCombinedSymbol(String combinedString) {
    Symbol result = null;
    if ( combinedString != null ) {
      String[] name_components = combinedString.split(':');
      if ( name_components.length == 2 ) {
        result = resolveSymbol(name_components[0], name_components[1]);
      }
    }
    return result;
  }

  public Symbol resolveSymbol(String authorty, String symbol) {
    Symbol result = null;
    List<Symbol> symbol_list = Symbol.executeQuery('select s from Symbol as s where s.authority.symbol = :authority and s.symbol = :symbol',
                                                   [authority:authorty?.toUpperCase(), symbol:symbol?.toUpperCase()]);
    if ( symbol_list.size() == 1 ) {
      result = symbol_list.get(0);
    }

    return result;
  }

  /* 
   * DirectoryEntries have a property customProperties of class com.k_int.web.toolkit.custprops.types.CustomPropertyContainer
   * In turn, the CustomPropertyContainer hasMany values of class com.k_int.web.toolkit.custprops.CustomProperty
   * CustomProperties have a CustomPropertyDefinition, where the name lives, so we filter the list to find the matching custprop
   */
  public CustomProperty extractCustomPropertyFromDirectoryEntry(DirectoryEntry de, String cpName) {
    if (!de || ! cpName) {
      return null
    }
    def custProps = de.customProperties?.value ?: []
    CustomProperty cp = (custProps.find {custProp -> custProp.definition?.name == cpName})
    return cp
  }

}
