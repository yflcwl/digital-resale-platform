package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sm.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 品牌对象 sm_brand
 *
 * @author sm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sm_brand")
public class Brand extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 品牌名称 */
    private String name;

    /** Logo URL */
    private String logo;

    /** 状态(0启用 1停用) */
    private String status;

    /** 排序号 */
    private Integer sort;
}
