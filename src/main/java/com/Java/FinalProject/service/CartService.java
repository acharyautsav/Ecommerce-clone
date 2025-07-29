package com.Java.FinalProject.service;

import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.entity.CustomerOrder;
import com.Java.FinalProject.entity.ItemsOrdered;
import com.Java.FinalProject.entity.Product;
import com.Java.FinalProject.repository.CustomerOrderRepository;
import com.Java.FinalProject.repository.ItemsOrderedRepository;
import com.Java.FinalProject.repository.ProductRepository;
import com.Java.FinalProject.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class CartService {
    @Autowired
    private CustomerOrderRepository customerOrderRepository;
    @Autowired
    private ItemsOrderedRepository itemsOrderedRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerRepository customerRepository;

    // Find or create a cart for the customer
    public CustomerOrder getOrCreateCart(Customer customer) {
        return customerOrderRepository.findByCustomerAndOrderStatus(customer, "CART")
                .orElseGet(() -> {
                    CustomerOrder cart = new CustomerOrder();
                    cart.setCustomer(customer);
                    cart.setOrderStatus("CART");
                    return customerOrderRepository.save(cart);
                });
    }

    // Add a product to the cart
    public void addToCart(Customer customer, Long productId, int quantity) {
        CustomerOrder cart = getOrCreateCart(customer);
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) throw new RuntimeException("Product not found");
        Product product = productOpt.get();
        // Check if item already in cart
        for (ItemsOrdered item : cart.getItems()) {
            if (item.getProduct().getProductId().equals(productId)) {
                item.setItemsOrderedQuantity(item.getItemsOrderedQuantity() + quantity);
                itemsOrderedRepository.save(item);
                return;
            }
        }
        ItemsOrdered newItem = new ItemsOrdered();
        newItem.setCustomerOrder(cart);
        newItem.setProduct(product);
        newItem.setItemsOrderedQuantity(quantity);
        newItem.setItemsOrderedPrice(product.getPrice().doubleValue());
        itemsOrderedRepository.save(newItem);
        cart.getItems().add(newItem);
        customerOrderRepository.save(cart);
    }

    // Get all items in the cart
    public List<ItemsOrdered> getCartItems(Customer customer) {
        CustomerOrder cart = getOrCreateCart(customer);
        return itemsOrderedRepository.findByCustomerOrder(cart);
    }

    // Remove an item from the cart
    public void removeFromCart(Customer customer, Long itemId) {
        CustomerOrder cart = getOrCreateCart(customer);
        itemsOrderedRepository.findById(itemId).ifPresent(item -> {
            if (item.getCustomerOrder().getOrderId().equals(cart.getOrderId())) {
                itemsOrderedRepository.delete(item);
            }
        });
    }

    // Increase or decrease quantity of a cart item
    public void changeQuantity(Customer customer, Long itemId, int delta) {
        CustomerOrder cart = getOrCreateCart(customer);
        itemsOrderedRepository.findById(itemId).ifPresent(item -> {
            if (item.getCustomerOrder().getOrderId().equals(cart.getOrderId())) {
                int newQty = item.getItemsOrderedQuantity() + delta;
                if (newQty <= 0) {
                    itemsOrderedRepository.delete(item);
                } else {
                    item.setItemsOrderedQuantity(newQty);
                    itemsOrderedRepository.save(item);
                }
            }
        });
    }

    // Clear the cart
    public void clearCart(Customer customer) {
        CustomerOrder cart = getOrCreateCart(customer);
        List<ItemsOrdered> items = itemsOrderedRepository.findByCustomerOrder(cart);
        for (ItemsOrdered item : items) {
            itemsOrderedRepository.delete(item);
        }
        // Also delete the cart itself
        customerOrderRepository.delete(cart);
    }
    
    // Clear cart by customer ID (for payment processing)
    public void clearCartByCustomerId(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null) {
            clearCart(customer);
        }
    }
    
    // Get cart items by customer ID (for payment processing)
    public List<ItemsOrdered> getCartItemsByCustomerId(Long customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null) {
            return getCartItems(customer);
        }
        return new ArrayList<>();
    }

    // Buy now - clear cart and add single item
    public void buyNow(Customer customer, Long productId, int quantity) {
        clearCart(customer);
        addToCart(customer, productId, quantity);
    }
} 