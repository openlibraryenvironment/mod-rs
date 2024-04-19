package org.olf.rs

import com.k_int.web.toolkit.SimpleLookupService
import grails.gorm.PagedResultList
import grails.gorm.multitenancy.CurrentTenant
import mod.rs.spring.service.model.GetPatronRequestParameter
import org.springframework.beans.factory.annotation.Autowired

@CurrentTenant
class PatronRequestService implements mod.rs.spring.service.PatronRequestService {

    @Autowired
    SimpleLookupService simpleLookupService

    @Override
    List<mod.rs.spring.model.PatronRequest> findRequests(GetPatronRequestParameter parameters) {
        PagedResultList<PatronRequest> requests = simpleLookupService.lookup(PatronRequest.class, parameters.getTerm())
        List<mod.rs.spring.model.PatronRequest> responseList = new ArrayList<>()
        requests.stream().forEach {
            r ->
                {
                    mod.rs.spring.model.PatronRequest result = new mod.rs.spring.model.PatronRequest()
                    result.setAuthor(r.author)
                    result.setHrid(r.hrid)
                    result.setId(r.id)
                    result.setPatronReference(r.patronReference)
                    result.setPatronIdentifier(r.patronIdentifier)
                    result.setTitle(r.title)
                    result.setRequestingInstitutionSymbol(r.requestingInstitutionSymbol)
                    result.setSupplyingInstitutionSymbol(r.supplyingInstitutionSymbol)
                    result.setSystemInstanceIdentifier(r.systemInstanceIdentifier)
                    result.setIsRequester(r.isRequester)
                    responseList.add(result)
                }
        }
        return responseList
    }
}