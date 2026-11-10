package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 商品图片对象 sm_product_image
 *
 * @author sm
 */
@Data
@TableName("sm_product_image")
public class ProductImage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 图片URL */
    private String url;

    /** 排序号 */
    private Integer sort;

    /** 是否主图(0否 1是) */
    private String isMain;

    /** 创建时间 */
    private Date createTime;
}
