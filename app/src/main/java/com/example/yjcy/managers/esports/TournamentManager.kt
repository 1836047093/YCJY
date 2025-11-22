package com.example.yjcy.managers.esports

import com.example.yjcy.data.esports.*
import com.example.yjcy.data.CompetitorCompany
import com.example.yjcy.data.HeroPosition
import java.util.Date
import kotlin.random.Random

/**
 * 赛事管理器
 * 管理所有赛事的创建、报名、进行、结算
 */
object TournamentManager {
    
    private val _tournaments = mutableMapOf<String, Tournament>()
    val activeTournaments: List<Tournament> get() = _tournaments.values.toList()
    
    private val _history = mutableListOf<TournamentRecord>()
    val history: List<TournamentRecord> get() = _history
    
    /**
     * 初始化赛事系统
     */
    fun initialize(
        savedTournaments: List<Tournament>?,
        savedHistory: List<TournamentRecord>?
    ) {
        _tournaments.clear()
        savedTournaments?.forEach { tournament ->
            _tournaments[tournament.id] = tournament
        }
        
        _history.clear()
        savedHistory?.let { _history.addAll(it) }
        
        android.util.Log.d("TournamentManager", 
            "初始化完成，赛事数: ${_tournaments.size}, 历史: ${_history.size}")
    }
    
    /**
     * 创建新赛事（自动填充AI战队）
     */
    fun createTournament(
        tier: TournamentTier,
        year: Int,
        season: TournamentSeason?,
        competitors: List<CompetitorCompany>? = null
    ): Tournament {
        val id = generateTournamentId(tier, year, season)
        
        // 检查是否已存在
        if (_tournaments.containsKey(id)) {
            android.util.Log.w("TournamentManager", "赛事已存在: $id")
            return _tournaments[id]!!
        }
        
        val tournament = Tournament(
            id = id,
            tier = tier,
            year = year,
            season = season,
            status = Tournament.TournamentStatus.REGISTRATION,
            registeredTeams = mutableListOf(),
            currentPhase = TournamentPhase.REGISTRATION,
            schedule = mutableListOf(),
            results = mutableMapOf(),
            prizePool = tier.minPrizePool
        )
        
        // 自动生成AI战队并报名
        competitors?.let { 
            val aiTeams = generateTeamsFromCompetitors(it)
            aiTeams.forEach { team ->
                tournament.registeredTeams.add(team)
                android.util.Log.d("TournamentManager", 
                    "AI战队${team.name}(${team.companyName})自动报名")
            }
        }
        
        _tournaments[id] = tournament
        android.util.Log.d("TournamentManager", "创建赛事: $id，已报名${tournament.registeredTeams.size}队")
        return tournament
    }
    
    /**
     * 生成赛事ID
     */
    private fun generateTournamentId(
        tier: TournamentTier,
        year: Int,
        season: TournamentSeason?
    ): String {
        return if (season != null) {
            "${tier.name}_${year}_${season.name}"
        } else {
            "${tier.name}_${year}"
        }
    }
    
    /**
     * 报名参赛
     */
    fun registerTeam(tournamentId: String, team: Team, entryFee: Long): Boolean {
        val tournament = _tournaments[tournamentId]
        if (tournament == null) {
            android.util.Log.w("TournamentManager", "赛事不存在: $tournamentId")
            return false
        }
        
        // 检查赛事状态
        if (tournament.status != Tournament.TournamentStatus.REGISTRATION) {
            android.util.Log.w("TournamentManager", "赛事不在报名阶段")
            return false
        }
        
        // 检查是否已报名
        if (tournament.registeredTeams.any { it.id == team.id }) {
            android.util.Log.w("TournamentManager", "队伍已报名")
            return false
        }
        
        // 检查资格
        if (!checkEligibility(tournament, team)) {
            android.util.Log.w("TournamentManager", "队伍不符合参赛资格")
            return false
        }
        
        // 报名成功
        tournament.registeredTeams.add(team)
        android.util.Log.d("TournamentManager", 
            "队伍${team.name}报名成功，当前${tournament.registeredTeams.size}队")
        
        return true
    }
    
