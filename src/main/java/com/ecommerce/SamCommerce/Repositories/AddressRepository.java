package com.ecommerce.SamCommerce.Repositories;

import com.ecommerce.SamCommerce.Entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Integer> {
}
