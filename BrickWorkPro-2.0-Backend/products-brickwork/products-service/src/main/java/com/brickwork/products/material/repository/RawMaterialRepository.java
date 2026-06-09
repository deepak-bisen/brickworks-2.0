package com.brickwork.products.material.repository;

import com.brickwork.products.material.entity.RawMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RawMaterialRepository extends JpaRepository<RawMaterial, String> {

    Optional<RawMaterial> findByMaterialNameIgnoreCase(String materialName);
}
