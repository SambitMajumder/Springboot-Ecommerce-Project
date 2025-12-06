package com.ecommerce.SamCommerce.Services;

import com.ecommerce.SamCommerce.Entity.Cart;
import com.ecommerce.SamCommerce.Entity.CartItem;
import com.ecommerce.SamCommerce.Entity.Product;
import com.ecommerce.SamCommerce.Exceptions.APIException;
import com.ecommerce.SamCommerce.Exceptions.ResourceNotFoundException;
import com.ecommerce.SamCommerce.Payload.CartDTO;
import com.ecommerce.SamCommerce.Payload.ProductDTO;
import com.ecommerce.SamCommerce.Repositories.CartItemRepository;
import com.ecommerce.SamCommerce.Repositories.CartRepository;
import com.ecommerce.SamCommerce.Repositories.ProductRepository;
import com.ecommerce.SamCommerce.Util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImplementation implements CartService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Integer productId, Integer quantity) {
        //1. FIND EXISTING CART OR CREATE ONE
        Cart cart = createCart();
        //2. RETRIEVE PRODUCT DETAILS
        Product product = productRepository.findById(productId)
                            .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));
        //3. PERFORM VALIDATIONS
        //VALIDATE IF THE SPECIFIC ITEM IS IN THE CART ALREADY BEFORE ADDING IT
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);
        if(cartItem!=null){
            throw  new APIException("Product " + product.getProductName() + "already exists in the cart");
        }
        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }

        if(product.getQuantity() < quantity){
            throw new APIException("Please add proper quantity: " + product.getProductName() + " -> " + product.getQuantity());
        }

        //4. CREATE CART ITEM
        CartItem newCartItem = new CartItem();
        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        newCartItem.setProductPrice(product.getSpecialPrice());
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());

        //5. SAVE CART ITEM
        cartItemRepository.save(newCartItem);
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItemList = cart.getCartItems();
        //WE ARE CONVERTING THE CART ITEMS TO PRODUCT DTO + QUANTITY BECAUSE WE NEED TO MANUALLY SET THE QUANTITY
        Stream<ProductDTO> productDTOStream = cartItemList.stream().map(item-> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            productDTO.setQuantity(item.getQuantity());
            return productDTO;
        });
        cartDTO.setProducts((productDTOStream.toList()));
        //6. RETURN UPDATED CART
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        //FINDING ALL THE CARTS
        List<Cart> carts = cartRepository.findAll();
        if(carts.isEmpty()){
            throw new APIException("No cart is present!");
        }
        //CONVERT THE LIST OF CARTS TO CART DTOS
        List<CartDTO> cartDTOS = carts.stream()
                .map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
                    //NOW CONVERT ALL THE PRODUCTS TO PRODUCTS DTOS
                    //BECAUSE IN THE CART DTO WE HAVE PRODUCT DTO
                    List<ProductDTO> products = cart.getCartItems().stream().map(cartItem ->{
                        ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                        productDTO.setQuantity(cartItem.getQuantity());
                        return productDTO;
                    }).toList();
                    //SETTING THE PRODUCTS IN THE PRODUCT DTO
                    cartDTO.setProducts(products);
                    return cartDTO;
                }).toList();
        return cartDTOS;
    }

    @Override
    public CartDTO getCart(String emailId, Integer cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if(cart == null){
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        //CHANGING THE PRODUCT QUANTITY TO CART QUANTITY IN THE RESPONSE JSON
        cart.getCartItems().forEach(item-> item.getProduct().setQuantity(item.getQuantity()));
        //GETTING THE PRODUCTS WHICH IS ADDED IN THE CART AND MAPPING THAT TO PRODUCT DTO
        List<ProductDTO> productDTOS = cart.getCartItems().stream()
                .map(p-> modelMapper.map(p.getProduct(), ProductDTO.class)).toList();
        cartDTO.setProducts(productDTOS);
        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Integer productId, Integer quantity) {
        String email = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(email);
        int cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));

        //DOES THE STOCK EXISTS VALIDATIONS
        if(product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }
        if(product.getQuantity() < quantity){
            throw new APIException("Please add proper quantity: " + product.getProductName() + " -> " + product.getQuantity());
        }

        //VERIFY IF THE CART IS NULL
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem==null){
            throw  new APIException("Product " + product.getProductName() + "not exist in the cart!");
        }
        //CALCULATE NEW QUANTITY
        int newQuantity = cartItem.getQuantity() + quantity;
        //VALIDATION TO PREVENT NEGATIVE QUANTITIES
        if(newQuantity<0){
            throw new APIException("The quantity can not be negative!");
        }
        if(newQuantity == 0){
            deleteFromCart(cartId, productId);
        }else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }

        //UPDATING THE CART ITEM IF THE CART IS EMPTY THEN DELETE THE PRODUCT
        CartItem updatedItem = cartItemRepository.save(cartItem);
        if(updatedItem.getQuantity()==0){
            cartItemRepository.deleteById(Long.valueOf(updatedItem.getCartItemId()));
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItemList = cart.getCartItems();
        //WE ARE CONVERTING THE CART ITEMS TO PRODUCT DTO + QUANTITY BECAUSE WE NEED TO MANUALLY SET THE QUANTITY
        Stream<ProductDTO> productDTOStream = cartItemList.stream().map(item-> {
            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            productDTO.setQuantity(item.getQuantity());
            return productDTO;
        });
        cartDTO.setProducts((productDTOStream.toList()));
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteFromCart(Integer cartId, Integer productId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem==null){
            throw new ResourceNotFoundException("Product", "productId", productId);
        }
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice()* cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);
        return "Product -> " + cartItem.getProduct().getProductName() + " removed from the cart!";
    }

    @Override
    public void updateProductInCarts(Integer cartId, int productId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(()-> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null){
            throw new APIException("Product not available in the cart!");
        }

        //HERE WE ARE REDUCING THE OLD PRODUCT PRICE*QUANTITY FROM THE TOTAL PRICE
        //NOW WE HAVE THE UPDATED TOTAL PRICE
        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice()*cartItem.getQuantity());
        //UPDATE THE NEW PRICE
        cartItem.setProductPrice(product.getSpecialPrice());
        //NOW UPDATE THE TOTAL PRICE : PREVIOUS CART PRICE + NEW ADDED PRODUCT PRICES
        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice()*cartItem.getQuantity()));
        cartItem = cartItemRepository.save(cartItem);
    }

    public Cart createCart(){
        //FIND THE CART WITH THE USER EMAIL
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        //IF NOT NULL THAT MEANS THERE IS A CART PRESENT FOR THE EMAIL USER
        if(userCart != null){
            return userCart;
        }
        //IF NOT PRESENT THEN CREATE A CART AND SET THE USER AND THE TOTAL PRICE
        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        //THE CART IS CREATED AND RETURN THE CART
        Cart newCart =  cartRepository.save(cart);
        return newCart;
    }
}
