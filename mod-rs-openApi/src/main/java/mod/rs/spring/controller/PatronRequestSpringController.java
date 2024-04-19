package mod.rs.spring.controller;

import mod.rs.spring.api.RsApi;
import mod.rs.spring.model.PatronRequest;
import mod.rs.spring.service.PatronRequestService;
import mod.rs.spring.service.model.GetPatronRequestParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
public class PatronRequestSpringController implements RsApi {

    private final PatronRequestService patronRequestService;

    @Autowired
    public PatronRequestSpringController(PatronRequestService patronRequestService) {
        this.patronRequestService = patronRequestService;
    }

    @Override
    public ResponseEntity<List<PatronRequest>> getPatronRequests(
            @NotNull String xOkapiTenant,
            @Valid String term,
            @Valid List<String> match,
            @Valid List<String> filters,
            @Valid List<String> sort,
            @Valid Integer max,
            @Valid Integer perPage,
            @Valid Integer offset,
            @Valid Integer page,
            @Valid Boolean stats,
            String xOkapiToken
    ){
        GetPatronRequestParameter parameter = new GetPatronRequestParameter(xOkapiToken, term, match, filters, sort, max, perPage, offset, page, stats, xOkapiToken);
        return ResponseEntity.ok(patronRequestService.findRequests(parameter));
    }

    @Override
    public ResponseEntity<PatronRequest> createPatronRequest(
            @NotNull String xOkapiTenant,
            @Valid PatronRequest body,
            String xOkapiToken
    ) {
        return new ResponseEntity<>(patronRequestService.createRequests(xOkapiTenant, xOkapiToken, body), HttpStatus.CREATED);
    }

}
