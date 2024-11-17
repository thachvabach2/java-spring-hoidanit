package vn.hoidanit.laptopshop.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.hoidanit.laptopshop.domain.Order;
import vn.hoidanit.laptopshop.service.OrderService;

@Controller
public class OrderController {

    public final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/admin/order")
    public String getOrderPage(Model model, @RequestParam("page") Optional<String> currentPageOptional) {
        int currentPage = 1;
        try {
            if (currentPageOptional.isPresent()) {
                // convert String to int
                currentPage = Integer.parseInt(currentPageOptional.get());
            } else {
                // user không truyền ?page=... thì k cần làm gì cả vì currentPage mặc định = 1
                // currentPage = 1
            }
        } catch (Exception e) {
            // currentPage = 1
            // TODO: handle exception
        }

        Pageable pageable = PageRequest.of(currentPage - 1, 1);
        Page<Order> ordersPage = this.orderService.fetchAllOrders(pageable);
        List<Order> orders = ordersPage.getContent();

        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        return "admin/order/show";
    }

    @GetMapping("/admin/order/{id}")
    public String getOrderDetailPage(Model model, @PathVariable long id) {
        Order order = this.orderService.fetchOrderById(id).get();

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", order.getOrderDetails());

        return "admin/order/detail";
    }

    // Delete Order
    @GetMapping("/admin/order/delete/{id}")
    public String getDeleteOrderPage(Model model, @PathVariable long id) {
        model.addAttribute("id", id);
        model.addAttribute("newOrder", new Order());
        return "admin/order/delete";
    }

    @PostMapping("/admin/order/delete")
    public String postDeleteAOrder(@ModelAttribute("newOrder") Order order) {
        this.orderService.deleteOrderById(order.getId());
        return "redirect:/admin/order";
    }

    // Update Order
    @GetMapping("/admin/order/update/{id}")
    public String getUpdateOrderPage(Model model, @PathVariable long id) {
        Order currentOrder = this.orderService.fetchOrderById(id).get();
        model.addAttribute("newOrder", currentOrder);
        return "admin/order/update";
    }

    @PostMapping("/admin/order/update")
    public String postUpdateOrder(@ModelAttribute("newOrder") Order updatedOrder) {
        this.orderService.updateOrder(updatedOrder);
        return "redirect:/admin/order";
    }

}
