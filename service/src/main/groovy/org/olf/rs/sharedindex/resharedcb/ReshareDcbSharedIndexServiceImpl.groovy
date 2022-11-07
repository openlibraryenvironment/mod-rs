package org.olf.rs.sharedindex.jiscdiscover

import org.olf.rs.SharedIndexActions;
import org.olf.rs.AvailabilityStatement;
import groovy.util.logging.Slf4j


@Slf4j
public class ReshareDcbSharedIndexServiceImpl implements SharedIndexActions {

  public List<AvailabilityStatement> findAppropriateCopies(Map description) {
    log.debug("ReshareDcbSharedIndexServiceImpl::findAppropriateCopies(${description})");
    return null;
  }

  public List<String> fetchSharedIndexRecords(Map description) {
    log.debug("ReshareDcbSharedIndexServiceImpl::fetchSharedIndexRecords(${description})");
    return null;
  }

}

