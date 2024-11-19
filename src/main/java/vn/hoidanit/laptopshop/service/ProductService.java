package vn.hoidanit.laptopshop.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import vn.hoidanit.laptopshop.domain.Cart;
import vn.hoidanit.laptopshop.domain.CartDetail;
import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.domain.OrderDetail;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;
import vn.hoidanit.laptopshop.domain.User;
import vn.hoidanit.laptopshop.repository.CartDetailRepository;
import vn.hoidanit.laptopshop.repository.CartRepository;
import vn.hoidanit.laptopshop.repository.OrderDetailRepository;
import vn.hoidanit.laptopshop.repository.OrderRepository;
import vn.hoidanit.laptopshop.repository.ProductRepository;
import vn.hoidanit.laptopshop.service.specification.ProductSpecs;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailRepository cartDetailRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(
            ProductRepository productRepository,
            CartRepository cartRepository,
            CartDetailRepository cartDetailRepository,
            UserService userService,
            OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Product saveProduct(Product product) {
        return this.productRepository.save(product);
    }

    public Page<Product> fetchProducts(Pageable pageable) {
        return this.productRepository.findAll(pageable);
    }

    // Case 0:
    public Page<Product> fetchProductsWithSpec(Pageable pageable, String name) {
        return this.productRepository.findAll(ProductSpecs.nameLike(name), pageable);
    }

    // Case 1:
    // public Page<Product> fetchProductWithSpec(Pageable pageable, double minPrice)
    // {
    // return this.productRepository.findAll(ProductSpecs.minPrice(minPrice),
    // pageable);
    // }

    // Case 2:
    // public Page<Product> fetchProductWithSpec(Pageable pageable, double maxPrice)
    // {
    // return this.productRepository.findAll(ProductSpecs.maxPrice(maxPrice),
    // pageable);
    // }

    // Case 3:
    // public Page<Product> fetchProductWithSpec(Pageable pageable, String factory)
    // {
    // return this.productRepository.findAll(ProductSpecs.matchFactory(factory),
    // pageable);
    // }

    // Case 4:
    // public Page<Product> fetchProductWithSpec(Pageable pageable, List<String>
    // factory) {
    // return this.productRepository.findAll(ProductSpecs.matchListFactory(factory),
    // pageable);
    // }

    // Case 5:
    // public Page<Product> fetchProductWithSpec(Pageable pageable, String price) {
    // if (price.equals("10-toi-15-trieu")) {
    // double min = 10000000;
    // double max = 15000000;
    // return this.productRepository.findAll(ProductSpecs.matchPrice(min, max),
    // pageable);
    // } else if (price.equals("15-toi-30-trieu")) {
    // double min = 15000000;
    // double max = 30000000;
    // return this.productRepository.findAll(ProductSpecs.matchPrice(min, max),
    // pageable);
    // } else {
    // return this.productRepository.findAll(pageable);
    // }
    // }

    // Case 6:
    public Page<Product> fetchProductWithSpec(Pageable pageable, List<String> price) {
        Specification<Product> combinedSpec = (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        int count = 0;
        for (String p : price) {
            double min = 0;
            double max = 0;

            // Set the appropriate min and max based on the price range string
            switch (p) {
                case "10-toi-15-trieu":
                    min = 10000000;
                    max = 15000000;
                    count++;
                    break;
                case "15-toi-20-trieu":
                    min = 15000000;
                    max = 20000000;
                    count++;
                    break;
                case "20-toi-30-trieu":
                    min = 20000000;
                    max = 30000000;
                    count++;
                    break;
                // Add more cases as needed
            }

            if (min != 0 && max != 0) {
                Specification<Product> rangeSpec = ProductSpecs.matchMultiPrice(min, max);
                combinedSpec = combinedSpec.or(rangeSpec);
            }
        }

        // Check if any price ranges were added (combinedSpec is empty)
        if (count == 0) {
            return this.productRepository.findAll(pageable);
        }
        return this.productRepository.findAll(combinedSpec, pageable);
    }

    // Case 7 (là case 6 nhưng tự viêt):
    // public Page<Product> fetchProductWithSpec(Pageable pageable, String price) {
    // String[] priceAbouts = price.split(",");
    // List<List<String>> listPriceAbouts = new ArrayList<List<String>>();
    // for (String priceAbout : priceAbouts) {
    // String[] handlePriceAbout = priceAbout.split("-");
    // String[] priceAboutOnlyPrice = { handlePriceAbout[0] + "000000",
    // handlePriceAbout[2] + "000000" };
    // List<String> listPriceAboutOnlyPrice = Arrays.asList(priceAboutOnlyPrice);
    // listPriceAbouts.add(listPriceAboutOnlyPrice);
    // }
    // return
    // this.productRepository.findAll(ProductSpecs.pricesAbout(listPriceAbouts),
    // pageable);
    // }

    public Optional<Product> fetchProductById(long id) {
        return this.productRepository.findById(id);
    }

    public void deleteAProductById(long id) {
        this.productRepository.deleteById(id);
    }

    public void handleAddProductToCart(String email, long productId, HttpSession session, long quantity) {
        User user = this.userService.getUserByEmail(email);

        if (user != null) {
            // cart table
            Cart cart = this.cartRepository.findByUser(user);

            // Check user có cart table chưa ? do nothing : insert user vào cart table
            if (cart == null) {
                // tạo mới cart
                Cart otherCart = new Cart();
                otherCart.setUser(user);
                otherCart.setSum(0);
                cart = this.cartRepository.save(otherCart);
            }

            // save card_detail
            // insert product vào card_detail
            Optional<Product> product = this.productRepository.findById(productId);
            if (product.isPresent()) {
                Product realProduct = product.get();

                // Check đã tồn tại product_id và card_id đã có trong cart_detail chưa ? chưa: 1
                // rồi: quantity+1
                CartDetail oldDetail = this.cartDetailRepository.findByCartAndProduct(cart, realProduct);

                if (oldDetail == null) {
                    CartDetail cartDetail = new CartDetail();
                    cartDetail.setCart(cart);
                    cartDetail.setProduct(realProduct);
                    cartDetail.setPrice(realProduct.getPrice());
                    cartDetail.setQuantity(quantity);
                    this.cartDetailRepository.save(cartDetail);

                    // update cart (sum)
                    int s = cart.getSum() + 1;
                    cart.setSum(cart.getSum() + 1);
                    this.cartRepository.save(cart);
                    session.setAttribute("sum", s);
                } else {
                    oldDetail.setQuantity(oldDetail.getQuantity() + quantity);
                    this.cartDetailRepository.save(oldDetail);
                }
            }
        }
    }

    public Cart fetchByUser(User user) {
        return this.cartRepository.findByUser(user);
    }

    public void handleRemoveCardDetail(long cartDetailId, HttpSession session) {
        Optional<CartDetail> cartDetailOptional = this.cartDetailRepository.findById(cartDetailId);
        if (cartDetailOptional.isPresent()) {
            CartDetail cartDetail = cartDetailOptional.get();

            // delete cart_detail
            this.cartDetailRepository.deleteById(cartDetailId);

            // update cart
            Cart currentCart = cartDetail.getCart();
            if (currentCart.getSum() > 1) {
                int sumCart = currentCart.getSum() - 1;
                currentCart.setSum(sumCart);
                session.setAttribute("sum", sumCart);
                this.cartRepository.save(currentCart);
            } else {
                // delete cart when sum = 1
                this.cartRepository.deleteById(currentCart.getId());
                session.setAttribute("sum", 0);
            }
        }
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetail> cartDetails) {
        for (CartDetail cartDetail : cartDetails) {
            Optional<CartDetail> cdOptional = this.cartDetailRepository.findById(cartDetail.getId());
            if (cdOptional.isPresent()) {
                CartDetail currentCartDetail = cdOptional.get();
                currentCartDetail.setQuantity(cartDetail.getQuantity());
                this.cartDetailRepository.save(currentCartDetail);
            }
        }
    }

    public void handlePlaceOrder(
            User user,
            HttpSession session,
            String receiverName, String receiverAddress, String receiverPhone) {

        // step 1: get cart by user
        Cart cart = this.cartRepository.findByUser(user);
        if (cart != null) {
            List<CartDetail> cartDetails = cart.getCartDetails();

            if (cartDetails != null) {

                // Create Order
                Order order = new Order();
                order.setUser(user);
                order.setReceiverName(receiverName);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverPhone(receiverPhone);
                order.setStatus("PENDING");

                double totalPrice = 0;
                for (CartDetail cd : cartDetails) {
                    totalPrice += cd.getPrice() * cd.getQuantity();
                }
                order.setTotalPrice(totalPrice);
                order = this.orderRepository.save(order);

                // Create OrderDetail
                for (CartDetail cd : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(cd.getProduct());
                    orderDetail.setPrice(cd.getPrice());
                    orderDetail.setQuantity(cd.getQuantity());

                    this.orderDetailRepository.save(orderDetail);
                }

                // Step 2: delete cart_detail and cart
                for (CartDetail cd : cartDetails) {
                    this.cartDetailRepository.deleteById(cd.getId());
                }

                this.cartRepository.deleteById(cart.getId());

                // Step 3: update session
                session.setAttribute("sum", 0);
            }
        }

    }

}
