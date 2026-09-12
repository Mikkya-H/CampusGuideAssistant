package com.freshman.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 论坛示例数据初始化器
 * 启动时自动检查，帖子表为空则插入8篇帖子和30条评论（含正确的回复引用关系）
 */
@Component
public class ForumDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ForumDataInitializer.class);
    private final JdbcTemplate jdbc;
    private int cid = 0; // 评论ID计数器

    public ForumDataInitializer(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        try {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM forum_post", Integer.class);
            if (count != null && count > 0) {
                log.info("[论坛数据] 已有 {} 条帖子，跳过初始化", count);
                return;
            }

            log.info("[论坛数据] 开始插入示例数据...");

            insertPosts();
            insertComments();

            log.info("[论坛数据] 初始化完成：{} 篇帖子，{} 条评论",
                    jdbc.queryForObject("SELECT COUNT(*) FROM forum_post", Integer.class),
                    jdbc.queryForObject("SELECT COUNT(*) FROM forum_comment", Integer.class));
        } catch (Exception e) {
            log.warn("[论坛数据] 初始化跳过: {}", e.getMessage());
        }
    }

    private void insertPosts() {
        String sql = "INSERT INTO forum_post (title,content,user_id,category,is_essence,is_top,view_count,reply_count,like_count,status,create_time) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        jdbc.update(sql, "新生必看：报到当天注意事项总结", "报到当天贴士：1.尽量上午到校人少不用排队 2.证件单独放别塞行李箱底下 3.军训服当场试穿不合适当场换 4.宿舍钥匙保管好丢了补办15块 5.加辅导员微信后续通知都在群里。祝大家报到顺利！", 1, "校园攻略", 1, 1, 328, 4, 25, 1, "2026-07-10 09:30:00");

        jdbc.update(sql, "软件工程专业的学长学姐在吗？", "我是2026级软件工程新生想提前了解：大一需要带电脑吗买什么配置？编程零基础会不会跟不上？软工大一的课多不多？宿舍能选厚德学区四人间吗？希望学长学姐指点谢谢！", 1, "学习交流", 0, 0, 156, 3, 18, 1, "2026-07-11 14:20:00");

        jdbc.update(sql, "关于军训防晒的血泪教训", "去年军训偷懒没涂防晒第一天就晒伤了。给新生建议：防晒霜买SPF50+别买便宜货；每2小时补涂一次；脖子后面和耳朵也要涂；晚上回宿舍敷芦荟胶；带个1L以上的大杯子。大庆太阳看着不毒其实紫外线很强！", 1, "生活问答", 1, 0, 289, 5, 32, 1, "2026-07-09 16:45:00");

        jdbc.update(sql, "厚德学区VS博文学区到底哪个住着舒服？", "马上要选宿舍了纠结中。厚德学区四人间1200一年：上床下桌空间大有空调独立卫浴，缺点是贵了400离第一食堂远一点。博文学区六人间800一年：便宜离食堂近人多热闹，缺点是公共卫浴空间挤没空调。住过的来说说体验？400块的差距值不值？", 1, "生活问答", 0, 0, 203, 5, 15, 1, "2026-07-12 10:10:00");

        jdbc.update(sql, "有一起打篮球的兄弟吗？", "2026级新生计算机学院的平时喜欢打篮球。想问：学校篮球场多吗要不要抢场地？有没有篮球社团怎么加？新生篮球赛什么时候怎么报名？开学后约球啊兄弟们！", 1, "社团招新", 0, 0, 98, 3, 12, 1, "2026-07-13 08:30:00");

        jdbc.update(sql, "新生必备APP清单分享", "整理了一下上大学必备的APP：学习类有超星学习通、知到、WPS Office；生活类有菜鸟、美团饿了么、哈啰单车；工具类有百度网盘、扫描全能王；社交类有QQ、微信。还有什么好用的APP评论区补充！", 1, "校园攻略", 0, 0, 176, 4, 22, 1, "2026-07-11 21:00:00");

        jdbc.update(sql, "开学迷路了怎么办校园地图在哪看？", "学校比想象中大好多从校门走到宿舍区走了快20分钟。有没有好用的校园地图？手机能导航吗？我看咱们迎新系统好像有地图功能有人用过吗准确不？", 1, "新生报到", 0, 0, 134, 3, 8, 1, "2026-07-14 07:15:00");

        jdbc.update(sql, "有没有想一起学Python的小伙伴？", "暑假想提前学点编程听说Python对新手比较友好。有没有同样想学的可以建个群互相监督打卡。目前在看B站上的教程感觉一个人学容易半途而废。有学长学姐带带就更好了！", 1, "学习交流", 0, 0, 87, 2, 16, 1, "2026-07-14 09:00:00");
    }

    private void insertComments() {
        // 每条评论插入后 cid 自增，用变量记住需要被回复的评论ID

        // ===== 帖子1 (ID=1) 的评论 =====
        addComment(1, 1, null, "太实用了！补充一点：报到那天带一支笔填表的时候你就知道多重要了", 8);
        addComment(1, 1, null, "请问如果下午才到的话还能办完所有手续吗？我是外省的火车下午两点才到", 3);
        int q1 = cid; // 记住"下午到"这条的ID，供下一条回复引用
        addComment(1, 1, q1, "可以的下午也能办就是人多要排队。建议下了火车直接坐学校的接站大巴过来更快", 5);
        addComment(1, 1, null, "学长好暖！收藏了！", 12);

        // ===== 帖子2 (ID=2) 的评论 =====
        addComment(2, 1, null, "软工大三学长来答：电脑必带推荐联想小新或华硕预算5000-7000就够了；零基础完全没关系大家都是从头学暑假可以看看B站Python入门；大一课不算多但数据结构要好好学；厚德需要抢名额有限提早登录系统", 20);
        int a1 = cid; // 学长回复的ID
        addComment(2, 1, null, "同2026级软工新生！加个好友吗？", 4);
        addComment(2, 1, a1, "谢谢学长太详细了！还有个问题软工需要学数学很难吗我数学一般...", 6);

        // ===== 帖子3 (ID=3) 的评论 =====
        addComment(3, 1, null, "哈哈哈太真实了！去年我们班晒伤了一大半", 15);
        addComment(3, 1, null, "请问女生军训期间能化妆吗？防晒加粉底会不会被教官说？", 2);
        int q2 = cid; // "化妆"那条的ID
        addComment(3, 1, q2, "别化了军训出汗量你想象不到妆花了更尴尬。涂个防晒加眉毛就行了大家都素颜", 18);
        addComment(3, 1, null, "补充：带个针线包！军服扣子真的很容易掉我两天掉了三颗扣子", 25);
        addComment(3, 1, null, "芦荟胶提前买好放宿舍别等晒伤了再买学校超市到时候会断货", 10);

        // ===== 帖子4 (ID=4) 的评论 =====
        addComment(4, 1, null, "厚德！必须厚德！多400块值爆了独立卫浴不用冬天跑走廊上厕所这个体验差距不是400块能衡量的", 22);
        addComment(4, 1, null, "看你预算吧。我住的博文其实习惯就好了六人间也挺热闹的。而且博文离第一食堂近下课直接吃饭不用走远路。穷学生表示省400块吃几顿好的不香吗？", 12);
        addComment(4, 1, null, "厚德名额有限不是想选就能选到的。建议做好两手准备能抢到最好抢不到博文启智也不错", 9);
        int a3 = cid; // "名额有限"的ID
        addComment(4, 1, a3, "怎么抢？是在迎新系统里选吗先到先得？", 3);
        int q3 = cid; // "怎么抢"的ID
        addComment(4, 1, q3, "对的迎新系统个人中心宿舍选择开放时间关注辅导员通知手速要快！去年不到半小时就抢完了", 7);

        // ===== 帖子5 (ID=5) 的评论 =====
        addComment(5, 1, null, "篮球场很多！室外12个场基本不用抢体育馆室内场需要预约。篮球社每年招新百团大战的时候去他们摊位报名就行。新生杯一般是10月份各学院组队到时候辅导员会通知", 8);
        addComment(5, 1, null, "约起来！我是石油工程的开学操场见", 5);
        addComment(5, 1, null, "什么水平能加篮球社啊？我只会投篮不会运球...", 2);

        // ===== 帖子6 (ID=6) 的评论 =====
        addComment(6, 1, null, "推荐一个：Forest专注森林防玩手机神器期末复习必备", 6);
        addComment(6, 1, null, "还有不背单词或者墨墨背单词大一要考四六级的提前背起来！", 9);
        addComment(6, 1, null, "菜鸟APP确实必备学校快递全走菜鸟驿站不下载取不了件", 4);
        addComment(6, 1, null, "补充：高德地图！大庆公交线路多查公交实时到站很方便", 3);

        // ===== 帖子7 (ID=7) 的评论 =====
        addComment(7, 1, null, "迎新系统校园导览里面有完整的地图教学楼食堂宿舍都能搜到还能导航！", 8);
        addComment(7, 1, null, "刚试了一下确实好用！还有百度地图集成连教学楼内部楼层都能看", 5);
        addComment(7, 1, null, "第一天可以提前到学校转转熟悉环境或者跟着志愿者走一遍两天就熟了", 3);

        // ===== 帖子8 (ID=8) 的评论 =====
        addComment(8, 1, null, "Python入门推荐莫烦Python的教程B站就有讲的特别清楚！或者看嵩天老师的Python语言程序设计那个是国家级精品课", 11);
        addComment(8, 1, null, "我也在学！加我QQ咱们建个群互相督促~", 4);
    }

    /** 插入一条评论并自增ID计数器 */
    private void addComment(int postId, int userId, Integer parentId, String content, int likes) {
        cid++;
        String sql = "INSERT INTO forum_comment (id, post_id, user_id, parent_id, content, like_count, status, create_time) VALUES (?,?,?,?,?,?,?,NOW())";
        jdbc.update(sql, cid, postId, userId, parentId, content, likes, 1);
    }
}
