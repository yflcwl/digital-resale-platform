package com.sm.inspection.constant;

/**
 * 质检任务状态常量
 *
 * @author sm
 */
public interface InspectionStatus {

    /** 已提交(已投递 MQ) */
    String SUBMITTED = "SUBMITTED";

    /** 处理中(智能体执行质检流程) */
    String PROCESSING = "PROCESSING";

    /** 成功(已生成报告) */
    String SUCCESS = "SUCCESS";

    /** 需补拍 */
    String NEED_RESHOOT = "NEED_RESHOOT";

    /** 失败 */
    String FAILED = "FAILED";
}
