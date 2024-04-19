package mod.rs.spring.service;

import mod.rs.spring.model.PatronRequest;
import mod.rs.spring.service.model.GetPatronRequestParameter;

import java.util.List;

public interface PatronRequestService {

    List<PatronRequest> findRequests(GetPatronRequestParameter parameters);

    PatronRequest createRequests(String xOkapiTenant, String xOkapiToken, PatronRequest patronRequest);
}
