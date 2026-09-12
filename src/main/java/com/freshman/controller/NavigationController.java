package com.freshman.controller;

import com.freshman.service.BaiduNavigationService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能导航 API 控制器
 * 功能：提供步行路线规划接口，前端 Leaflet 地图调用后绘制真实路线
 * 负责成员：Z
 * 所属模块：校园导览 / 智能导航
 */
@RestController
@RequestMapping("/api")
public class NavigationController {

    private final BaiduNavigationService navService;

    public NavigationController(BaiduNavigationService navService) {
        this.navService = navService;
    }

    /**
     * 获取步行导航路线
     * @param fromLat 起点纬度 (WGS-84)
     * @param fromLng 起点经度 (WGS-84)
     * @param toLat   终点纬度 (WGS-84)
     * @param toLng   终点经度 (WGS-84)
     * @param toName  终点建筑名称（可选）
     * @return { status, points: [[lat,lng],...], distance, duration }
     */
    @GetMapping("/navigation")
    public Map<String, Object> getNavigation(
            @RequestParam double fromLat,
            @RequestParam double fromLng,
            @RequestParam double toLat,
            @RequestParam double toLng,
            @RequestParam(defaultValue = "") String toName) {

        Map<String, Object> result = new HashMap<>();
        try {
            BaiduNavigationService.RouteResult route = navService.getWalkingRoute(
                    fromLat, fromLng, toLat, toLng, toName);

            if (route == null || route.points == null || route.points.isEmpty()) {
                result.put("status", 1);
                result.put("message", "导航线路计算失败");
                return result;
            }

            // 将坐标点转为前端友好格式 [[lat,lng], ...]
            double[][] pts = new double[route.points.size()][2];
            for (int i = 0; i < route.points.size(); i++) {
                pts[i] = route.points.get(i);
            }

            // 判断是否兜底（直线方案）
            boolean isFallback = route.points.size() == 2;

            result.put("status", 0);
            result.put("points", pts);
            result.put("distance", Math.round(route.distanceMeters));
            result.put("duration", Math.round(route.durationSeconds));
            result.put("isFallback", isFallback);
            result.put("message", isFallback ? "已为您估算直线距离（百度路线规划暂不可用）" : "");

        } catch (Exception e) {
            result.put("status", -1);
            result.put("message", "导航服务异常: " + e.getMessage());
        }
        return result;
    }
}
