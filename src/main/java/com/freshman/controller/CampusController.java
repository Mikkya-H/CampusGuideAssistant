package com.freshman.controller;

import com.freshman.entity.*;
import com.freshman.mapper.*;
import com.freshman.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * 校园导览控制器
 * 功能：负责校园建筑/地标的展示和详情查看，帮助新生快速了解校园环境
 * 负责成员：Z
 * 所属模块：校园导览模块
 *
 * @author Z
 * @version 1.0
 */
@Controller
@RequestMapping("/campus")
public class CampusController {

    private final BuildingMapper buildingMapper;

    /**
     * 构造器注入BuildingMapper
     * @param buildingMapper 校园建筑数据访问层
     */
    public CampusController(BuildingMapper buildingMapper) {
        this.buildingMapper = buildingMapper;
    }

    /**
     * 校园导览首页
     * 功能：查询并展示所有校园建筑/地标信息
     *
     * @param model Spring MVC的Model对象
     * @return 校园导览首页视图
     */
    @GetMapping
    public String index(Model model) {
        // 查询所有建筑信息
        List<Building> buildings = buildingMapper.selectList(null);
        model.addAttribute("buildings", buildings);
        return "campus/index";
    }

    /**
     * 建筑详情页（含智能导航）
     * 功能：展示建筑详细信息，同时加载所有建筑用于地图导航
     */
    @GetMapping("/{id}")
    public String buildingDetail(@PathVariable Long id, Model model) {
        try {
            Building building = buildingMapper.selectById(id);
            if (building == null) return "redirect:/campus";
            model.addAttribute("building", building);
            // 加载所有建筑用于智能导航地图
            List<Building> allBuildings = buildingMapper.selectList(null);
            System.out.println("[DEBUG] allBuildings loaded: " + (allBuildings != null ? allBuildings.size() : 0));
            model.addAttribute("allBuildings", allBuildings != null ? allBuildings : java.util.Collections.emptyList());
        } catch (Exception e) {
            System.err.println("[ERROR] buildingDetail(" + id + "): " + e.getMessage());
            e.printStackTrace();
            return "redirect:/campus";
        }
        return "campus/detail";
    }
}
