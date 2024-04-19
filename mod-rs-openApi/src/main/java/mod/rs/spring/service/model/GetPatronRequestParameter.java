package mod.rs.spring.service.model;

import java.util.List;

public class GetPatronRequestParameter {
    String xOkapiTenant;
    String term;
    List<String> match;
    List<String> filters;
    List<String> sort;
    Integer max;
    Integer perPage;
    Integer offset;
    Integer page;
    Boolean stats;
    String xOkapiToke;

    public GetPatronRequestParameter(String xOkapiTenant, String term, List<String> match, List<String> filters, List<String> sort, Integer max, Integer perPage, Integer offset, Integer page, Boolean stats, String xOkapiToke) {
        this.xOkapiTenant = xOkapiTenant;
        this.term = term;
        this.match = match;
        this.filters = filters;
        this.sort = sort;
        this.max = max;
        this.perPage = perPage;
        this.offset = offset;
        this.page = page;
        this.stats = stats;
        this.xOkapiToke = xOkapiToke;
    }

    public String getxOkapiTenant() {
        return xOkapiTenant;
    }

    public String getTerm() {
        return term;
    }

    public List<String> getMatch() {
        return match;
    }

    public List<String> getFilters() {
        return filters;
    }

    public List<String> getSort() {
        return sort;
    }

    public Integer getMax() {
        return max;
    }

    public Integer getPerPage() {
        return perPage;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getPage() {
        return page;
    }

    public Boolean getStats() {
        return stats;
    }

    public String getxOkapiToke() {
        return xOkapiToke;
    }
}
