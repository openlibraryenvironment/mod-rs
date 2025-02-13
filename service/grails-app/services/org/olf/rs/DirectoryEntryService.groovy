package org.olf.rs;

import org.olf.okapi.modules.directory.DirectoryEntry;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.constants.Directory;

import com.k_int.web.toolkit.custprops.CustomProperty;

import groovy.util.logging.Slf4j


@Slf4j
/* This service will hold some statics and some methods dedicated to parsing information out of a DirectoryEntry */
public class DirectoryEntryService {

  boolean directoryEntryIsLending(DirectoryEntry dirEnt) {
    def entry_loan_policy = parseCustomPropertyValue(dirEnt, Directory.KEY_ILL_POLICY_LOAN);

    log.debug("directoryEntry(${dirEnt}) loan_policy : ${entry_loan_policy}");
    return (
      entry_loan_policy == null ||
      entry_loan_policy == Directory.LOAN_POLICY_LENDING_ALL ||
      entry_loan_policy == Directory.LOAN_POLICY_LENDING_PHYSICAL_ONLY
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
  public static Symbol resolveCombinedSymbol(String combinedString) {
    Symbol result = null;
    if ( combinedString != null ) {
      String[] name_components = combinedString.split(':');
      if ( name_components.length > 1 ) {
        result = resolveSymbol(name_components[0], name_components[1..-1].join(':'));
      } else {
        log.warn("Unexpected lack of colon-separated authority when attempting to parse symbol ${combinedString}");
      }
    } else {
      log.warn("resolveCombinedSymbol called with NULL string");
    }
    return result;
  }

  public static Symbol resolveSymbol(String authority, String symbol) {
    Symbol result = null;
    List<Symbol> symbol_list = Symbol.executeQuery('select s from Symbol as s where s.authority.symbol = :authority and s.symbol = :symbol',
                                                   [authority:authority?.toUpperCase(), symbol:symbol?.toUpperCase()]);
    if ( symbol_list.size() == 1 ) {
      result = symbol_list.get(0);
    } else {
      log.warn("Missing or multiple symbol match for : ${authority}:${symbol} (${symbol_list?.size()})");
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

  /*
    Given a comma-separated list of qualified symbol names, return a list of all symbols that can be resolved
   */
  public static List<Symbol> resolveSymbolsFromStringList(String symbolListString) {
    List<Symbol> results = [];
    //Split by commas
    List<String> symbolList = symbolListString.split(",");
    for ( String sub : symbolList ) {
      List<String> parts = sub.split(":", 2); //Split into no more than 2 segments, allowing colons in symbol names
      if (parts?.size() == 2) {
        Symbol resolvedSymbol = resolveSymbol(parts[0], parts[1]);
        if (resolvedSymbol != null) {
          results.add(resolvedSymbol);
        }
      } else {
        log.warn("Unable to split ${sub} into authority and symbol name");
      }
    }
    return results;
  }

}
