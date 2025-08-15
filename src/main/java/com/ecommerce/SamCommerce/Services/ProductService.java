package com.ecommerce.SamCommerce.Services;

import com.ecommerce.SamCommerce.Payload.ProductDTO;
import com.ecommerce.SamCommerce.Payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductDTO addingProduct(ProductDTO productDTO, int categoryID);

    ProductResponse displayProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductResponse displayProductsByCategory(int categoryID, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductResponse displayProductsByKeyWord(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductDTO updatingProduct(ProductDTO productDTO, int productID);

    ProductDTO deletingProduct(int productID);

    ProductDTO updateProductImage(int productID, MultipartFile image) throws IOException;
}
