package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sm.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品分类对象 sm_category
 *
 * @author sm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sm_category")
public class Category extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 父分类ID(0为顶级) */
    private Long parentId;

    /** 分类名称 */
    private String name;

    /** 图标URL */
    private String icon;

    /** 层级:1/2/3 */
    private Integer level;

    /** 排序号 */
    private Integer sort;

    /** 状态(0启用 1停用) */
    private String status;
}
