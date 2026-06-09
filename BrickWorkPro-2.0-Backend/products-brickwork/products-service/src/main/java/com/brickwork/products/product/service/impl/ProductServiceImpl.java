package com.brickwork.products.product.service.impl;

import com.brickwork.products.product.dto.ProductDTO;
import com.brickwork.products.product.entity.Product;
import com.brickwork.products.product.entity.ProductAttachment;
import com.brickwork.products.product.repository.ProductRepository;
import com.brickwork.products.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getProductById(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return  mapToDTO(product);
    }

    @Override
    @Transactional
    public List<ProductDTO> getProductsByCategory(String category){
        return productRepository.findByCategory(category).stream().map(this::mapToDTO).collect(Collectors.toList());
    }


    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO, MultipartFile imageFile) throws IOException {
        Product product = mapToEntity(productDTO);
        // Handle the image file and bind it to the Product via the OneToMany relationship
        if (imageFile != null && !imageFile.isEmpty()) {
            ProductAttachment productAttachment = new ProductAttachment();
            productAttachment.setProduct(product);
            productAttachment.setName(imageFile.getOriginalFilename());
            productAttachment.setExtension(imageFile.getContentType());
            productAttachment.setImageData(imageFile.getBytes());

            // Add the attachment to the product's list so CascadeType.ALL can save it
            product.setAttachments(new ArrayList<>(List.of(productAttachment)));
        }

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    /**
     * Deletes a product by its ID.
     * @param productID The ID of the product to delete.
     * @throws RuntimeException if the product is not found.
     */
    @Override
    @Transactional
    public void deleteProduct(String productID) {
        if (!productRepository.existsById(productID)) {
            // Or you could throw a custom ResourceNotFoundException
            throw new RuntimeException("Product not found with id: " + productID);
        }
        productRepository.deleteById(productID);
    }

    @Override
    @Transactional
    public void deductStock(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
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
    @Transactional
    public ProductDTO updateProduct(String productId, ProductDTO productDTO, MultipartFile imageFile) throws IOException {
        // Find the existing product
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Only update fields if they are NOT null in the DTO
        if (productDTO.getName() != null) existingProduct.setName(productDTO.getName());
        if (productDTO.getDescription() != null) existingProduct.setDescription(productDTO.getDescription());
        if (productDTO.getCategory() != null) existingProduct.setCategory(productDTO.getCategory());
        if (productDTO.getDimensions() != null) existingProduct.setDimensions(productDTO.getDimensions());
        if (productDTO.getBrickType() != null) existingProduct.setBrickType(productDTO.getBrickType());
        if (productDTO.getUnitPrice() != null) existingProduct.setUnitPrice(productDTO.getUnitPrice());
        if (productDTO.getStockQuantity() != null) existingProduct.setStockQuantity(productDTO.getStockQuantity());
        if (productDTO.getEstimatedCost() != null) existingProduct.setEstimatedCost(productDTO.getEstimatedCost());
        if (productDTO.getBulkDiscountThreshold() != null) existingProduct.setBulkDiscountThreshold(productDTO.getBulkDiscountThreshold());

        if (imageFile != null && !imageFile.isEmpty()) {
            ProductAttachment attachment;

            //// If an attachment already exists, update the first one. Otherwise, create a new one.
            if(existingProduct.getAttachments() != null && !existingProduct.getAttachments().isEmpty()){
                attachment = existingProduct.getAttachments().getFirst();
            }else {
                attachment = new ProductAttachment();
                attachment.setProduct(existingProduct);
                if (existingProduct.getAttachments() == null) {
                    existingProduct.setAttachments(new ArrayList<>());
                }
                existingProduct.getAttachments().add(attachment);
            }

            attachment.setName(imageFile.getOriginalFilename());
            attachment.setExtension(imageFile.getContentType());
            attachment.setImageData(imageFile.getBytes());
        }


        // Save the updated product back to the database
        return mapToDTO(productRepository.save(existingProduct));
    }

    private ProductDTO mapToDTO(Product product) {
        // Safely extract image data if attachments exist
        String imageName = null;
        String imageType = null;
        byte[] imageData = null;

        if (product.getAttachments() != null && !product.getAttachments().isEmpty()) {
            ProductAttachment firstAttachment = product.getAttachments().getFirst();
            imageName = firstAttachment.getName();
            imageType = firstAttachment.getExtension();
            imageData = firstAttachment.getImageData();
        }

        return new ProductDTO(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getUnitPrice(),
                product.getStockQuantity(),
                product.getBrickType(),
                product.getDimensions(),
                product.getEstimatedCost(),
                product.getBulkDiscountThreshold(),
                imageName,
                imageType,
                imageData
        );
    }

    private Product mapToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setUnitPrice(dto.getUnitPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setBrickType(dto.getBrickType());
        product.setDimensions(dto.getDimensions());
        product.setEstimatedCost(dto.getEstimatedCost());
        product.setBulkDiscountThreshold(dto.getBulkDiscountThreshold());
        // Image data is no longer mapped directly to the Product entity here.
        // It is handled in createProduct/updateProduct methods via ProductAttachment.
        return product;
    }
}
