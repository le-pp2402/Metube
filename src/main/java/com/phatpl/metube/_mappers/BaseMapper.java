package com.phatpl.metube._mappers;


import org.springframework.data.domain.Page;

import java.util.List;

public interface BaseMapper<E, DTO> {
    DTO toDTO(E entity);

    E toEntity(DTO dto);

    List<DTO> toListDTO(List<E> e);

    List<DTO> toListDTO(Page<E> e);

    List<E> toListEntity(List<DTO> dto);

}