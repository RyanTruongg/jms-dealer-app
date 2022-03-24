package com.dealerapp.service.mapper;

import com.dealerapp.domain.Dealer;
import com.dealerapp.service.dto.DealerDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Dealer} and its DTO {@link DealerDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface DealerMapper extends EntityMapper<DealerDTO, Dealer> {}
