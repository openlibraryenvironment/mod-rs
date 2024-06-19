package org.olf.rs

import com.sun.xml.bind.marshaller.NamespacePrefixMapper

class IllNamespacePrefixMapper extends NamespacePrefixMapper {

    @Override
    String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if ("http://illtransactions.org/2013/iso18626" == namespaceUri) {
            return "ill";
        }
        return suggestion;
    }
}