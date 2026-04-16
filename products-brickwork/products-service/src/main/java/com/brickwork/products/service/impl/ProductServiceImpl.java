package com.brickwork.products.service.impl;

import com.brickwork.products.dto.ProductDTO;
import com.brickwork.products.entity.Product;
import com.brickwork.products.repository.ProductRepository;
import com.brickwork.products.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return  mapToDTO(product);
    }

    @Override
    public List<ProductDTO> getProductsByCategory(String category){
        return productRepository.findByCategory(category).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile imageFile) throws IOException {
        Product product = mapToEntity(productDTO);
        product.setImageName(imageFile.getOriginalFilename());
        product.setImageType(imageFile.getContentType());
        product.setImageData(imageFile.getBytes());

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    /**
     * Deletes a product by its ID.
     * @param productID The ID of the product to delete.
     * @throws RuntimeException if the product is not found.
     */
    @Override
    public void deleteProduct(String productID) {
        if (!productRepository.existsById(productID)) {
            // Or you could throw a custom ResourceNotFoundException
            throw new RuntimeException("Product not found with id: " + productID);
        }
        productRepository.deleteById(productID);
    }

    /**
     * Updates an existing product.
     *
     * @param productId : The ID of the product to update.
     * @param productDTO : The new details for the product.
     * @return The updated product.
     * @throws RuntimeException if the product is not found.
     */
    @Override
    public ProductDTO updateProduct(String productId, ProductDTO productDTO, MultipartFile imageFile) throws IOException {
        // Find the existing product
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Update the fields
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setDimensions(productDTO.getDimensions());
        existingProduct.setColor(productDTO.getColor());
        existingProduct.setBrickType(productDTO.getBrickType());
        existingProduct.setUnitPrice(productDTO.getUnitPrice());
        existingProduct.setStockQuantity(productDTO.getStockQuantity());

        if (imageFile != null && !imageFile.isEmpty()) {
            existingProduct.setImageName(imageFile.getOriginalFilename());
            existingProduct.setImageType(imageFile.getContentType());
            existingProduct.setImageData(imageFile.getBytes());
        }

        existingProduct.setEstimatedCost(productDTO.getEstimatedCost());
        existingProduct.setBulkDiscountThreshold(productDTO.getBulkDiscountThreshold());

        // Save the updated product back to the database
        return mapToDTO(productRepository.save(existingProduct));
    }

    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(product.getProductId(), product.getName(), product.getColor(), product.getDescription(),
                product.getBrickType(), product.getCategory(), product.getDimensions(),
                product.getUnitPrice(), product.getStockQuantity(), product.getEstimatedCost(),
                product.getBulkDiscountThreshold(), product.getImageName(), product.getImageType(), product.getImageData());
    }

    private Product mapToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setImageName(dto.getName());
        product.setImageType(dto.getImageType());
        product.setImageData(dto.getImageData());
        product.setDimensions(dto.getDimensions());
        product.setUnitPrice(dto.getUnitPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setEstimatedCost(dto.getEstimatedCost());
        product.setBulkDiscountThreshold(dto.getBulkDiscountThreshold());
        return product;
    }
}