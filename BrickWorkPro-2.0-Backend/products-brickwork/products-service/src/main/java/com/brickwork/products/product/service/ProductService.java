package com.brickwork.products.product.service;

import com.brickwork.products.product.dto.ProductDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    ProductDTO getProductById(String productId);
    List<ProductDTO> getAllProducts();
    List<ProductDTO> getProductsByCategory(String category);
    ProductDTO createProduct(ProductDTO productDTO, MultipartFile imageFile) throws IOException;
    ProductDTO updateProduct(String productId, ProductDTO productDTO, MultipartFile imageFile) throws IOException;
    void deleteProduct(String productId);
    void deductStock(String productId, int quantity);
}
