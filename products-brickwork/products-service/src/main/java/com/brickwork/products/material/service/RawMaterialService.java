package com.brickwork.products.material.service;

import com.brickwork.products.material.dto.RawMaterialDTO;

import java.util.List;

public interface RawMaterialService {
    List<RawMaterialDTO> getAllMaterials();
    RawMaterialDTO getMaterialById(String id);
    RawMaterialDTO addMaterial(RawMaterialDTO dto);
    RawMaterialDTO updateMaterialStock(String id, Double stockAdded);
}
