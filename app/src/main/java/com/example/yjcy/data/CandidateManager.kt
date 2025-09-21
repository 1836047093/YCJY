package com.example.yjcy.data

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlin.random.Random

class CandidateManager {
    private val _candidates = mutableStateOf(generateInitialCandidates())
    val candidates by _candidates
    
    private val candidateNames = listOf(
        "李明", "王芳", "张伟", "刘娜", "陈强", "赵敏", "孙丽", "周杰",
        "吴涛", "郑雪", "马超", "朱琳", "胡斌", "林雨", "黄磊", "徐静",
        "邓飞", "谢萍", "韩冰", "曹阳", "彭丽", "董伟", "袁娟", "蒋涛"
    )
    
    private val positions = listOf("程序员", "美术师", "策划师", "音效师", "客服")
    private val educationLevels = listOf("大专", "本科", "硕士", "博士")
    
    /**
     * 生成初始候选人列表
     */
    private fun generateInitialCandidates(): List<Candidate> {
        return listOf(
            Candidate(
                id = 1, name = "李明", position = "程序员",
                programmingSkill = 4, designSkill = 2, planningSkill = 2, soundSkill = 1, customerServiceSkill = 2,
                expectedSalary = 8000, experienceYears = 3, educationLevel = "本科",
                recruitmentCost = 2000, successRate = 0.8f
            ),
            Candidate(
                id = 2, name = "王芳", position = "美术师",
                programmingSkill = 1, designSkill = 5, planningSkill = 3, soundSkill = 2, customerServiceSkill = 2,
                expectedSalary = 7500, experienceYears = 4, educationLevel = "本科",
                recruitmentCost = 1800, successRate = 0.85f
            ),
            Candidate(
                id = 3, name = "张伟", position = "策划师",
                programmingSkill = 2, designSkill = 3, planningSkill = 5, soundSkill = 2, customerServiceSkill = 3,
                expectedSalary = 7000, experienceYears = 2, educationLevel = "硕士",
                recruitmentCost = 2200, successRate = 0.75f
            ),
            Candidate(
                id = 4, name = "刘娜", position = "音效师",
                programmingSkill = 1, designSkill = 2, planningSkill = 2, soundSkill = 5, customerServiceSkill = 2,
                expectedSalary = 6500, experienceYears = 3, educationLevel = "本科",
                recruitmentCost = 1500, successRate = 0.9f
            ),
            Candidate(
                id = 5, name = "陈强", position = "客服",
                programmingSkill = 2, designSkill = 1, planningSkill = 3, soundSkill = 2, customerServiceSkill = 5,
                expectedSalary = 4500, experienceYears = 1, educationLevel = "大专",
                recruitmentCost = 1000, successRate = 0.95f
            )
        )
    }
    
