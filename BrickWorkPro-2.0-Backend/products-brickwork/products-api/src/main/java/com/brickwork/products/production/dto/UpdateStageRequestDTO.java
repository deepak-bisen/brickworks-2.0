package com.brickwork.products.production.dto;

import com.brickwork.products.production.enums.ProductionStage;
import lombok.Data;

@Data
public class UpdateStageRequestDTO {
    private ProductionStage stage;
}
