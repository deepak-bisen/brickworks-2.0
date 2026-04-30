package com.brickwork.products.material.service.impl;

import com.brickwork.products.material.dto.RawMaterialDTO;
import com.brickwork.products.material.entity.RawMaterial;
import com.brickwork.products.material.repository.RawMaterialRepository;
import com.brickwork.products.material.service.RawMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public RawMaterialDTO addMaterial(RawMaterialDTO dto) {
        RawMaterial material = new RawMaterial();
        material.setName(dto.getName());
        material.setUnitOfMeasure(dto.getUnitOfMeasure());
        material.setCurrentStockLevel(dto.getCurrentStockLevel());
        return mapToDTO(rawMaterialRepository.save(material));
    }

    @Override
    public RawMaterialDTO updateMaterialStock(String id, Double stockAdded) {
        RawMaterial rawMaterial = rawMaterialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RawMaterial not found with ID: " + id));

        Double currentStockLevel = rawMaterial.getCurrentStockLevel() != null ? rawMaterial.getCurrentStockLevel() : 0.0;
        rawMaterial.setCurrentStockLevel(currentStockLevel + stockAdded);
        return mapToDTO(rawMaterialRepository.save(rawMaterial));
    }

    private  RawMaterialDTO mapToDTO(RawMaterial rawMaterial) {
        return new RawMaterialDTO(
                rawMaterial.getRawMaterialId(),
                rawMaterial.getName(),
                rawMaterial.getUnitOfMeasure(),
                rawMaterial.getCurrentStockLevel()
        );
    }
}
