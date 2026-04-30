package com.brickwork.products.controller.impl;

import com.brickwork.products.controller.RawMaterialController;
import com.brickwork.products.dto.RawMaterialDTO;
import com.brickwork.products.service.RawMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RawMaterialControllerImpl implements RawMaterialController {

    @Autowired
    private RawMaterialService rawMaterialService;

    @Override
    public ResponseEntity<List<RawMaterialDTO>> getAllMaterials() {
        return ResponseEntity.ok(rawMaterialService.getAllMaterials());
    }

    @Override
    public ResponseEntity<RawMaterialDTO> getMaterialById(String materialId) {
        return ResponseEntity.ok(rawMaterialService.getMaterialById(materialId));
    }

    @Override
    public ResponseEntity<RawMaterialDTO> addMaterial(RawMaterialDTO rawMaterialDTO) {
        return ResponseEntity.ok(rawMaterialService.addMaterial(rawMaterialDTO));
    }

    @Override
    public ResponseEntity<RawMaterialDTO> updateMaterialStock(String materialId, Double stockAdded) {
        return ResponseEntity.ok(rawMaterialService.updateMaterialStock(materialId, stockAdded));
    }
}
