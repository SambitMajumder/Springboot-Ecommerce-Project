package com.ecommerce.SamCommerce.Controllers;

import com.ecommerce.SamCommerce.Config.AppConstants;
import com.ecommerce.SamCommerce.Payload.ProductDTO;
import com.ecommerce.SamCommerce.Payload.ProductResponse;
import com.ecommerce.SamCommerce.Services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    private ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //CREATE NEW PRODUCTS
    @PostMapping("/admin/categories/{categoryID}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO, @PathVariable int categoryID){
        ProductDTO savedProductDTO = productService.addingProduct(productDTO, categoryID);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    //GET ALL THE PRODUCTS
    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllTheProducts(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.pageNumberProduct, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.pageSizeProduct, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.sortByProduct, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.sortOrderProduct, required = false) String sortOrder
    ){
        ProductResponse productResponse = productService.displayProducts(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    //GET PRODUCTS USING CATEGORY
    @GetMapping("/public/categories/{categoryID}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(@PathVariable int categoryID,
                                                                 @RequestParam(name = "pageNumber", defaultValue = AppConstants.pageNumberProduct, required = false) Integer pageNumber,
                                                                 @RequestParam(name = "pageSize", defaultValue = AppConstants.pageSizeProduct, required = false) Integer pageSize,
                                                                 @RequestParam(name = "sortBy", defaultValue = AppConstants.sortByProduct, required = false) String sortBy,
                                                                 @RequestParam(name = "sortOrder", defaultValue = AppConstants.sortOrderProduct, required = false) String sortOrder){
        ProductResponse productResponse = productService.displayProductsByCategory(categoryID, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    //GET PRODUCTS USING KEYWORD
    @GetMapping("/public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword(@PathVariable String keyword,
                                                                @RequestParam(name = "pageNumber", defaultValue = AppConstants.pageNumberProduct, required = false) Integer pageNumber,
                                                                @RequestParam(name = "pageSize", defaultValue = AppConstants.pageSizeProduct, required = false) Integer pageSize,
                                                                @RequestParam(name = "sortBy", defaultValue = AppConstants.sortByProduct, required = false) String sortBy,
                                                                @RequestParam(name = "sortOrder", defaultValue = AppConstants.sortOrderProduct, required = false) String sortOrder){
        ProductResponse productResponse = productService.displayProductsByKeyWord(keyword, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    //UPDATE PRODUCT
    @PutMapping("/admin/products/{productID}")
    public ResponseEntity<ProductDTO> updateProducts(@Valid @PathVariable int productID, @RequestBody ProductDTO productDTO){
        ProductDTO updatedProductDTO = productService.updatingProduct(productDTO, productID);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

    //DELETE PRODUCT
    @DeleteMapping("/admin/products/{productID}")
    public ResponseEntity<ProductDTO> deletingProducts(@PathVariable int productID){
        ProductDTO productDTO = productService.deletingProduct(productID);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

    //UPDATING THE PRODUCT IMAGE
    @PutMapping("/products/{productID}/image")
    public ResponseEntity<ProductDTO> updatingProductImage(@PathVariable int productID, @RequestParam("image")MultipartFile image) throws IOException {
        ProductDTO updatedProductDTO = productService.updateProductImage(productID, image);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

}


