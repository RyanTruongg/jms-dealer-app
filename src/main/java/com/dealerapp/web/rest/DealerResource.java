package com.dealerapp.web.rest;

import com.dealerapp.repository.DealerRepository;
import com.dealerapp.security.SecurityUtils;
import com.dealerapp.service.DealerQueryService;
import com.dealerapp.service.DealerService;
import com.dealerapp.service.criteria.DealerCriteria;
import com.dealerapp.service.dto.DealerDTO;
import com.dealerapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.dealerapp.domain.Dealer}.
 */
@RestController
@RequestMapping("/api")
public class DealerResource {

    private final Logger log = LoggerFactory.getLogger(DealerResource.class);

    private static final String ENTITY_NAME = "dealerappDealer";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DealerService dealerService;

    private final DealerRepository dealerRepository;

    private final DealerQueryService dealerQueryService;

    public DealerResource(DealerService dealerService, DealerRepository dealerRepository,
            DealerQueryService dealerQueryService) {
        this.dealerService = dealerService;
        this.dealerRepository = dealerRepository;
        this.dealerQueryService = dealerQueryService;
    }

    /**
     * {@code POST  /dealers} : Create a new dealer.
     *
     * @param dealerDTO the dealerDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
     *         body the new dealerDTO, or with status {@code 400 (Bad Request)} if
     *         the dealer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/dealers")
    public ResponseEntity<DealerDTO> createDealer(@RequestBody DealerDTO dealerDTO) throws URISyntaxException {
        log.debug("REST request to save Dealer : {}", dealerDTO);
        if (dealerDTO.getId() != null) {
            throw new BadRequestAlertException("A new dealer cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DealerDTO result = dealerService.save(dealerDTO);
        return ResponseEntity
                .created(new URI("/api/dealers/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    /**
     * {@code PUT  /dealers/:id} : Updates an existing dealer.
     *
     * @param id        the id of the dealerDTO to save.
     * @param dealerDTO the dealerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated dealerDTO,
     *         or with status {@code 400 (Bad Request)} if the dealerDTO is not
     *         valid,
     *         or with status {@code 500 (Internal Server Error)} if the dealerDTO
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/dealers/{id}")
    public ResponseEntity<DealerDTO> updateDealer(
            @PathVariable(value = "id", required = false) final Long id,
            @RequestBody DealerDTO dealerDTO) throws URISyntaxException {
        log.debug("REST request to update Dealer : {}, {}", id, dealerDTO);
        if (dealerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dealerDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dealerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        DealerDTO result = dealerService.save(dealerDTO);
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME,
                        dealerDTO.getId().toString()))
                .body(result);
    }

    /**
     * {@code PATCH  /dealers/:id} : Partial updates given fields of an existing
     * dealer, field will ignore if it is null
     *
     * @param id        the id of the dealerDTO to save.
     * @param dealerDTO the dealerDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the updated dealerDTO,
     *         or with status {@code 400 (Bad Request)} if the dealerDTO is not
     *         valid,
     *         or with status {@code 404 (Not Found)} if the dealerDTO is not found,
     *         or with status {@code 500 (Internal Server Error)} if the dealerDTO
     *         couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/dealers/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DealerDTO> partialUpdateDealer(
            @PathVariable(value = "id", required = false) final Long id,
            @RequestBody DealerDTO dealerDTO) throws URISyntaxException {
        log.debug("REST request to partial update Dealer partially : {}, {}", id, dealerDTO);
        if (dealerDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, dealerDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!dealerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DealerDTO> result = dealerService.partialUpdate(dealerDTO);

        return ResponseUtil.wrapOrNotFound(
                result,
                HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, dealerDTO.getId().toString()));
    }

    /**
     * {@code GET  /dealers} : get all the dealers.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
     *         of dealers in body.
     */
    @GetMapping("/dealers")
    public ResponseEntity<List<DealerDTO>> getAllDealers(
            DealerCriteria criteria,
            @org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get Dealers by criteria: {}", criteria);
        Page<DealerDTO> page = dealerQueryService.findByCriteria(criteria, pageable);
        System.out.println(SecurityUtils.getCurrentUserLogin());
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /dealers/count} : count all the dealers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count
     *         in body.
     */
    @GetMapping("/dealers/count")
    public ResponseEntity<Long> countDealers(DealerCriteria criteria) {
        log.debug("REST request to count Dealers by criteria: {}", criteria);
        return ResponseEntity.ok().body(dealerQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /dealers/:id} : get the "id" dealer.
     *
     * @param id the id of the dealerDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
     *         the dealerDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/dealers/{id}")
    public ResponseEntity<DealerDTO> getDealer(@PathVariable Long id) {
        log.debug("REST request to get Dealer : {}", id);
        Optional<DealerDTO> dealerDTO = dealerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(dealerDTO);
    }

    /**
     * {@code DELETE  /dealers/:id} : delete the "id" dealer.
     *
     * @param id the id of the dealerDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/dealers/{id}")
    public ResponseEntity<Void> deleteDealer(@PathVariable Long id) {
        log.debug("REST request to delete Dealer : {}", id);
        dealerService.delete(id);
        return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
                .build();
    }
}
