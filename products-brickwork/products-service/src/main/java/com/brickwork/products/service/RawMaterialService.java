package com.brickwork.products.service;

import com.brickwork.products.dto.RawMaterialDTO;

import java.util.List;

public interface RawMaterialService {
    List<RawMaterialDTO> getAllMaterials();
    RawMaterialDTO getMaterialById(String id);
    RawMaterialDTO addMaterial(RawMaterialDTO dto);
    RawMaterialDTO updateMaterialStock(String id, Double stockAdded);
}
