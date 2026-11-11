package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sm.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 首页轮播图对象 sm_carousel
 *
 * @author sm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sm_carousel")
public class Carousel extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 标题 */
    private String title;

    /** 图片URL */
    private String imageUrl;

    /** 跳转链接 */
    private String link;

    /** 排序号 */
    private Integer sort;

    /** 状态(0展示 1隐藏) */
    private String status;

    /** 删除标志 */
    @TableLogic
    private String delFlag;
}
