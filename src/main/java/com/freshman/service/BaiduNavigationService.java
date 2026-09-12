package com.freshman.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * 百度地图导航服务
 * 功能：调用百度地图 Agent Plan Direction API 获取真实步行路线
 * 负责成员：Z
 * 所属模块：校园导览 / 智能导航
 */
@Service
public class BaiduNavigationService {

    private static final String DIRECTION_API = "https://api.map.baidu.com/agent_plan/v1/direction";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String token;

    public BaiduNavigationService(@Value("${BAIDU_MAP_AUTH_TOKEN:#{null}}") String token) {
        this.token = token;
    }

    /**
     * 获取步行路线
     * @param fromLat 起点纬度 (WGS-84)
     * @param fromLng 起点经度 (WGS-84)
     * @param toLat   终点纬度 (WGS-84)
     * @param toLng   终点经度 (WGS-84)
     * @param toName  终点建筑名
     * @return 路线结果（坐标点列表、距离米、时间秒），异常时返回 null
     */
    public RouteResult getWalkingRoute(double fromLat, double fromLng,
                                        double toLat, double toLng,
                                        String toName) {
        try {
            // 1. WGS-84 → GCJ-02（百度坐标系）
            double[] fromGcj = wgs84ToGcj02(fromLat, fromLng);
            double[] toGcj = wgs84ToGcj02(toLat, toLng);

            // 2. 构造请求参数
            String request = String.format("从我的位置步行到%s", toName.isEmpty() ? "目的地" : toName);
            String url = String.format("%s?user_raw_request=%s&location=%.6f,%.6f",
                    DIRECTION_API,
                    java.net.URLEncoder.encode(request, "UTF-8"),
                    fromGcj[0], fromGcj[1]);

            // 3. 调用百度 API
            String response = restTemplate.getForObject(url, String.class,
                    org.springframework.http.HttpHeaders.AUTHORIZATION, "Bearer " + token);

            // 4. 在 Header 中设置 Authorization
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            var entity = new org.springframework.http.HttpEntity<>(headers);

            var exchangeUrl = url; // rebuild due to URLEncoder
            // Actually let's use a simpler approach
            url = DIRECTION_API
                    + "?user_raw_request=" + java.net.URLEncoder.encode(request, "UTF-8")
                    + "&location=" + String.format("%.6f,%.6f", fromGcj[0], fromGcj[1]);

            var responseEntity = restTemplate.exchange(
                    url, org.springframework.http.HttpMethod.GET, entity, String.class);
            response = responseEntity.getBody();

            // 5. 解析响应
            JsonNode root = objectMapper.readTree(response);
            int status = root.get("status").asInt();
            if (status != 0) {
                System.err.println("百度导航API错误: status=" + status + ", msg=" + root.get("message"));
                return null;
            }

            JsonNode result = root.get("result");
            if (result == null) return null;

            // 检查是否为路线结果
            String answerType = result.get("answer_type") != null ? result.get("answer_type").asText() : "";
            if (!"gptmodel_navigate".equals(answerType)) {
                // 可能是地点澄清，走直线兜底
                return fallbackRoute(fromLat, fromLng, toLat, toLng);
            }

            // 提取路线中的坐标
            JsonNode routes = result.get("routes");
            if (routes == null || !routes.isArray() || routes.size() == 0) {
                return fallbackRoute(fromLat, fromLng, toLat, toLng);
            }

            JsonNode route = routes.get(0);
            double distanceMeters = route.get("distance") != null ? route.get("distance").asDouble() : 0;
            double durationSec = route.get("duration") != null ? route.get("duration").asDouble() : 0;

            // 提取各步骤中的坐标点
            List<double[]> points = new ArrayList<>();
            JsonNode steps = route.get("steps");
            if (steps != null && steps.isArray()) {
                for (JsonNode step : steps) {
                    JsonNode start = step.get("start_location");
                    if (start != null) {
                        double lat = start.get("lat").asDouble();
                        double lng = start.get("lng").asDouble();
                        double[] wgs = gcj02ToWgs84(lat, lng);
                        points.add(new double[]{wgs[0], wgs[1]});
                    }
                    JsonNode end = step.get("end_location");
                    if (end != null) {
                        double lat = end.get("lat").asDouble();
                        double lng = end.get("lng").asDouble();
                        double[] wgs = gcj02ToWgs84(lat, lng);
                        points.add(new double[]{wgs[0], wgs[1]});
                    }
                    // 尝试提取轨迹点
                    JsonNode path = step.get("path");
                    if (path != null && path.isArray()) {
                        for (JsonNode p : path) {
                            double lat = p.get("lat").asDouble();
                            double lng = p.get("lng").asDouble();
                            double[] wgs = gcj02ToWgs84(lat, lng);
                            points.add(new double[]{wgs[0], wgs[1]});
                        }
                    }
                }
            }

            if (points.isEmpty()) {
                return fallbackRoute(fromLat, fromLng, toLat, toLng);
            }

            RouteResult rr = new RouteResult();
            rr.points = points;
            rr.distanceMeters = distanceMeters > 0 ? distanceMeters : calcDistance(fromLat, fromLng, toLat, toLng);
            rr.durationSeconds = durationSec > 0 ? durationSec : rr.distanceMeters / 1.3;
            return rr;

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackRoute(fromLat, fromLng, toLat, toLng);
        }
    }

    /** 直线兜底方案 */
    private RouteResult fallbackRoute(double fromLat, double fromLng, double toLat, double toLng) {
        double dist = calcDistance(fromLat, fromLng, toLat, toLng);
        RouteResult rr = new RouteResult();
        rr.points = List.of(new double[]{fromLat, fromLng}, new double[]{toLat, toLng});
        rr.distanceMeters = dist;
        rr.durationSeconds = dist / 1.3;
        return rr;
    }

    /** WGS-84 → GCJ-02 */
    public static double[] wgs84ToGcj02(double lat, double lng) {
        // 先判断是否在中国境内，不在则不需要偏移
        if (lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271) {
            return new double[]{lat, lng};
        }
        double[] d = delta(lat, lng);
        return new double[]{lat + d[0], lng + d[1]};
    }

    /** GCJ-02 → WGS-84 */
    public static double[] gcj02ToWgs84(double lat, double lng) {
        if (lng < 72.004 || lng > 137.8347 || lat < 0.8293 || lat > 55.8271) {
            return new double[]{lat, lng};
        }
        double[] d = delta(lat, lng);
        return new double[]{lat - d[0], lng - d[1]};
    }

    private static double[] delta(double lat, double lng) {
        double pi = Math.PI;
        double a = 6378245.0;
        double ee = 0.00669342162296594323;
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        return new double[]{dLat, dLng};
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320.0 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    /** Haversine 距离 */
    private static double calcDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000;
        double dLat = (lat2 - lat1) * Math.PI / 180;
        double dLng = (lng2 - lng1) * Math.PI / 180;
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(lat1*Math.PI/180)*Math.cos(lat2*Math.PI/180)*Math.sin(dLng/2)*Math.sin(dLng/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    /** 路线结果 */
    public static class RouteResult {
        public List<double[]> points;      // [lat, lng] WGS-84
        public double distanceMeters;      // 米
        public double durationSeconds;     // 秒
    }
}
