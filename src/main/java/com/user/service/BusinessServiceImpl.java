package com.user.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

<<<<<<< HEAD
import com.user.api_response.ApiResponse;
import com.user.entity.BusinessEntity;
import com.user.entity.Transaction;
=======
import com.user.entity.BusinessEntity;
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
import com.user.repository.BusinessRepository;
import com.user.repository.UserEntityRepo;
@Service
public class BusinessServiceImpl {

	@Autowired
	private TransactionServiceImpl transactionServiceImpl;
	
    @Autowired
    private BusinessRepository businessRepository;
    
    @Autowired
    private UserEntityRepo userEntityRepo;

<<<<<<< HEAD
    public ApiResponse<BusinessEntity> addBusiness(BusinessEntity business) {
=======
    public BusinessEntity addBusiness(BusinessEntity business) {
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
    	Long userId = transactionServiceImpl.getLoggedInUserId();
    	business.setUserId(userId);
        business.setCreatedDate(LocalDateTime.now());
        business.setUpdatedDate(LocalDateTime.now());
        
        userEntityRepo.updateShopName(userId, business.getBusinessName());
<<<<<<< HEAD
        BusinessEntity save = businessRepository.save(business);
        
        return ApiResponse.<BusinessEntity>builder()
                .success(true)
                .message("Business Profile Saved")
                .data(save)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ApiResponse<BusinessEntity> updateBusiness(Long id, BusinessEntity business) {
=======
        return businessRepository.save(business);
    }

    public BusinessEntity updateBusiness(Long id, BusinessEntity business) {
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
        BusinessEntity existing = businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found with id: " + id));
        Long userId = transactionServiceImpl.getLoggedInUserId();
        existing.setUserId(userId);
        existing.setEmail(business.getEmail());
        existing.setPhone1(business.getPhone1());
        existing.setPhone2(business.getPhone2());
        existing.setAddress(business.getAddress());
        existing.setBusinessName(business.getBusinessName());
        existing.setBusinessDescription(business.getBusinessDescription());
        existing.setUpdatedDate(LocalDateTime.now());

        userEntityRepo.updateShopName(userId, business.getBusinessName());
<<<<<<< HEAD
        
        return ApiResponse.<BusinessEntity>builder()
                .success(true)
                .message("Business Profile Updated")
                .data(businessRepository.save(existing))
                .timestamp(LocalDateTime.now())
                .build();
=======
        return businessRepository.save(existing);
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
    }

    public void deleteBusiness(Long id) {
        if (!businessRepository.existsById(id)) {
            throw new RuntimeException("Business not found with id: " + id);
        }
        businessRepository.deleteById(id);
    }

    public BusinessEntity getBusinessById(Long id) {
        return businessRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business not found with id: " + id));
    }

    public List<BusinessEntity> getAllBusinesses() {
        return businessRepository.findAll();
    }

<<<<<<< HEAD
    public ApiResponse<BusinessEntity> getBusinessesByUserId() {

        Long userId = transactionServiceImpl.getLoggedInUserId();

        BusinessEntity findByUserId = businessRepository.findByUserId(userId);

        return ApiResponse.<BusinessEntity>builder()
                .success(true)
                .message("Successful")
                .data(findByUserId)
                .timestamp(LocalDateTime.now())
                .build();
=======
    public List<BusinessEntity> getBusinessesByUserId(Long userId) {
        return businessRepository.findByUserId(userId);
>>>>>>> 45fd25982da46d435d6a1f4c455026ae04e43c17
    }
}