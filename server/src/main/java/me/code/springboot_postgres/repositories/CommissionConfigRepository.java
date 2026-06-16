/**
 * @file CommissionConfigRepository.java
 * @description 佣金配置数据访问接口，提供佣金配置的查询和保存方法
 * @input 无
 * @output 佣金配置实体
 */
package me.code.springboot_postgres.repositories;

import me.code.springboot_postgres.models.entities.CommissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 佣金配置数据访问接口
 * 职责：提供佣金配置的CRUD操作，全局仅一条记录
 */
public interface CommissionConfigRepository extends JpaRepository<CommissionConfig, String> {
}
