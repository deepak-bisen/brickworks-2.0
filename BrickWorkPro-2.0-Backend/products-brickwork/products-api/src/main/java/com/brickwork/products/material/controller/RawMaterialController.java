package com.brickwork.products.material.controller;

import com.brickwork.products.material.dto.RawMaterialDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/raw-materials")
public interface RawMaterialController {

    @GetMapping("/all")
    ResponseEntity<List<RawMaterialDTO>> getAllMaterials();

    @GetMapping("/get/{id}")
    ResponseEntity<RawMaterialDTO> getMaterialById(@PathVariable("id") String materialId);

    @PostMapping("/add")
    ResponseEntity<RawMaterialDTO> addMaterial(@RequestBody RawMaterialDTO rawMaterialDTO);

    @PutMapping("/update/{id}")
    ResponseEntity<RawMaterialDTO> updateMaterialStock(@PathVariable("id") String materialId, @RequestParam("stockAdded") Double stockAdded);
}
