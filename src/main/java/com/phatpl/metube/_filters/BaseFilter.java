package com.phatpl.metube._filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.phatpl.metube._utils.Constant;

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
