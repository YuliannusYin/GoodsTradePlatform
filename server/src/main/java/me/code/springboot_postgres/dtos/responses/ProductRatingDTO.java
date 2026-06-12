/**
 * @file ProductRatingDTO.java
 * @description 商品评分统计响应数据传输对象
 * @input 无（由服务层构造）
 * @output 平均评分和评价数量
 */
package me.code.springboot_postgres.dtos.responses;

/**
 * 商品评分统计响应DTO
 * 职责：封装商品的平均评分和评价总数
 */
public record ProductRatingDTO(double averageRating, int reviewCount) {}
