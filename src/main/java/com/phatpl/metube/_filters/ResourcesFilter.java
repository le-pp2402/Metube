package com.phatpl.metube._filters;

import com.meilisearch.sdk.SearchRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResourcesFilter extends BaseFilter {
    Integer page;
    String q;

    public Integer getPage() {
        if (page <= 0) return 1;
        return page;
    }

    public String getQ() {
        if (q == null || q.isEmpty()) return "";
        return q;
    }

    public SearchRequest getSearchRequest() {
        return SearchRequest.builder()
                .q(this.getQ())
                .filter(new String[]{"private = false"})
                .sort(new String[]{
                        "created:desc"
                })
                .hitsPerPage(5)
                .page(this.getPage())
                .build();
    }
}
