package com.brickwork.products.material.service.impl;

import com.brickwork.products.material.dto.RawMaterialDTO;
import com.brickwork.products.material.entity.RawMaterial;
import com.brickwork.products.material.repository.RawMaterialRepository;
import com.brickwork.products.material.service.RawMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RawMaterialServiceImpl implements RawMaterialService {

    @Autowired
    private RawMaterialRepository rawMaterialRepository;

    @Override
    public List<RawMaterialDTO> getAllMaterials() {
        return rawMaterialRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public RawMaterialDTO getMaterialById(String RawMaterialId) {
        RawMaterial material = rawMaterialRepository.findById(RawMaterialId).
                orElseThrow(() -> new RuntimeException("RawMaterial not found with ID: " + RawMaterialId));
        return mapToDTO(material);
    }

    @Override
    @Transactional
    public RawMaterialDTO addMaterial(RawMaterialDTO dto) {
        RawMaterial material = new RawMaterial();
        material.setMaterialName(dto.getMaterialName());
        material.setUnitOfMeasure(dto.getUnitOfMeasure());
        material.setCurrentStockLevel(dto.getCurrentStockLevel());
        return mapToDTO(rawMaterialRepository.save(material));
    }

    @Override
    @Transactional
    public RawMaterialDTO updateMaterialStock(String id, Double stockAdded) {
        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RawMaterial not found with ID: " + id));

        Double currentStockLevel = rawMaterial.getCurrentStockLevel() != null ? rawMaterial.getCurrentStockLevel() : 0.0;
        rawMaterial.setCurrentStockLevel(currentStockLevel + stockAdded);
        return mapToDTO(rawMaterialRepository.save(rawMaterial));
    }

    private  RawMaterialDTO mapToDTO(RawMaterial rawMaterial) {
        return new RawMaterialDTO(
                rawMaterial.getId(),
                rawMaterial.getMaterialName(),
                rawMaterial.getUnitOfMeasure(),
                rawMaterial.getCurrentStockLevel()
        );
    }

    @Override
    @Transactional
    public void consumeMaterial(String materialName, double amount) {
        RawMaterial material = rawMaterialRepository.findByMaterialNameIgnoreCase(materialName)
                .orElseThrow(() -> new RuntimeException("Material not found: " + materialName));

        Double currentStock = material.getCurrentStockLevel() != null ? material.getCurrentStockLevel() : 0.0;
        if (currentStock < amount) {
            throw new RuntimeException("Insufficient stock for: " + materialName);
        }

        material.setCurrentStockLevel(currentStock - amount);
        rawMaterialRepository.save(material);
    }
}