    /**
     * 生成新的随机候选人
     */
    fun generateRandomCandidate(): Candidate {
        val position = positions.random()
        val name = candidateNames.random()
        val experienceYears = Random.nextInt(0, 8)
        val educationLevel = educationLevels.random()
        
        // 根据职位生成相应的技能倾向
        val skillsList = when (position) {
            "程序员" -> Pair(
                Random.nextInt(3, 6), // 程序员编程技能较高
                Random.nextInt(1, 3)
            ).let { pair ->
                listOf(pair.first, pair.second, Random.nextInt(1, 4), Random.nextInt(1, 3), Random.nextInt(1, 3))
            }
            "美术师" -> Pair(
                Random.nextInt(1, 3),
                Random.nextInt(3, 6) // 美术师设计技能较高
            ).let { pair ->
                listOf(pair.first, pair.second, Random.nextInt(1, 4), Random.nextInt(1, 4), Random.nextInt(1, 3))
            }
            "策划师" -> Pair(
                Random.nextInt(1, 4),
                Random.nextInt(3, 6) // 策划师策划技能较高
            ).let { pair ->
                listOf(pair.first, Random.nextInt(1, 4), pair.second, Random.nextInt(1, 3), Random.nextInt(2, 4))
            }
            "音效师" -> Pair(
                Random.nextInt(1, 3),
                Random.nextInt(3, 6) // 音效师音效技能较高
            ).let { pair ->
                listOf(pair.first, Random.nextInt(1, 4), Random.nextInt(1, 3), pair.second, Random.nextInt(1, 3))
            }
            "客服" -> Pair(
                Random.nextInt(1, 3),
                Random.nextInt(3, 6) // 客服客服技能较高
            ).let { pair ->
                listOf(pair.first, Random.nextInt(1, 3), Random.nextInt(2, 4), Random.nextInt(1, 3), pair.second)
            }
            else -> listOf(
                Random.nextInt(1, 4), Random.nextInt(1, 4), Random.nextInt(1, 4),
                Random.nextInt(1, 4), Random.nextInt(1, 4)
            )
        }
        
        val programming = skillsList[0]
        val design = skillsList[1]
        val planning = skillsList[2]
        val sound = skillsList[3]
        val customerService = skillsList[4]
        
        // 根据经验和教育水平调整期望薪资
        val baseSalary = when (position) {
            "程序员" -> 7000
            "美术师" -> 6500
            "策划师" -> 6000
            "音效师" -> 5500
            "客服" -> 4000
            else -> 5000
        }
        
        val experienceBonus = experienceYears * 500
        val educationBonus = when (educationLevel) {
            "博士" -> 3000
            "硕士" -> 1500
            "本科" -> 500
            else -> 0
        }
        
        val expectedSalary = baseSalary + experienceBonus + educationBonus + Random.nextInt(-1000, 2000)
        
        // 计算招聘成本和成功率
        val recruitmentCost = (expectedSalary * 0.2f).toInt() + Random.nextInt(-200, 500)
        val successRate = 0.6f + (experienceYears * 0.05f) + Random.nextFloat() * 0.3f
        
        return Candidate(
            id = (candidates.maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            position = position,
            programmingSkill = programming,
            designSkill = design,
            planningSkill = planning,
            soundSkill = sound,
            customerServiceSkill = customerService,
            expectedSalary = maxOf(3000, expectedSalary),
            experienceYears = experienceYears,
            educationLevel = educationLevel,
            recruitmentCost = maxOf(500, recruitmentCost),
            successRate = minOf(1.0f, maxOf(0.3f, successRate))
        )
    }
    
    /**
     * 添加新候选人
     */
    fun addCandidate(candidate: Candidate) {
        _candidates.value = _candidates.value + candidate
    }
    
    /**
     * 移除候选人
     */
    fun removeCandidate(candidateId: Int) {
        _candidates.value = _candidates.value.filter { it.id != candidateId }
    }
    
    /**
     * 更新候选人状态
     */
    fun updateCandidateStatus(candidateId: Int, status: AvailabilityStatus) {
        _candidates.value = _candidates.value.map { candidate ->
            if (candidate.id == candidateId) {
                candidate.copy(availabilityStatus = status, updatedAt = System.currentTimeMillis())
            } else {
                candidate
            }
        }
    }
    
    /**
     * 筛选候选人
     */
    fun filterCandidates(filter: CandidateFilter): List<Candidate> {
        return candidates.filter { candidate ->
            // 搜索查询
            if (filter.searchQuery.isNotEmpty()) {
                val query = filter.searchQuery.lowercase()
                if (!candidate.name.lowercase().contains(query) && 
                    !candidate.position.lowercase().contains(query)) {
                    return@filter false
                }
            }
            
            // 职位筛选
            if (filter.positions.isNotEmpty() && candidate.position !in filter.positions) {
                return@filter false
            }
            
            // 薪资范围筛选
            if (filter.minSalary != null && candidate.expectedSalary < filter.minSalary) {
                return@filter false
            }
            if (filter.maxSalary != null && candidate.expectedSalary > filter.maxSalary) {
                return@filter false
            }
            
            // 经验范围筛选
            if (filter.minExperience != null && candidate.experienceYears < filter.minExperience) {
                return@filter false
            }
            if (filter.maxExperience != null && candidate.experienceYears > filter.maxExperience) {
                return@filter false
            }
            
            // 教育水平筛选
            if (filter.educationLevels.isNotEmpty() && candidate.educationLevel !in filter.educationLevels) {
                return@filter false
            }
            
            // 可用状态筛选
            if (filter.availabilityStatus != null && candidate.availabilityStatus != filter.availabilityStatus) {
                return@filter false
            }
            
            // 最低技能等级筛选
            if (filter.minSkillLevel != null && candidate.getSpecialtySkillLevel() < filter.minSkillLevel) {
                return@filter false
            }
            
            true
        }
    }
    
    /**
     * 排序候选人
     */
    fun sortCandidates(candidates: List<Candidate>, sortBy: CandidateSortBy, ascending: Boolean = true): List<Candidate> {
        val sorted = when (sortBy) {
            CandidateSortBy.NAME -> candidates.sortedBy { it.name }
            CandidateSortBy.SALARY -> candidates.sortedBy { it.expectedSalary }
            CandidateSortBy.EXPERIENCE -> candidates.sortedBy { it.experienceYears }
            CandidateSortBy.SKILL_LEVEL -> candidates.sortedBy { it.getSpecialtySkillLevel() }
            CandidateSortBy.SUCCESS_RATE -> candidates.sortedBy { it.successRate }
            CandidateSortBy.RECRUITMENT_COST -> candidates.sortedBy { it.recruitmentCost }
            CandidateSortBy.CREATED_AT -> candidates.sortedBy { it.createdAt }
        }
        
        return if (ascending) sorted else sorted.reversed()
    }
    
    /**
     * 获取可用候选人数量（状态为AVAILABLE的候选人）
     */
    fun getAvailableCandidatesCount(): Int {
        return candidates.count { it.availabilityStatus == AvailabilityStatus.AVAILABLE }
    }

    /**
     * 生成更多候选人
     */
    fun generateMoreCandidates(count: Int = 5) {
        repeat(count) {
            addCandidate(generateRandomCandidate())
        }
    }
}