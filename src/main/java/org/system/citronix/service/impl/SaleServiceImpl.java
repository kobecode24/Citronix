package org.system.citronix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.system.citronix.dto.request.SaleRequest;
import org.system.citronix.dto.response.SaleResponse;
import org.system.citronix.entity.Harvest;
import org.system.citronix.entity.Sale;
import org.system.citronix.enums.SeasonEnum;
import org.system.citronix.exception.BusinessException;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.mapper.SaleMapper;
import org.system.citronix.repository.HarvestRepository;
import org.system.citronix.repository.SaleRepository;
import org.system.citronix.service.SaleService;
import org.system.citronix.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SaleServiceImpl implements SaleService {
    private final SaleRepository saleRepository;
    private final HarvestRepository harvestRepository;
    private final SaleMapper saleMapper;

    @Override
    public SaleResponse createSale(SaleRequest request) {
        Harvest harvest = harvestRepository.findById(request.getHarvestId())
                .orElseThrow(() -> new ResourceNotFoundException("Harvest not found with id: " + request.getHarvestId()));

        ValidationUtil.validateSaleQuantity(harvest);

        Sale sale = saleMapper.toEntity(request);
        sale.setHarvest(harvest);

        // Validate sale date
        ValidationUtil.validateSaleDate(sale, harvest);

        if (harvest.isSold()) {
            throw new BusinessException(
                    String.format("Harvest %d has already been sold", harvest.getId())
            );
        }


        // Save and return
        Sale savedSale = saleRepository.save(sale);

        // Force load of harvest data for revenue calculation
        savedSale = saleRepository.findById(savedSale.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found after saving"));

        return saleMapper.toResponse(savedSale);
    }

    @Override
    public SaleResponse updateSale(Long id, SaleRequest request) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));

        Harvest harvest = harvestRepository.findById(request.getHarvestId())
                .orElseThrow(() -> new ResourceNotFoundException("Harvest not found with id: " + request.getHarvestId()));

        ValidationUtil.validateSaleDate(saleMapper.toEntity(request), harvest);

        ValidationUtil.validateSaleQuantity(harvest);

        saleMapper.updateSaleFromRequest(request, sale);
        sale.setHarvest(harvest);
        return saleMapper.toResponse(saleRepository.save(sale));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getSaleById(Long id) {
        return saleRepository.findById(id)
                .map(saleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getAllSales() {
        return saleRepository.findAll().stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesByHarvestId(Long harvestId) {
        return saleRepository.findByHarvestId(harvestId).stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date must be before end date");
        }
        return saleRepository.findByDateBetween(startDate, endDate).stream()
                .map(saleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> getSalesByCustomer(String customer) {
        return saleRepository.findByCustomer(customer).stream()
                .map(saleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSale(Long id) {
        if (!saleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sale not found with id: " + id);
        }
        saleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateTotalRevenueBetweenDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date must be before end date");
        }
        Double revenue = saleRepository.calculateTotalRevenueBetweenDates(startDate, endDate);
        return revenue != null ? revenue : 0.0;
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageUnitPriceBySeason(SeasonEnum season) {
        Double averagePrice = saleRepository.calculateAverageUnitPriceBySeason(season);
        return averagePrice != null ? averagePrice : 0.0;
    }
}