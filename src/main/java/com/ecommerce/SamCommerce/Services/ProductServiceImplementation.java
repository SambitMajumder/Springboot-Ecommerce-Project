package com.ecommerce.SamCommerce.Services;

import com.ecommerce.SamCommerce.Entity.Categories;
import com.ecommerce.SamCommerce.Entity.Product;
import com.ecommerce.SamCommerce.Exceptions.APIException;
import com.ecommerce.SamCommerce.Exceptions.ResourceNotFoundException;
import com.ecommerce.SamCommerce.Payload.ProductDTO;
import com.ecommerce.SamCommerce.Payload.ProductResponse;
import com.ecommerce.SamCommerce.Repositories.CategoryRepository;
import com.ecommerce.SamCommerce.Repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImplementation implements ProductService{

    @Value("${project.image}")
    private String path;

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private ModelMapper modelMapper;
    private FileService fileService;

    @Autowired
    public ProductServiceImplementation(ProductRepository productRepository, CategoryRepository categoryRepository, ModelMapper modelMapper, FileService fileService) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
        this.categoryRepository = categoryRepository;
        this.fileService = fileService;
    }

    @Transactional
    @Override
    public ProductDTO addingProduct(ProductDTO productDTO, int categoryID) {
        Categories category = categoryRepository.findById(categoryID).orElseThrow(()-> new ResourceNotFoundException("Category","categoryID",categoryID));
        //VALIDATION -> IF THE PRODUCT IS PRESENT OR NOT IN THE DATABASE
        boolean isProductPresent = true;
        List<Product> products = category.getProducts();
        for(Product value: products){
            if(value.getProductName().equals(productDTO.getProductName())){
                isProductPresent = false;
                break;
            }
        }
        if(isProductPresent){
            //CONVERTING THE PRODUCT DTO -> PRODUCT ENTITY
            Product product = modelMapper.map(productDTO, Product.class);
            product.setImage("default.png");
            product.setCategories(category);
            double specialPrice = product.getPrice() - ((product.getDiscount()*.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);
            return modelMapper.map(savedProduct, ProductDTO.class);
        }else {
            throw new APIException("The Product is already present");
        }
    }

    @Override
    public ProductResponse displayProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        //IMPLEMENTING SORTING AND PAGINATION
        Sort sortByandOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByandOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);

        //Getting the list of products
        List<Product> productList = productPage.getContent();
        if(productList.isEmpty()){
            throw new APIException("Product List is empty");
        }
        //Changing the list into DTO
        List<ProductDTO> productDTOS = productList.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();
        if(productDTOS.isEmpty()){
            throw new APIException("Product not found");
        }
        //Converting the DTO to Response and returning it
        ProductResponse productResponse = new ProductResponse();
        productResponse.setProductResponseContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse displayProductsByCategory(int categoryID, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        //Get the Category
        Categories category =categoryRepository.findById(categoryID).orElseThrow(()-> new ResourceNotFoundException("Categories", "categoryId", categoryID));

        //IMPLEMENTING SORTING AND PAGINATION
        Sort sortByandOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByandOrder);
        Page<Product> productPage = productRepository.findByCategoriesOrderByPriceAsc(category, pageDetails);

        //Find the product
        List<Product> products = productPage.getContent();
        if(products.isEmpty()){
            throw new APIException("Product List is empty");
        }
        List<ProductDTO> productDTOS = products.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();
        if(productDTOS.isEmpty()){
            throw new APIException("Product not found");
        }
        //Converting the DTO to Response and returning it
        ProductResponse productResponse = new ProductResponse();
        productResponse.setProductResponseContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse displayProductsByKeyWord(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        //IMPLEMENTING SORTING AND PAGINATION
        Sort sortByandOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByandOrder);
        Page<Product> productPage = productRepository.findByProductNameContaining(keyword, pageDetails);

        //Find the product using keyword
        List<Product> products = productPage.getContent();
        if(products.isEmpty()){
            throw new APIException("Product List is empty");
        }
        List<ProductDTO> productDTOS = products.stream().map(product -> modelMapper.map(product, ProductDTO.class)).toList();
        if(productDTOS.isEmpty()){
            throw new APIException("Product not found");
        }
        //Converting the DTO to Response and returning it
        ProductResponse productResponse = new ProductResponse();
        productResponse.setProductResponseContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    //UPDATING THE PRODUCT
    @Transactional
    @Override
    public ProductDTO updatingProduct(ProductDTO productDTO, int productID) {
        Product presentProduct = productRepository.findById(productID).orElseThrow(() -> new ResourceNotFoundException("Product", "productID", productID));
        //CONVERTING THE DTO TO ENTITY
        Product product = modelMapper.map(productDTO, Product.class);
        //UPDATING THE PRODUCT AS PER THE REQUEST BODY
        presentProduct.setProductName(product.getProductName());
        presentProduct.setDescription(product.getDescription());
        presentProduct.setDiscount(product.getDiscount());
        presentProduct.setPrice(product.getPrice());
        presentProduct.setQuantity(product.getQuantity());
        //double specialPriceProduct = product.getPrice() - ((product.getDiscount()*.01) * product.getPrice());
        presentProduct.setSpecialPrice(product.getSpecialPrice());
        Product saveProduct = productRepository.save(presentProduct);
        return modelMapper.map(saveProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deletingProduct(int productID) {
        Product presentProduct = productRepository.findById(productID).orElseThrow(()-> new ResourceNotFoundException("Product", "productID", productID));
        productRepository.delete(presentProduct);
        return modelMapper.map(presentProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(int productID, MultipartFile image) throws IOException {
        Product presentProduct = productRepository.findById(productID).orElseThrow(() -> new ResourceNotFoundException("Product", "productID", productID));
        //UPLOAD THE IMAGE
        //GET THE FILE NAME OF THE UPLOADED IMAGE
        String fileName = fileService.uploadImage(path, image);
        //UPDATING THE NEW FILE NAME TO THE PRODUCT
        presentProduct.setImage(fileName);
        //SAVE THE PRODUCT
        Product product = productRepository.save(presentProduct);
        //RETURN DTO
        return modelMapper.map(product, ProductDTO.class);
    }
}
