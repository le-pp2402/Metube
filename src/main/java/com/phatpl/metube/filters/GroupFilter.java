package com.phatpl.metube.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
@AllArgsConstructor
public class GroupFilter extends BaseFilter {
    Integer page;

    @Override
    public Pageable getPageable() {
        if (page == null || page <= 0) page = 1;
        super.setPageNumber(page - 1);
        return super.getPageable();
    }
}
