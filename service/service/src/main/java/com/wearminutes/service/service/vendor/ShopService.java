package com.wearminutes.service.service.vendor;

import com.wearminutes.service.dto.entity.Shop;
import com.wearminutes.service.dto.request.ShopRequest;
import com.wearminutes.service.exception.ResourceNotFoundException;
import com.wearminutes.service.repository.ShopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ShopService {

    @Autowired
    private ShopRepository shopRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 1. Register new shop
    @Transactional
    public Shop registerShop(ShopRequest request) {
        Shop shop = new Shop();
        shop.setVendorId(request.getVendorId());
        shop.setShopName(request.getShopName());
        shop.setOwnerName(request.getOwnerName());
        shop.setPhoneNumber(request.getPhoneNumber());
        shop.setAddress(request.getAddress());
        shop.setCity(request.getCity());
        shop.setState(request.getState());
        shop.setPincode(request.getPincode());
        shop.setIsActive(true);
        shop.setCreatedAt(LocalDateTime.now().format(FORMATTER));
        shop.setUpdatedAt(LocalDateTime.now().format(FORMATTER));

        return shopRepository.save(shop);
    }

    // 2. Fetch all shops by vendor ID
    public List<Shop> getShopsByVendor(Long vendorId) {
        return shopRepository.findByVendorId(vendorId);
    }

    // 3. Fetch single shop by ID
    public Shop getShopById(Long id) {
        return shopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", id));
    }

    // 4. Update shop details
    @Transactional
    public Shop updateShop(Long id, ShopRequest request) {
        Shop existingShop = getShopById(id);

        existingShop.setShopName(request.getShopName());
        existingShop.setOwnerName(request.getOwnerName());
        existingShop.setPhoneNumber(request.getPhoneNumber());
        existingShop.setAddress(request.getAddress());
        existingShop.setCity(request.getCity());
        existingShop.setState(request.getState());
        existingShop.setPincode(request.getPincode());
        existingShop.setUpdatedAt(LocalDateTime.now().format(FORMATTER));

        return shopRepository.save(existingShop);
    }

    // 5. Delete shop
    @Transactional
    public void deleteShop(Long id) {
        Shop shop = getShopById(id);
        shopRepository.delete(shop);
    }
}
