package com.dealerapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dealerapp.IntegrationTest;
import com.dealerapp.domain.Dealer;
import com.dealerapp.repository.DealerRepository;
import com.dealerapp.service.criteria.DealerCriteria;
import com.dealerapp.service.dto.DealerDTO;
import com.dealerapp.service.mapper.DealerMapper;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link DealerResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DealerResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/dealers";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private DealerRepository dealerRepository;

    @Autowired
    private DealerMapper dealerMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDealerMockMvc;

    private Dealer dealer;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Dealer createEntity(EntityManager em) {
        Dealer dealer = new Dealer().name(DEFAULT_NAME);
        return dealer;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Dealer createUpdatedEntity(EntityManager em) {
        Dealer dealer = new Dealer().name(UPDATED_NAME);
        return dealer;
    }

    @BeforeEach
    public void initTest() {
        dealer = createEntity(em);
    }

    @Test
    @Transactional
    void createDealer() throws Exception {
        int databaseSizeBeforeCreate = dealerRepository.findAll().size();
        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);
        restDealerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dealerDTO)))
            .andExpect(status().isCreated());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeCreate + 1);
        Dealer testDealer = dealerList.get(dealerList.size() - 1);
        assertThat(testDealer.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void createDealerWithExistingId() throws Exception {
        // Create the Dealer with an existing ID
        dealer.setId(1L);
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        int databaseSizeBeforeCreate = dealerRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDealerMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dealerDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllDealers() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        // Get all the dealerList
        restDealerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dealer.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }

    @Test
    @Transactional
    void getDealer() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        // Get the dealer
        restDealerMockMvc
            .perform(get(ENTITY_API_URL_ID, dealer.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(dealer.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }

    @Test
    @Transactional
    void getDealersByIdFiltering() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        Long id = dealer.getId();

        defaultDealerShouldBeFound("id.equals=" + id);
        defaultDealerShouldNotBeFound("id.notEquals=" + id);

        defaultDealerShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultDealerShouldNotBeFound("id.greaterThan=" + id);

        defaultDealerShouldBeFound("id.lessThanOrEqual=" + id);
        defaultDealerShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllDealersByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        // Get all the dealerList where name equals to DEFAULT_NAME
        defaultDealerShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the dealerList where name equals to UPDATED_NAME
        defaultDealerShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllDealersByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        // Get all the dealerList where name not equals to DEFAULT_NAME
        defaultDealerShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the dealerList where name not equals to UPDATED_NAME
        defaultDealerShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllDealersByNameIsInShouldWork() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        // Get all the dealerList where name in DEFAULT_NAME or UPDATED_NAME
        defaultDealerShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the dealerList where name equals to UPDATED_NAME
        defaultDealerShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllDealersByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        // Get all the dealerList where name is not null
        defaultDealerShouldBeFound("name.specified=true");

        // Get all the dealerList where name is null
        defaultDealerShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    void getAllDealersByNameContainsSomething() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        // Get all the dealerList where name contains DEFAULT_NAME
        defaultDealerShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the dealerList where name contains UPDATED_NAME
        defaultDealerShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    void getAllDealersByNameNotContainsSomething() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        // Get all the dealerList where name does not contain DEFAULT_NAME
        defaultDealerShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the dealerList where name does not contain UPDATED_NAME
        defaultDealerShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultDealerShouldBeFound(String filter) throws Exception {
        restDealerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(dealer.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));

        // Check, that the count call also returns 1
        restDealerMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultDealerShouldNotBeFound(String filter) throws Exception {
        restDealerMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restDealerMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingDealer() throws Exception {
        // Get the dealer
        restDealerMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewDealer() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        int databaseSizeBeforeUpdate = dealerRepository.findAll().size();

        // Update the dealer
        Dealer updatedDealer = dealerRepository.findById(dealer.getId()).get();
        // Disconnect from session so that the updates on updatedDealer are not directly saved in db
        em.detach(updatedDealer);
        updatedDealer.name(UPDATED_NAME);
        DealerDTO dealerDTO = dealerMapper.toDto(updatedDealer);

        restDealerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dealerDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(dealerDTO))
            )
            .andExpect(status().isOk());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        Dealer testDealer = dealerList.get(dealerList.size() - 1);
        assertThat(testDealer.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void putNonExistingDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().size();
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDealerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, dealerDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(dealerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().size();
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDealerMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(dealerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().size();
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDealerMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(dealerDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDealerWithPatch() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        int databaseSizeBeforeUpdate = dealerRepository.findAll().size();

        // Update the dealer using partial update
        Dealer partialUpdatedDealer = new Dealer();
        partialUpdatedDealer.setId(dealer.getId());

        restDealerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDealer.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDealer))
            )
            .andExpect(status().isOk());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        Dealer testDealer = dealerList.get(dealerList.size() - 1);
        assertThat(testDealer.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    void fullUpdateDealerWithPatch() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        int databaseSizeBeforeUpdate = dealerRepository.findAll().size();

        // Update the dealer using partial update
        Dealer partialUpdatedDealer = new Dealer();
        partialUpdatedDealer.setId(dealer.getId());

        partialUpdatedDealer.name(UPDATED_NAME);

        restDealerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDealer.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedDealer))
            )
            .andExpect(status().isOk());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
        Dealer testDealer = dealerList.get(dealerList.size() - 1);
        assertThat(testDealer.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    void patchNonExistingDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().size();
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDealerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, dealerDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(dealerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().size();
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDealerMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(dealerDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDealer() throws Exception {
        int databaseSizeBeforeUpdate = dealerRepository.findAll().size();
        dealer.setId(count.incrementAndGet());

        // Create the Dealer
        DealerDTO dealerDTO = dealerMapper.toDto(dealer);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDealerMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(dealerDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Dealer in the database
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDealer() throws Exception {
        // Initialize the database
        dealerRepository.saveAndFlush(dealer);

        int databaseSizeBeforeDelete = dealerRepository.findAll().size();

        // Delete the dealer
        restDealerMockMvc
            .perform(delete(ENTITY_API_URL_ID, dealer.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Dealer> dealerList = dealerRepository.findAll();
        assertThat(dealerList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
