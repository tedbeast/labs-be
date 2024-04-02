package org.revature.service;

import org.revature.App;
import org.revature.entity.PKey;
import org.revature.repository.ProductKeyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * service class intended to track the authentication and creation of product keys
 */
@Service
public class AuthService {
    ProductKeyRepository productKeyRepository;
    public AuthService(ProductKeyRepository productKeyRepository){
        this.productKeyRepository = productKeyRepository;
    }

    /**
     * true/false for if a user should be allowed basic access to lab content
     * @param pkey
     * @return
     */
    public boolean validateUser(long pkey){
        App.log.info("Checking if product key is valid for user: "+pkey);
        Optional<PKey> productKey = productKeyRepository.findById(pkey);
        if(productKey.isPresent()){
            return productKey.get().isActive();
        }
        return false;
    }

    /**
     * true/false for if a user should be allowed access to sensitive admin actions
     * @param pkey
     * @return
     */
    public boolean validateAdmin(long pkey){
        App.log.info("Checking if product key is valid for admin: "+pkey);
        Optional<PKey> productKey = productKeyRepository.findById(pkey);
        if(productKey.isPresent()){
            return productKey.get().isAdmin() && productKey.get().isActive();
        }
        return false;
    }

    /**
     * true/false for if a user should be allowed access to create new admins
     * @param pkey
     * @return
     */
    public boolean validateSuperAdmin(long pkey){
        App.log.info("Checking if product key is valid for superadmin: "+pkey);
        Optional<PKey> productKey = productKeyRepository.findById(pkey);
        if(productKey.isPresent()){
            return productKey.get().isSuperAdmin() && productKey.get().isActive();
        }
        return false;
    }
    public PKey getProductKey(long pkey){
        return productKeyRepository.findById(pkey).get();
    }
}
