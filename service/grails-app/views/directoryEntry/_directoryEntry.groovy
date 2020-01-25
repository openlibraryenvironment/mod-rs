import groovy.transform.*
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.ServiceAccount

json g.render(directoryEntry, [expand: [ 'status'], excludes:[ 'tags','symbols', 'services', 'parent', 'units' ]]) {
  tagSummary directoryEntry.getTagSummary()
  symbolSummary directoryEntry.getSymbolSummary()
  fullyQualifiedName directoryEntry.getFullyQualifiedName()
}

