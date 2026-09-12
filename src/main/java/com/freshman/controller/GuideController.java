package com.freshman.controller;

import com.freshman.entity.*;
import com.freshman.mapper.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

/**
 * 迎新指南控制器
 * 功能：负责新生报到流程、常见问题解答（含学生提问）、专业介绍、教师名录等信息的展示
 * 负责成员：W
 * 所属模块：迎新指南模块
 *
 * @author W
 * @version 1.0
 */
@Controller
@RequestMapping("/guide")
public class GuideController {

    private final RegistrationStepMapper stepMapper;
    private final FaqMapper faqMapper;
    private final MajorMapper majorMapper;
    private final TeacherMapper teacherMapper;

    /**
     * 构造器注入四个Mapper
     */
    public GuideController(RegistrationStepMapper stepMapper, FaqMapper faqMapper,
                          MajorMapper majorMapper, TeacherMapper teacherMapper) {
        this.stepMapper = stepMapper;
        this.faqMapper = faqMapper;
        this.majorMapper = majorMapper;
        this.teacherMapper = teacherMapper;
    }

    /**
     * 报到流程页面
     */
    @GetMapping("/registration")
    public String registration(Model model) {
        List<RegistrationStep> steps = stepMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RegistrationStep>()
                .orderByAsc(RegistrationStep::getStepNo));
        model.addAttribute("steps", steps);
        return "guide/registration";
    }

    /**
     * 常见问题(FAQ)页面
     * 功能：只展示已回复的问题（status=1），按排序字段升序，支持按类别筛选
     */
    @GetMapping("/faq")
    public String faq(Model model, @RequestParam(required = false) String category) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Faq>();
        wrapper.eq(Faq::getStatus, 1);  // 只展示管理员已回复的问题
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Faq::getCategory, category);
        }
        wrapper.orderByAsc(Faq::getSort);
        List<Faq> faqs = faqMapper.selectList(wrapper);
        model.addAttribute("faqs", faqs);
        model.addAttribute("currentCategory", category);
        return "guide/faq";
    }

    /**
     * 学生提交问题
     * 功能：已登录学生可提交问题，问题进入待回复状态，管理员回复后才会公开显示
     */
    @PostMapping("/faq/ask")
    public String askQuestion(@RequestParam String question,
                              @RequestParam String category,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        Faq faq = new Faq();
        faq.setQuestion(question.trim());
        faq.setCategory(category);
        faq.setAnswer("");          // 空答案，等待管理员回复
        faq.setStatus(0);           // 0=待回复
        faq.setSort(999);           // 排在最后
        faq.setViewCount(0);
        faqMapper.insert(faq);
        redirectAttributes.addFlashAttribute("success", "问题已提交，管理员回复后将公开展示！");
        return "redirect:/guide/faq";
    }

    /**
     * 专业介绍页面
     */
    @GetMapping("/majors")
    public String majors(Model model) {
        List<Major> majors = majorMapper.selectList(null);
        model.addAttribute("majors", majors);
        return "guide/majors";
    }

    /**
     * 教师名录页面
     */
    @GetMapping("/teachers")
    public String teachers(Model model) {
        List<Teacher> teachers = teacherMapper.selectList(null);
        model.addAttribute("teachers", teachers);
        return "guide/teachers";
    }
}
