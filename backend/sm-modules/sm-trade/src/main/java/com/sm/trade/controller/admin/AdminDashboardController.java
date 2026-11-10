package com.sm.trade.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.sm.common.core.domain.R;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.mapper.AdminInspectionMapper;
import com.sm.trade.mapper.AdminStatMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台:仪表盘 + 质检报告查阅
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
@SaCheckRole("superadmin")
public class AdminDashboardController {

    private final AdminStatMapper statMapper;
    private final AdminInspectionMapper inspectionMapper;

    /**
     * 仪表盘数据
     */
    @GetMapping("/dashboard")
    public R<Map<String, Object>> dashboard() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overview", statMapper.selectOverview());
        result.put("orderTrend", statMapper.selectOrderTrend7d());
        result.put("orderStatusDist", statMapper.selectOrderStatusDist());
        result.put("categoryDist", statMapper.selectCategoryProductDist(8));
        return R.ok(result);
    }

    /**
     * 质检报告分页
     */
    @GetMapping("/inspection/report/page")
    public TableDataInfo<Map<String, Object>> reportPage(@RequestParam(required = false) String grade,
                                                         @RequestParam(required = false) String keyword,
                                                         PageQuery pageQuery) {
        long pageNum = pageQuery.getPageNum() == null ? 1 : pageQuery.getPageNum();
        long pageSize = pageQuery.getPageSize() == null ? 10 : pageQuery.getPageSize();
        List<Map<String, Object>> rows = inspectionMapper.selectReportPage(
            (pageNum - 1) * pageSize, pageSize, grade, keyword);
        long total = inspectionMapper.selectReportCount(grade, keyword);
        return new TableDataInfo<>(rows, total);
    }

    /**
     * 质检报告详情(含缺陷明细)
     */
    @GetMapping("/inspection/report/{id}")
    public R<Map<String, Object>> reportDetail(@PathVariable Long id) {
        Map<String, Object> report = inspectionMapper.selectReportDetail(id);
        if (report == null) {
            return R.fail("报告不存在");
        }
        report.put("defects", inspectionMapper.selectReportDefects(id));
        return R.ok(report);
    }
}
