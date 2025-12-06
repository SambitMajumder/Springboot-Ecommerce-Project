package com.ecommerce.SamCommerce.Services;

import com.ecommerce.SamCommerce.Entity.*;
import com.ecommerce.SamCommerce.Exceptions.APIException;
import com.ecommerce.SamCommerce.Exceptions.ResourceNotFoundException;
import com.ecommerce.SamCommerce.Payload.OrderDTO;
import com.ecommerce.SamCommerce.Payload.OrderItemDTO;
import com.ecommerce.SamCommerce.Repositories.*;
import com.ecommerce.SamCommerce.Util.AuthUtil;
import jakarta.persistence.AttributeOverride;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImplementation implements OrderService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Transactional
    @Override
    public OrderDTO placeOrder(String email, Integer addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        //GETTING THE USER CART
        String loggedEmail = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(loggedEmail);
        if(cart == null){
            throw new ResourceNotFoundException("Cart", "email", loggedEmail);
        }
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
        //CREATE A NEW ORDER
        Order order = new Order();
        order.setEmail(loggedEmail);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted!");
        order.setAddress(address);
        //SET THE PAYMENT INFO INTO THE ORDER
        //CREATING THE PAYMENT OBJECT
        Payment payment = new Payment(paymentMethod, pgPaymentId, pgStatus, pgName, pgResponseMessage);
        //SETTING THE PAYMENT OBJECT INTO PAYMENT
        payment.setOrder(order);
        //SAVING THE PAYMENT
        payment = paymentRepository.save(payment);
        //SETTING THE PAYMENT INTO THE ORDER
        order.setPayment(payment);
        //SAVING THE ORDER
        Order savedOrder = orderRepository.save(order);
        //GET ITEMS FROM THE CART INTO THE ORDER ITEMS
        List<CartItem> cartItems = cart.getCartItems();
        if(cartItems == null || cartItems.isEmpty()){
            throw new APIException("Cart is Empty");
        }
        //WE HAVE THE CARD ITEMS NOW SET THE ITEMS IN THE ORDER ITEMS
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            //ONE ORDER ITEM WILL HOLD ALL THE DETAILS
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(order);
            //ADD THE ORDER ITEM TO ORDER ITEMS ARRAYLIST
            orderItems.add(orderItem);
        }
        //SAVE THE ORDER ITEMS
        orderItems = orderItemRepository.saveAll(orderItems);
        //UPDATE PRODUCT STOCK MEANS IF SOMEONE ADDED ONE PRODUCT REDUCE THE PRODUCT FROM INVENTORY
        cart.getCartItems().forEach(item -> {
            int quantity = item.getQuantity();
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);
            //CLEAR THE CART
            cartService.deleteFromCart(cart.getCartId(), product.getProductID());
        });

        //SEND BACK THE ORDER SUMMARY
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        orderItems.forEach(item->{
            orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class));
        });
        orderDTO.setAddressId(addressId);
        return orderDTO;
    }
}
