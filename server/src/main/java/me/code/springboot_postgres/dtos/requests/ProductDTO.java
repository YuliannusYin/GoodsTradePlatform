/**
 * @file ProductDTO.java
 * @description 商品请求数据传输对象，用于商品创建和编辑
 * @input 商品名称、描述、图片URL列表、价格、数量、分类、成色、来源
 * @output 包含商品信息的DTO对象
 */
package me.code.springboot_postgres.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品请求DTO
 * 职责：封装创建或编辑商品时提交的商品详细信息
 */
public record ProductDTO(
        @NotBlank String name,
        String description,
        List<String> imageUrls,
        @NotNull @Positive BigDecimal price,
        @NotNull @Min(0) int quantity,
        @NotBlank String category,
        String condition,
        String source) {
}
