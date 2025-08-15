package com.ecommerce.SamCommerce.Repositories;

import com.ecommerce.SamCommerce.Entity.Categories;
import com.ecommerce.SamCommerce.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Page<Product> findByCategoriesOrderByPriceAsc(Categories categories, Pageable pageDetails);
    Page<Product> findByProductNameContaining(String keyword, Pageable pageDetails);
    //WE CAN USE : List<Product> findByProductNameIgnoreCase(String keyword); -> in the implementation do like: '%'+keyword+'%'
}
