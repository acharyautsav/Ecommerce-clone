package com.Java.FinalProject.controller;

import com.Java.FinalProject.entity.SuperAdmin;
import com.Java.FinalProject.entity.Seller;
import com.Java.FinalProject.entity.Customer;
import com.Java.FinalProject.entity.ProductCategory;
import com.Java.FinalProject.entity.Product;
import com.Java.FinalProject.repository.ProductCategoryRepository;
import com.Java.FinalProject.repository.ProductRepository;
import com.Java.FinalProject.repository.ItemsOrderedRepository;
import com.Java.FinalProject.service.SuperAdminService;
import com.Java.FinalProject.service.SellerService;
import com.Java.FinalProject.service.CustomerService;
import com.Java.FinalProject.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {
    @Autowired
    private SuperAdminService superAdminService;
    @Autowired
    private SellerService sellerService;
    @Autowired
    private CustomerService customerService;
    @Autowired
    private ProductCategoryRepository productCategoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ItemsOrderedRepository itemsOrderedRepository;

    @Autowired
    private ProductService productService;

    @GetMapping("/login")
    public String loginPage() {
        return "superadmin/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session) {
        Optional<SuperAdmin> adminOpt = superAdminService.authenticate(username, password);
        if (adminOpt.isPresent()) {
            session.setAttribute("superadminId", adminOpt.get().getId());
            session.setAttribute("superadminUsername", adminOpt.get().getUsername());
            return "redirect:/superadmin/dashboard";
        }
        return "redirect:/superadmin/login?error=1";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (session.getAttribute("superadminId") == null) {
            return "redirect:/superadmin/login";
        }
        
        // Get real data counts
        long totalSellers = sellerService.getAllSellers().size();
        long totalCustomers = customerService.getAllCustomers().size();
        long totalCategories = productCategoryRepository.count();
        long totalProducts = productRepository.count();
        
        // For now, we'll assume all sellers are active and none are pending
        // In a real application, you'd have status fields to determine this
        long activeSellers = totalSellers;
        long pendingSellers = 0; // Assuming no pending sellers for now
        
        model.addAttribute("superadminUsername", session.getAttribute("superadminUsername"));
        model.addAttribute("activeSellers", activeSellers);
        model.addAttribute("pendingSellers", pendingSellers);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalProducts", totalProducts);
        
        return "superadmin/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/superadmin/login";
    }

    @GetMapping("/sellers")
    public String manageSellers(HttpSession session, Model model) {
        if (session.getAttribute("superadminId") == null) {
            return "redirect:/superadmin/login";
        }
        List<Seller> sellers = sellerService.getAllSellers();
        model.addAttribute("sellers", sellers);
        return "superadmin/sellers";
    }

    @GetMapping("/customers")
    public String manageCustomers(HttpSession session, Model model) {
        if (session.getAttribute("superadminId") == null) {
            return "redirect:/superadmin/login";
        }
        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);
        return "superadmin/customers";
    }

    @GetMapping("/categories")
    public String manageCategories(HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            return "redirect:/superadmin/login";
        }
        return "superadmin/categories";
    }

    @GetMapping("/products")
    public String manageProducts(HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            return "redirect:/superadmin/login";
        }
        return "superadmin/products";
    }

    // --- REST API for Sellers ---
    @GetMapping("/api/sellers")
    @ResponseBody
    public List<Seller> apiGetAllSellers(HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        List<Seller> sellers = sellerService.getAllSellers();
        System.out.println("Found " + sellers.size() + " sellers in database");
        for (Seller seller : sellers) {
            System.out.println("Seller ID: " + seller.getSellerId() + ", Name: " + seller.getSellerName() + ", Email: " + seller.getSellerEmail());
        }
        return sellers;
    }

    @GetMapping("/api/sellers/{id}")
    @ResponseBody
    public Seller apiGetSellerById(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        System.out.println("Fetching seller with ID: " + id);
        Seller seller = sellerService.getSellerById(id).orElseThrow(() -> new RuntimeException("Seller not found"));
        System.out.println("Found seller: " + seller.getSellerName() + ", Email: " + seller.getSellerEmail());
        
        // Decrypt password for editing if it's encrypted, otherwise show as-is
        if (seller.getSellerPassword() != null) {
            if (sellerService.isPasswordEncrypted(seller.getSellerPassword())) {
                String decryptedPassword = sellerService.decryptPassword(seller.getSellerPassword());
                seller.setSellerPassword(decryptedPassword);
                System.out.println("Password was encrypted, decrypted for editing");
            } else {
                System.out.println("Password was plain text, showing as-is");
            }
            // If not encrypted, keep as-is (plain text)
        }
        
        return seller;
    }

    @PostMapping("/api/sellers")
    @ResponseBody
    public Seller apiAddSeller(@RequestBody Seller seller, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        return sellerService.saveSeller(seller);
    }

    @PutMapping("/api/sellers/{id}")
    @ResponseBody
    public Seller apiUpdateSeller(@PathVariable Long id, @RequestBody Seller seller, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        seller.setSellerId(id);
        return sellerService.updateSeller(seller);
    }

    @DeleteMapping("/api/sellers/{id}")
    @ResponseBody
    public void apiDeleteSeller(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        sellerService.deleteSeller(id);
    }

    // --- REST API for Customers ---
    @GetMapping("/api/customers")
    @ResponseBody
    public List<Customer> apiGetAllCustomers(HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        return customerService.getAllCustomers();
    }

    @GetMapping("/api/customers/{id}")
    @ResponseBody
    public Customer apiGetCustomerById(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        Customer customer = customerService.getCustomerById(id).orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Decrypt password for editing
        if (customer.getCustomerPassword() != null) {
            String decryptedPassword = customerService.decryptPassword(customer.getCustomerPassword());
            customer.setCustomerPassword(decryptedPassword);
        }
        
        return customer;
    }

    @PostMapping("/api/customers")
    @ResponseBody
    public Customer apiAddCustomer(@RequestBody Customer customer, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        return customerService.saveCustomer(customer);
    }

    @PutMapping("/api/customers/{id}")
    @ResponseBody
    public Customer apiUpdateCustomer(@PathVariable Long id, @RequestBody Customer customer, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        customer.setCustomerId(id);
        return customerService.updateCustomer(customer);
    }

    @DeleteMapping("/api/customers/{id}")
    @ResponseBody
    public void apiDeleteCustomer(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        customerService.deleteCustomer(id);
    }

    // --- REST API for Categories ---
    @GetMapping("/api/categories")
    @ResponseBody
    public List<ProductCategory> apiGetAllCategories(HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        return productCategoryRepository.findAll();
    }

    @PostMapping("/api/categories")
    @ResponseBody
    public ProductCategory apiAddCategory(@RequestBody ProductCategory category, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        return productCategoryRepository.save(category);
    }

    @PutMapping("/api/categories/{id}")
    @ResponseBody
    public ProductCategory apiUpdateCategory(@PathVariable Integer id, @RequestBody ProductCategory category, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        category.setCategoryId(id);
        return productCategoryRepository.save(category);
    }

    @DeleteMapping("/api/categories/{id}")
    @ResponseBody
    public void apiDeleteCategory(@PathVariable Integer id, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        productCategoryRepository.deleteById(id);
    }

    // --- REST API for Products ---
    @GetMapping("/api/products")
    @ResponseBody
    public List<Product> apiGetAllProducts(HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        return productRepository.findAll();
    }

    @PutMapping("/api/products/{id}")
    @ResponseBody
    public Product apiUpdateProduct(@PathVariable Long id, @RequestBody Product product, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        Product existing = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        existing.setProductName(product.getProductName());
        existing.setCategory(product.getCategory());
        existing.setPrice(product.getPrice());
        existing.setDescription(product.getDescription());
        // Optionally allow changing isActive or other fields if needed
        return productRepository.save(existing);
    }

    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public void apiDeleteProduct(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("superadminId") == null) {
            throw new RuntimeException("Unauthorized");
        }
        productService.hardDeleteProduct(id);
    }
} 