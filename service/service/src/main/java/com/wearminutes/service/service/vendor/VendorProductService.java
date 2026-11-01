package com.wearminutes.service.service.vendor;

import com.wearminutes.service.dto.entity.Product;
import com.wearminutes.service.dto.entity.Shop;
import com.wearminutes.service.dto.request.ProductRequest;
import com.wearminutes.service.exception.ResourceNotFoundException;
import com.wearminutes.service.repository.ProductRepository;
import com.wearminutes.service.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VendorProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ShopRepository shopRepository;

    // 1. Add new product
    @Transactional
    public Product addProduct(ProductRequest request) {
        Shop shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new ResourceNotFoundException("Shop", request.getShopId()));

        Product product = new Product();
        product.setShop(shop);
        product.setTitle(request.getTitle());
        product.setFabric(request.getFabric());
        product.setColor(request.getColor());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());

        return productRepository.save(product);
    }

    // 2. Fetch all products by shop ID
    public List<Product> getProductsByShop(Long shopId) {
        return productRepository.findByShopId(shopId);
    }

    // 3. Fetch single product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    // 4. Update product details
    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product existingProduct = getProductById(id);

        existingProduct.setTitle(request.getTitle());
        existingProduct.setFabric(request.getFabric());
        existingProduct.setColor(request.getColor());
        existingProduct.setPrice(request.getPrice());
        existingProduct.setStockQuantity(request.getStockQuantity());
        existingProduct.setImageUrl(request.getImageUrl());

        return productRepository.save(existingProduct);
    }

    // 5. Delete product
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}
