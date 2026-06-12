package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Product;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecifications {

    public static Specification<Product> hasStatus(Product.Status status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    public static Specification<Product> hasCategory(Product.Category category) {
        return (root, query, cb) -> category != null ? cb.equal(root.get("category"), category) : null;
    }

    public static Specification<Product> nameContains(String searchTerm) {
        return (root, query, cb) ->
            (searchTerm != null && !searchTerm.isBlank())
                ? cb.like(cb.lower(root.get("name")), "%" + searchTerm.toLowerCase() + "%")
                : null;
    }

    public static Specification<Product> orderByPriceAsc() {
        return (root, query, cb) -> {
            query.orderBy(cb.asc(root.get("price")));
            return null;
        };
    }

    public static Specification<Product> orderByPriceDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("price")));
            return null;
        };
    }

    public static Specification<Product> orderByQuantityDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("quantity")));
            return null;
        };
    }
}
