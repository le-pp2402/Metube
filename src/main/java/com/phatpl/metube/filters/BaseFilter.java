package com.phatpl.metube.filters;

import com.phatpl.metube.utils.Constant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseFilter {
    private final Integer pageSize = Constant.PAGE_SIZE;
    private Integer pageNumber;
    public Pageable getPageable() {
        return PageRequest.of(pageNumber, pageSize);
    }
}
