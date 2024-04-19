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
        final int offset = parameters.offset ?: 0
        final int perPage = Math.min(parameters.perPage ?: parameters.max ?: 10, 100)
        final int page = parameters.page ?: (offset ? (offset / perPage) + 1 : 1)
        PagedResultList<PatronRequest> requests
        if (parameters.stats) {
            requests = simpleLookupService.lookupWithStats(PatronRequest.class, parameters.term, perPage, page, parameters.filters, parameters.match, parameters.sort, null, null)
        } else {
            requests = simpleLookupService.lookup(PatronRequest.class, parameters.term, perPage, page, parameters.filters, parameters.match, parameters.sort, null)
        }

        List<mod.rs.spring.model.PatronRequest> responseList = new ArrayList<>()
        requests.stream().forEach {
            r ->responseList.add(toSpringPatronRequest(r))
        }
        return responseList
    }

    @Override
    mod.rs.spring.model.PatronRequest createRequests(String xOkapiTenant, String xOkapiToken, mod.rs.spring.model.PatronRequest patronRequest) {
        PatronRequest request = new PatronRequest()
        request.isRequester = patronRequest.getIsRequester()
        request.author = patronRequest.author
        request.hrid = patronRequest.hrid
        request.patronReference = patronRequest.patronReference
        request.patronIdentifier = patronRequest.patronIdentifier
        request.title = patronRequest.title
        request.requestingInstitutionSymbol = patronRequest.requestingInstitutionSymbol
        request.supplyingInstitutionSymbol = patronRequest.supplyingInstitutionSymbol
        request.systemInstanceIdentifier = patronRequest.systemInstanceIdentifier
        request.save(flush: true, failOnError: true)
        return toSpringPatronRequest(request)
    }

    mod.rs.spring.model.PatronRequest toSpringPatronRequest(PatronRequest patronRequest) {
        mod.rs.spring.model.PatronRequest result = new mod.rs.spring.model.PatronRequest()
        result.setAuthor(patronRequest.author)
        result.setHrid(patronRequest.hrid)
        result.setId(patronRequest.id)
        result.setPatronReference(patronRequest.patronReference)
        result.setPatronIdentifier(patronRequest.patronIdentifier)
        result.setTitle(patronRequest.title)
        result.setRequestingInstitutionSymbol(patronRequest.requestingInstitutionSymbol)
        result.setSupplyingInstitutionSymbol(patronRequest.supplyingInstitutionSymbol)
        result.setSystemInstanceIdentifier(patronRequest.systemInstanceIdentifier)
        result.setIsRequester(patronRequest.isRequester)
        return result
    }
}