    /**
     * 检查参赛资格
     */
    private fun checkEligibility(tournament: Tournament, team: Team): Boolean {
        // 检查队伍规模
        val minSize = when (tournament.tier) {
            TournamentTier.CITY_CUP -> 5  // 5主力
            TournamentTier.CHAMPIONSHIP -> 7  // 5主力+2替补
            TournamentTier.WORLDS -> 7
        }
        
        if (team.players.size < minSize) {
            android.util.Log.w("TournamentManager", "队伍人数不足: ${team.players.size} < $minSize")
            return false
        }
        
        // 检查前置赛事（锦标赛和世界赛需要参加过前置赛事）
        if (tournament.tier == TournamentTier.CHAMPIONSHIP) {
            val hasParticipated = team.tournamentHistory.any { 
                it.tier == TournamentTier.CITY_CUP 
            }
            if (!hasParticipated) {
                android.util.Log.w("TournamentManager", "未参加过城市杯")
                return false
            }
        }
        
        if (tournament.tier == TournamentTier.WORLDS) {
            val hasParticipated = team.tournamentHistory.any { 
                it.tier == TournamentTier.CHAMPIONSHIP 
            }
            if (!hasParticipated) {
                android.util.Log.w("TournamentManager", "未参加过锦标赛")
                return false
            }
        }
        
        return true
    }
    
    /**
     * 开始赛事
     */
    fun startTournament(tournamentId: String): Boolean {
        val tournament = _tournaments[tournamentId] ?: return false
        
        // 检查参赛队伍数量
        val requiredTeams = when (tournament.tier) {
            TournamentTier.CITY_CUP -> 16
            TournamentTier.CHAMPIONSHIP -> 12
            TournamentTier.WORLDS -> 16
        }
        
        if (tournament.registeredTeams.size < requiredTeams) {
            android.util.Log.w("TournamentManager", 
                "参赛队伍不足: ${tournament.registeredTeams.size} < $requiredTeams")
            return false
        }
        
        // 生成赛程
        when (tournament.tier) {
            TournamentTier.CITY_CUP -> generateCityCupSchedule(tournament)
            TournamentTier.CHAMPIONSHIP -> generateChampionshipSchedule(tournament)
            TournamentTier.WORLDS -> generateWorldsSchedule(tournament)
        }
        
        tournament.status = Tournament.TournamentStatus.IN_PROGRESS
        tournament.currentPhase = when (tournament.tier) {
            TournamentTier.CITY_CUP, TournamentTier.WORLDS -> TournamentPhase.GROUP_STAGE
            TournamentTier.CHAMPIONSHIP -> TournamentPhase.REGISTRATION  // 常规赛阶段
        }
        
        android.util.Log.d("TournamentManager", "赛事开始: $tournamentId")
        return true
    }
    
    /**
     * 生成城市杯赛程
     */
    private fun generateCityCupSchedule(tournament: Tournament) {
        val teams = tournament.registeredTeams.shuffled()
        
        // 分成4组，每组4队
        val groups = teams.chunked(4)
        var matchId = 1
        var dayOffset = 0
        
        // 小组赛（单循环BO1）
        groups.forEachIndexed { groupIndex, group ->
            val groupName = ('A' + groupIndex).toString()
            
            // 每组内循环赛
            for (i in 0 until group.size) {
                for (j in i + 1 until group.size) {
                    val match = ScheduledMatch(
                        id = "${tournament.id}_match_${matchId++}",
                        date = Date(System.currentTimeMillis() + dayOffset * 86400000L),
                        blueTeam = group[i],
                        redTeam = group[j],
                        format = MatchFormat.BO1,
                        phase = "小组赛-${groupName}组",
                        status = ScheduledMatch.MatchStatus.SCHEDULED,
                        result = null
                    )
                    tournament.schedule.add(match)
                    dayOffset++
                }
            }
        }
        
        android.util.Log.d("TournamentManager", 
            "城市杯小组赛赛程生成完成，共${tournament.schedule.size}场比赛")
    }
    
    /**
     * 生成锦标赛赛程
     */
    private fun generateChampionshipSchedule(tournament: Tournament) {
        val teams = tournament.registeredTeams.shuffled()
        var matchId = 1
        var dayOffset = 0
        
        // 常规赛（双循环BO3）
        for (round in 1..2) {
            for (i in 0 until teams.size) {
                for (j in i + 1 until teams.size) {
                    val match = ScheduledMatch(
                        id = "${tournament.id}_match_${matchId++}",
                        date = Date(System.currentTimeMillis() + dayOffset * 86400000L),
                        blueTeam = teams[i],
                        redTeam = teams[j],
                        format = MatchFormat.BO3,
                        phase = "常规赛第${round}轮",
                        status = ScheduledMatch.MatchStatus.SCHEDULED,
                        result = null
                    )
                    tournament.schedule.add(match)
                    dayOffset += 2  // BO3需要2天
                }
            }
        }
        
        android.util.Log.d("TournamentManager", 
            "锦标赛常规赛赛程生成完成，共${tournament.schedule.size}场比赛")
    }
    
