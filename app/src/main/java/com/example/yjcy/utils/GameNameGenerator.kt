package com.example.yjcy.utils

import com.example.yjcy.ui.GameTheme
import kotlin.random.Random

/**
 * 游戏名称生成器
 * 根据游戏主题生成随机的游戏名称
 */
object GameNameGenerator {
    
    // 各主题的游戏名前缀
    private val actionPrefixes = listOf("烈焰", "暗影", "狂战", "铁血", "雷霆", "极限", "无畏", "复仇", "战神", "英雄")
    private val adventurePrefixes = listOf("失落", "秘境", "神秘", "未知", "奇幻", "传说", "探险", "远征", "寻宝", "奥秘")
    private val rpgPrefixes = listOf("永恒", "命运", "魔法", "圣域", "幻想", "神话", "仙境", "魔幻", "混沌", "起源")
    private val strategyPrefixes = listOf("帝国", "王朝", "文明", "征服", "统治", "霸业", "权谋", "天下", "三国", "战略")
    private val simulationPrefixes = listOf("模拟", "经营", "大亨", "城市", "农场", "工厂", "王国", "建造", "创造", "世界")
    private val puzzlePrefixes = listOf("智力", "谜题", "益智", "头脑", "逻辑", "思维", "解谜", "烧脑", "巧妙", "机智")
    private val racingPrefixes = listOf("极速", "狂飙", "赛车", "飞驰", "疾速", "竞速", "飙车", "急速", "冲刺", "赛道")
    private val sportsPrefixes = listOf("冠军", "王者", "巨星", "传奇", "职业", "超级", "热血", "巅峰", "梦幻", "全明星")
    private val horrorPrefixes = listOf("恐怖", "惊悚", "诅咒", "鬼屋", "逃生", "黑暗", "午夜", "禁地", "迷雾", "死亡")
    private val casualPrefixes = listOf("开心", "欢乐", "趣味", "轻松", "休闲", "可爱", "萌萌", "快乐", "悠闲", "甜蜜")
    private val shooterPrefixes = listOf("火线", "战场", "狙击", "反恐", "枪战", "突击", "战争", "前线", "决战", "战斗")
    private val mobaPrefixes = listOf("王者", "英雄", "巅峰", "荣耀", "竞技", "对决", "冠军", "传说", "终极", "无双")
    
    // 各主题的游戏名后缀
    private val actionSuffixes = listOf("之刃", "战歌", "传说", "之怒", "英雄", "战记", "风暴", "征程", "传奇", "之路")
    private val adventureSuffixes = listOf("大陆", "世界", "之旅", "探险", "秘境", "传说", "奇遇", "历险", "寻宝", "之谜")
    private val rpgSuffixes = listOf("物语", "传说", "奇缘", "纪元", "编年史", "之书", "世纪", "幻想曲", "之光", "奥德赛")
    private val strategySuffixes = listOf("时代", "战争", "崛起", "争霸", "纪元", "之路", "帝国", "天下", "征战", "雄心")
    private val simulationSuffixes = listOf("之星", "大亨", "模拟器", "物语", "世界", "经营记", "传说", "故事", "日记", "梦想")
    private val puzzleSuffixes = listOf("挑战", "大师", "王者", "冒险", "旅程", "世界", "奇妙旅", "游戏", "谜题", "乐园")
    private val racingSuffixes = listOf("传说", "王者", "之王", "风暴", "竞速", "狂飙", "飞车", "天下", "精英", "赛")
    private val sportsSuffixes = listOf("联赛", "经理", "世界", "之路", "梦想", "生涯", "传奇", "荣耀", "巨星", "王朝")
    private val horrorSuffixes = listOf("之夜", "惊魂", "医院", "迷宫", "禁地", "城堡", "之屋", "逃生", "实录", "档案")
    private val casualSuffixes = listOf("消消乐", "大冒险", "乐园", "天堂", "世界", "物语", "之旅", "派对", "嘉年华", "时光")
    private val shooterSuffixes = listOf("行动", "前线", "突击", "战场", "使命", "精英", "战争", "荣耀", "狙击手", "特战队")
    private val mobaSuffixes = listOf("竞技场", "对决", "战场", "荣耀", "争霸", "联盟", "王者", "传说", "巅峰赛", "之战")
    
    /**
     * 根据游戏主题生成随机游戏名
     */
    fun generateGameName(theme: GameTheme): String {
        val (prefixes, suffixes) = when (theme) {
            GameTheme.ACTION -> actionPrefixes to actionSuffixes
            GameTheme.ADVENTURE -> adventurePrefixes to adventureSuffixes
            GameTheme.RPG -> rpgPrefixes to rpgSuffixes
            GameTheme.STRATEGY -> strategyPrefixes to strategySuffixes
            GameTheme.SIMULATION -> simulationPrefixes to simulationSuffixes
            GameTheme.PUZZLE -> puzzlePrefixes to puzzleSuffixes
            GameTheme.RACING -> racingPrefixes to racingSuffixes
            GameTheme.SPORTS -> sportsPrefixes to sportsSuffixes
            GameTheme.HORROR -> horrorPrefixes to horrorSuffixes
            GameTheme.CASUAL -> casualPrefixes to casualSuffixes
            GameTheme.SHOOTER -> shooterPrefixes to shooterSuffixes
            GameTheme.MOBA -> mobaPrefixes to mobaSuffixes
        }
        
        val prefix = prefixes.random()
        val suffix = suffixes.random()
        
        return "$prefix$suffix"
    }
    
    /**
     * 生成多个随机游戏名供选择
     */
    fun generateGameNames(theme: GameTheme, count: Int = 5): List<String> {
        val names = mutableSetOf<String>()
        var attempts = 0
        val maxAttempts = count * 10 // 防止死循环
        
        while (names.size < count && attempts < maxAttempts) {
            names.add(generateGameName(theme))
            attempts++
        }
        
        return names.toList()
    }
}
