package vn.hoidanit.laptopshop.service.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import vn.hoidanit.laptopshop.domain.Product;
import vn.hoidanit.laptopshop.domain.Product_;

public class ProductSpecs {
    public static Specification<Product> nameLike(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(Product_.NAME), "%" + name + "%");
    }

    // Case 1
    public static Specification<Product> minPrice(double price) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.ge(root.get(Product_.PRICE), price);
    }

    // Case 2:
    public static Specification<Product> maxPrice(double maxPrice) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.le(root.get(Product_.PRICE), maxPrice);
    }

    // Case 3:
    public static Specification<Product> matchFactory(String factory) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(Product_.FACTORY), factory);
    }

    // Case 4:
    public static Specification<Product> matchListFactory(List<String> factories) {
        return (root, query, criteriaBuilder) -> {
            // C1:
            // Expression<String> parentExpression = root.get(Product_.FACTORY);
            // return parentExpression.in(factories);

            // C2:
            return criteriaBuilder.in(root.get(Product_.FACTORY)).value(factories);
        };
    }

    // Case 5
    public static Specification<Product> matchPrice(double min, double max) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                (criteriaBuilder.gt(root.get(Product_.PRICE), min)),
                (criteriaBuilder.le(root.get(Product_.PRICE), max)));
    }

    // Case 6
    public static Specification<Product> matchMultiPrice(double min, double max) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.between(root.get(Product_.PRICE), min, max);
    }

    // Case 7:
    public static Specification<Product> pricesAbout(List<List<String>> listPriceAbouts) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (List<String> listPriceAbout : listPriceAbouts) {
                Predicate pred = criteriaBuilder.between(root.get(Product_.PRICE), listPriceAbout.get(0),
                        listPriceAbout.get(1));
                predicates.add(pred);
            }

            Predicate finalQuery = criteriaBuilder.or(predicates.toArray(new Predicate[0]));
            return finalQuery;
        };
    }
}