    /**
     * 生成全球总决赛赛程
     */
    private fun generateWorldsSchedule(tournament: Tournament) {
        // 简化实现：先只生成小组赛
        val teams = tournament.registeredTeams.shuffled()
        val groups = teams.chunked(4)
        var matchId = 1
        var dayOffset = 0
        
        groups.forEachIndexed { groupIndex, group ->
            val groupName = ('A' + groupIndex).toString()
            
            // 双循环BO1
            for (round in 1..2) {
                for (i in 0 until group.size) {
                    for (j in i + 1 until group.size) {
                        val match = ScheduledMatch(
                            id = "${tournament.id}_match_${matchId++}",
                            date = Date(System.currentTimeMillis() + dayOffset * 86400000L),
                            blueTeam = group[i],
                            redTeam = group[j],
                            format = MatchFormat.BO1,
                            phase = "小组赛-${groupName}组-第${round}轮",
                            status = ScheduledMatch.MatchStatus.SCHEDULED,
                            result = null
                        )
                        tournament.schedule.add(match)
                        dayOffset++
                    }
                }
            }
        }
        
        android.util.Log.d("TournamentManager", 
            "全球总决赛小组赛赛程生成完成，共${tournament.schedule.size}场比赛")
    }
    
    /**
     * 推进赛事（模拟一场比赛）
     */
    fun progressTournament(tournamentId: String): MatchResult? {
        val tournament = _tournaments[tournamentId] ?: return null
        
        // 找到下一场待进行的比赛
        val nextMatch = tournament.schedule.find { 
            it.status == ScheduledMatch.MatchStatus.SCHEDULED 
        } ?: return null
        
        // 创建Match对象
        val match = Match(
            id = nextMatch.id,
            tournamentId = tournament.id,
            blueTeam = nextMatch.blueTeam,
            redTeam = nextMatch.redTeam,
            bpSession = null,
            result = null,
            format = nextMatch.format
        )
        
        // 模拟比赛
        val result = MatchSimulator.simulateMatch(match)
        
        // 更新比赛状态
        nextMatch.status = ScheduledMatch.MatchStatus.COMPLETED
        nextMatch.result = result
        tournament.results[nextMatch.id] = result
        
        // 更新选手状态
        MatchSimulator.updateAfterMatch(match, result)
        
        android.util.Log.d("TournamentManager", 
            "比赛完成: ${nextMatch.phase} - ${result.winner.name} vs ${result.loser.name}")
        
        // 检查是否需要进入下一阶段
        checkPhaseTransition(tournament)
        
        return result
    }
    
    /**
     * 检查阶段转换
     */
    private fun checkPhaseTransition(tournament: Tournament) {
        val completedMatches = tournament.schedule.count { 
            it.status == ScheduledMatch.MatchStatus.COMPLETED 
        }
        val totalMatches = tournament.schedule.size
        
        if (completedMatches == totalMatches) {
            // 所有比赛完成，进入淘汰赛或结束
            if (tournament.currentPhase == TournamentPhase.GROUP_STAGE) {
                // 生成淘汰赛
                generatePlayoffs(tournament)
                tournament.currentPhase = TournamentPhase.PLAYOFFS
            } else if (tournament.currentPhase == TournamentPhase.PLAYOFFS) {
                // 赛事结束
                completeTournament(tournament)
            }
        }
    }
    
    /**
     * 生成淘汰赛
     */
    private fun generatePlayoffs(tournament: Tournament) {
        // 简化实现：直接选出前8名进入淘汰赛
        val standings = calculateStandings(tournament)
        val top8 = standings.take(8)
        
        var matchId = tournament.schedule.size + 1
        var dayOffset = tournament.schedule.size
        
        // 1/4决赛（BO5）
        for (i in 0 until 4) {
            val match = ScheduledMatch(
                id = "${tournament.id}_match_${matchId++}",
                date = Date(System.currentTimeMillis() + dayOffset * 86400000L),
                blueTeam = top8[i * 2],
                redTeam = top8[i * 2 + 1],
                format = MatchFormat.BO5,
                phase = "1/4决赛",
                status = ScheduledMatch.MatchStatus.SCHEDULED,
                result = null
            )
            tournament.schedule.add(match)
            dayOffset += 3  // BO5需要3天
        }
        
        android.util.Log.d("TournamentManager", "淘汰赛赛程生成完成")
    }
    
    /**
     * 计算积分榜
     */
    private fun calculateStandings(tournament: Tournament): List<Team> {
        val teamScores = mutableMapOf<String, Int>()
        
        // 统计每个队伍的胜场
        tournament.results.values.forEach { result ->
            val winnerId = result.winner.id
            teamScores[winnerId] = teamScores.getOrDefault(winnerId, 0) + 1
        }
        
        // 按胜场排序
        return tournament.registeredTeams.sortedByDescending { 
            teamScores.getOrDefault(it.id, 0) 
        }
    }
    
