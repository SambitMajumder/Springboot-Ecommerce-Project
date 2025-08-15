package com.ecommerce.SamCommerce.Services;

import com.ecommerce.SamCommerce.Entity.Categories;
import com.ecommerce.SamCommerce.Exceptions.APIException;
import com.ecommerce.SamCommerce.Exceptions.ResourceNotFoundException;
import com.ecommerce.SamCommerce.Payload.CategoryDTO;
import com.ecommerce.SamCommerce.Payload.CategoryResponse;
import com.ecommerce.SamCommerce.Repositories.CategoryRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImplementation implements CategoryService{

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    public CategoryServiceImplementation(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        //1. This is Create so no use of response
        //2. First map the categories with the categoryDTO because we are calling the categoryDTO as a parameter
        //3. Find the Category from DB by title
        //4. Validate if it is already present or not
        //5. save the category by using repository.save
        //6. now map the savedCategory with the Category DTO

        Categories categories = modelMapper.map(categoryDTO, Categories.class);
        Categories categoryFromDB = categoryRepository.findByCategoryTitle(categories.getCategoryTitle());
        if(categoryFromDB !=null){
            throw new APIException("Category with the this name already present: " + categories.getCategoryTitle());
        }
        Categories savedCategory = categoryRepository.save(categories);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryResponse categoryDisplayAll(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        //1. Fetching the details from the database
        //2. Validation if the list is empty or not
        //3. we are mapping categories list to categoryDTO list
        //4. Adding the Response and returning it

        //Implementing Sorting
        //if sortOrder = asc then use ascending or use descending
        Sort sortByandOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        //Implementing pagination
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByandOrder);
        Page<Categories> categoryPage = categoryRepository.findAll(pageDetails);

        List<Categories> allCategories = categoryPage.getContent();
        if(allCategories.isEmpty()){
            throw new APIException("No Category Found");
        }
        List<CategoryDTO> categoryDTOs = allCategories.stream().
                map(Categories -> modelMapper.map(Categories, CategoryDTO.class)).toList();
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOs);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public Categories categoryDisplayById(int id) {
        return categoryRepository.findById(id).orElseThrow(()-> new RuntimeException("Category not found"));
    }

    @Transactional
    @Override
    public CategoryDTO deleteCategory(int id) {

        Optional<Categories> findTheCategory = categoryRepository.findById(id);
        Categories deleteCategory = findTheCategory.orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",id));
        categoryRepository.deleteById(id);
        return modelMapper.map(deleteCategory, CategoryDTO.class);
    }

    @Transactional
    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, int id) {
        // 1. Fetch existing category by ID.
        // 2. Throw an error if it doesn't exist.
        // 3. Map the DTO to entity, set the existing ID.
        // 4. Save the updated entity.
        // 5. Return the updated DTO.

        Optional<Categories> presentCategory = categoryRepository.findById(id);
        Categories savedCategory = presentCategory.orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",id));
        Categories categories = modelMapper.map(categoryDTO, Categories.class);
        categories.setCategoryID(id);
        savedCategory = categoryRepository.save(categories);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }
}
