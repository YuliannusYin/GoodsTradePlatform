/**
 * @file OrderItemService.java
 * @description 订单项服务类，负责生成订单项列表和计算总价
 * @input 商品列表
 * @output 订单项列表或总价
 */
package me.code.springboot_postgres.services;

import lombok.NoArgsConstructor;
import me.code.springboot_postgres.models.entities.OrderItem;
import me.code.springboot_postgres.models.entities.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单项服务类
 * 职责：根据商品列表生成订单项（自动合并相同商品），并计算订单总价
 */
@Service
@NoArgsConstructor
public class OrderItemService {

    /**
     * 根据商品列表生成订单项，相同商品自动合并数量
     * @param products 商品列表
     * @return 订单项列表
     */
    public List<OrderItem> generateOrderItems(List<Product> products) {
        List<OrderItem> items = new ArrayList<>();

        for (var product : products) {
            // 添加或更新订单项（相同商品合并数量）
            addOrUpdateOrderItems(product, items);
        }
        return items;
    }

    /**
     * 添加新订单项或更新已有订单项的数量
     * @param product 商品
     * @param items 订单项列表
     */
    private void addOrUpdateOrderItems(Product product, List<OrderItem> items) {
        for (var item : items) {
            // 商品已存在于订单项中，数量加1
            if (isMatchingProduct(product, item)) {
                updateExistingOrderItem(item);
                return;
            }
        }
        // 商品不存在于订单项中，新增一条
        addNewOrderItem(product, items);
    }

    /**
     * 判断商品是否与订单项中的商品匹配
     * @param product 商品
     * @param item 订单项
     * @return 是否匹配
     */
    private boolean isMatchingProduct(Product product, OrderItem item) {
        Product productInItem = item.getProduct();
        return product.getId().equals(productInItem.getId());
    }

    /**
     * 更新已有订单项的数量和小计价格
     * @param item 订单项
     */
    private void updateExistingOrderItem(OrderItem item) {
        item.setAmount(item.getAmount() + 1);
        // 重新计算小计价格
        item.setPrice(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getAmount())));
    }

    /**
     * 添加新的订单项（数量为1）
     * 构造函数已自动计算 price = 单价 × 数量，无需再次设置
     * @param product 商品
     * @param items 订单项列表
     */
    private void addNewOrderItem(Product product, List<OrderItem> items) {
        var newDetail = new OrderItem(product, 1);
        items.add(newDetail);
    }

    /**
     * 计算订单项列表的总价
     * @param items 订单项列表
     * @return 总价
     */
    public BigDecimal getTotalPrice(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
