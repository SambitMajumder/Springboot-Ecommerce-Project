package com.ecommerce.SamCommerce.Services;

import com.ecommerce.SamCommerce.Entity.Categories;
import com.ecommerce.SamCommerce.Payload.CategoryDTO;
import com.ecommerce.SamCommerce.Payload.CategoryResponse;
import jakarta.validation.Valid;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryResponse categoryDisplayAll(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    Categories categoryDisplayById(int id);
    CategoryDTO deleteCategory(int id);
    CategoryDTO updateCategory(@Valid CategoryDTO categoryDTO, int id);
}
