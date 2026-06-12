/**
 * @file ProductSpecifications.java
 * @description 商品动态查询条件构造类，提供按状态、分类、名称过滤和排序的Specification
 * @input 商品状态、分类、搜索关键词
 * @output JPA Specification查询条件
 */
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.Product;
import org.springframework.data.jpa.domain.Specification;

/**
 * 商品动态查询条件构造类
 * 职责：提供可组合的商品查询条件，用于实现动态筛选和排序
 */
public class ProductSpecifications {

    /**
     * 按审核状态过滤条件
     * @param status 商品状态
     * @return 状态过滤Specification
     */
    public static Specification<Product> hasStatus(Product.Status status) {
        return (root, query, cb) -> status != null ? cb.equal(root.get("status"), status) : null;
    }

    /**
     * 按商品分类过滤条件
     * @param category 商品分类
     * @return 分类过滤Specification
     */
    public static Specification<Product> hasCategory(Product.Category category) {
        return (root, query, cb) -> category != null ? cb.equal(root.get("category"), category) : null;
    }

    /**
     * 按商品名称模糊搜索条件（不区分大小写）
     * @param searchTerm 搜索关键词
     * @return 名称模糊搜索Specification
     */
    public static Specification<Product> nameContains(String searchTerm) {
        return (root, query, cb) ->
            (searchTerm != null && !searchTerm.isBlank())
                ? cb.like(cb.lower(root.get("name")), "%" + searchTerm.toLowerCase() + "%")
                : null;
    }

    /**
     * 按价格升序排序条件
     * @return 价格升序排序Specification
     */
    public static Specification<Product> orderByPriceAsc() {
        return (root, query, cb) -> {
            query.orderBy(cb.asc(root.get("price")));
            return null;
        };
    }

    /**
     * 按价格降序排序条件
     * @return 价格降序排序Specification
     */
    public static Specification<Product> orderByPriceDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("price")));
            return null;
        };
    }

    /**
     * 按库存数量降序排序条件
     * @return 库存降序排序Specification
     */
    public static Specification<Product> orderByQuantityDesc() {
        return (root, query, cb) -> {
            query.orderBy(cb.desc(root.get("quantity")));
            return null;
        };
    }
}
