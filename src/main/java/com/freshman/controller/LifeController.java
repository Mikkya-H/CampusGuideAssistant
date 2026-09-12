package com.freshman.controller;

import com.freshman.entity.*;
import com.freshman.mapper.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 校园生活控制器
 * 功能：负责校园生活相关信息的展示，包括宿舍信息、食堂信息、社团信息、校园活动等，
 *       帮助新生全面了解校园生活环境和各类资源
 * 负责成员：S
 * 所属模块：校园生活模块
 *
 * @author S
 * @version 1.0
 */
@Controller
@RequestMapping("/life")
public class LifeController {

    private final DormitoryMapper dormitoryMapper;
    private final CafeteriaMapper cafeteriaMapper;
    private final ClubMapper clubMapper;
    private final ActivityMapper activityMapper;

    /**
     * 构造器注入四个Mapper
     */
    public LifeController(DormitoryMapper dormitoryMapper, CafeteriaMapper cafeteriaMapper,
                         ClubMapper clubMapper, ActivityMapper activityMapper) {
        this.dormitoryMapper = dormitoryMapper;
        this.cafeteriaMapper = cafeteriaMapper;
        this.clubMapper = clubMapper;
        this.activityMapper = activityMapper;
    }

    /**
     * 校园生活首页
     * 功能：展示校园生活的总览入口页面
     *
     * @param model Spring MVC的Model对象
     * @return 校园生活首页视图
     */
    @GetMapping
    public String index(Model model) {
        return "life/index";
    }

    /**
     * 宿舍信息页面
     * 功能：展示学校所有宿舍楼的信息，包括房型、设施、费用等
     *
     * @param model Spring MVC的Model对象
     * @return 宿舍信息视图
     */
    @GetMapping("/dormitory")
    public String dormitory(Model model) {
        // 查询所有宿舍信息
        List<Dormitory> dormitories = dormitoryMapper.selectList(null);
        model.addAttribute("dormitories", dormitories);
        return "life/dormitory";
    }

    /**
     * 食堂信息页面
     * 功能：展示学校所有食堂的信息，包括位置、特色菜品等
     *
     * @param model Spring MVC的Model对象
     * @return 食堂信息视图
     */
    @GetMapping("/cafeteria")
    public String cafeteria(Model model) {
        // 查询所有食堂信息
        List<Cafeteria> cafeterias = cafeteriaMapper.selectList(null);
        model.addAttribute("cafeterias", cafeterias);
        return "life/cafeteria";
    }

    /**
     * 社团信息页面
     * 功能：展示学校各类学生社团的信息，帮助新生了解和选择感兴趣的社团
     *
     * @param model Spring MVC的Model对象
     * @return 社团信息视图
     */
    @GetMapping("/clubs")
    public String clubs(Model model) {
        // 查询所有社团信息
        List<Club> clubs = clubMapper.selectList(null);
        model.addAttribute("clubs", clubs);
        return "life/clubs";
    }

    /**
     * 校园活动页面
     * 功能：按活动开始时间降序展示校园活动列表，最新的活动排在最前面
     *
     * @param model Spring MVC的Model对象
     * @return 校园活动视图
     */
    @GetMapping("/activities")
    public String activities(Model model) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Activity>();
        // 按活动开始时间降序排列，即将开始的活动排在最前面
        wrapper.orderByDesc(Activity::getStartTime);
        List<Activity> activities = activityMapper.selectList(wrapper);
        model.addAttribute("activities", activities);
        return "life/activities";
    }
}
