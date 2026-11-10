package com.sm.trade.constant;

/**
 * 商品状态常量
 *
 * @author sm
 */
public interface ProductStatus {

    /** 草稿 */
    String DRAFT = "DRAFT";

    /** 待质检 */
    String WAITING_QC = "WAITING_QC";

    /** 在售 */
    String ON_SALE = "ON_SALE";

    /** 已锁定(存在进行中订单) */
    String LOCKED = "LOCKED";

    /** 已售出 */
    String SOLD = "SOLD";

    /** 已下架 */
    String OFF_SHELF = "OFF_SHELF";

    /** 审核中(预留) */
    String AUDITING = "AUDITING";

    /** 审核拒绝(预留) */
    String REJECTED = "REJECTED";
}
