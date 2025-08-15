package com.ecommerce.SamCommerce.Repositories;

import com.ecommerce.SamCommerce.Entity.Categories;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Categories, Integer> {
    Categories findByCategoryTitle(@NotBlank String categoryTitle);
}