    /**
     * 完成赛事
     */
    private fun completeTournament(tournament: Tournament) {
        tournament.status = Tournament.TournamentStatus.COMPLETED
        tournament.currentPhase = TournamentPhase.COMPLETED
        
        // 发放奖励
        distributeRewards(tournament)
        
        android.util.Log.d("TournamentManager", "赛事完成: ${tournament.id}")
    }
    
    /**
     * 发放奖励
     */
    private fun distributeRewards(tournament: Tournament) {
        val finalStandings = calculateFinalStandings(tournament)
        
        finalStandings.forEachIndexed { index, team ->
            val placement = index + 1
            val prizeMoney = calculatePrizeMoney(tournament.tier, placement)
            val prestige = calculatePrestige(tournament.tier, placement)
            
            // 记录到历史
            val record = TournamentRecord(
                tournamentId = tournament.id,
                tier = tournament.tier,
                year = tournament.year,
                season = tournament.season,
                placement = placement,
                prizeMoney = prizeMoney,
                prestigeEarned = prestige
            )
            
            _history.add(record)
            
            android.util.Log.d("TournamentManager", 
                "${team.name} 获得第${placement}名，奖金${prizeMoney}，声望${prestige}")
        }
    }
    
    /**
     * 计算最终排名
     */
    private fun calculateFinalStandings(tournament: Tournament): List<Team> {
        // 简化实现：根据胜场排序
        return calculateStandings(tournament)
    }
    
    /**
     * 计算奖金
     */
    private fun calculatePrizeMoney(tier: TournamentTier, placement: Int): Long {
        return when (tier) {
            TournamentTier.CITY_CUP -> when (placement) {
                1 -> 250_000
                2 -> 100_000
                3 -> 50_000
                4 -> 30_000
                in 5..8 -> 20_000
                else -> 0
            }
            TournamentTier.CHAMPIONSHIP -> when (placement) {
                1 -> 2_500_000
                2 -> 1_000_000
                3 -> 500_000
                4 -> 300_000
                in 5..8 -> 100_000
                else -> 0
            }
            TournamentTier.WORLDS -> when (placement) {
                1 -> 25_000_000
                2 -> 10_000_000
                3 -> 5_000_000
                4 -> 3_000_000
                in 5..8 -> 2_000_000
                in 9..16 -> 1_000_000
                else -> 500_000
            }
        }
    }
    
    /**
     * 计算声望
     */
    private fun calculatePrestige(tier: TournamentTier, placement: Int): Int {
        val basePrestige = when (tier) {
            TournamentTier.CITY_CUP -> 50
            TournamentTier.CHAMPIONSHIP -> 200
            TournamentTier.WORLDS -> 1000
        }
        
        return when (placement) {
            1 -> basePrestige
            2 -> (basePrestige * 0.6).toInt()
            3 -> (basePrestige * 0.4).toInt()
            4 -> (basePrestige * 0.3).toInt()
            in 5..8 -> (basePrestige * 0.2).toInt()
            else -> (basePrestige * 0.1).toInt()
        }
    }
    
    /**
     * 从竞争对手公司生成电竞战队
     */
    fun generateTeamsFromCompetitors(competitors: List<CompetitorCompany>): List<Team> {
        return competitors.take(8).mapIndexed { index, company ->
            val teamNames = listOf(
                "${company.name}战队", "${company.name}电竞", "${company.name}俱乐部",
                "${company.name}联盟", "${company.name}勇士", "${company.name}传奇"
            )
            val teamName = teamNames.random()
            
            // 生成5-7名选手
            val playerCount = 5 + Random.nextInt(3) // 5-7人
            val positions: List<HeroPosition> = listOf(
                HeroPosition.TOP, HeroPosition.JUNGLE, HeroPosition.MID, 
                HeroPosition.ADC, HeroPosition.SUPPORT, HeroPosition.TOP, HeroPosition.JUNGLE
            )
            val players = (1..playerCount).map { playerIndex ->
                val position = positions.getOrNull(playerIndex - 1) ?: HeroPosition.JUNGLE
                PlayerManager.generatePlayerForTeam(
                    teamId = "competitor_${company.id}",
                    position = position
                )
            }
            
            Team(
                id = "competitor_${company.id}",
                name = teamName,
                players = players,
                tournamentHistory = emptyList(),
                companyName = company.name
            )
        }
    }

    /**
     * 获取赛事详情
     */
    fun getTournament(tournamentId: String): Tournament? {
        return _tournaments[tournamentId]
    }
    
}
