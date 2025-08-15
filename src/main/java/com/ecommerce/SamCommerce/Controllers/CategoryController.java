package com.ecommerce.SamCommerce.Controllers;

import com.ecommerce.SamCommerce.Config.AppConstants;
import com.ecommerce.SamCommerce.Entity.Categories;
import com.ecommerce.SamCommerce.Payload.CategoryDTO;
import com.ecommerce.SamCommerce.Payload.CategoryResponse;
import com.ecommerce.SamCommerce.Services.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {
    private CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    //@GetMapping("/public/categories")
    @RequestMapping(value ="/public/categories" , method = RequestMethod.GET)
    public ResponseEntity<CategoryResponse> gettingTheCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.pageNumber, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.pageSize, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.sortBy, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.sortOrder, required = false) String sortOrder
    ){
        CategoryResponse categoryResponse = categoryService.categoryDisplayAll(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }

    @GetMapping("/public/categories/{id}")
    public ResponseEntity<Categories> gettingTheCategories(@PathVariable int id){
        Categories categories = categoryService.categoryDisplayById(id);
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> addingTheCategories(@Valid @RequestBody CategoryDTO categoryDTO){
        CategoryDTO savedCategoryDTO = categoryService.createCategory(categoryDTO);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
    }

    @PutMapping("/public/categories/{id}")
    public ResponseEntity<CategoryDTO> updatingTheCategory(@Valid @RequestBody CategoryDTO categoryDTO,@PathVariable int id){
        CategoryDTO savedCategoryDTO = categoryService.updateCategory(categoryDTO, id);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/public/categories/{id}")
    public ResponseEntity<CategoryDTO> deletingTheCategory(@PathVariable int id){
        CategoryDTO deletedCategoryDTO  = categoryService.deleteCategory(id);
        return new ResponseEntity<>(deletedCategoryDTO,HttpStatus.OK);
    }
}
