package com.example.yjcy.data

/**
 * 服务器类型枚举
 */
enum class ServerType(
    val displayName: String,
    val capacity: Int,      // 容量（万人）
    val cost: Long,          // 费用（元）
    val description: String
) {
    BASIC("星尘-D型服务器", 10, 100000L, "适合小规模运营，10万容量"),
    INTERMEDIATE("星尘-C型服务器", 50, 500000L, "适合中等规模，50万容量"),
    ADVANCED("星尘-B型服务器", 200, 2000000L, "适合大规模运营，200万容量"),
    CLOUD("星尘-A型服务器", 500, 5000000L, "最高性能，500万容量")
}

/**
 * 服务器实例数据类
 */
data class ServerInstance(
    val id: String,
    val type: ServerType,
    val purchaseYear: Int,
    val purchaseMonth: Int,
    val purchaseDay: Int,
    val isActive: Boolean = true
) {
    /**
     * 获取服务器总容量（万人）
     */
    fun getCapacity(): Int = type.capacity
    
    /**
     * 获取服务器购买费用
     */
    fun getCost(): Long = type.cost
}

/**
 * 游戏服务器管理数据类
 */
data class GameServerInfo(
    val gameId: String,
    val servers: List<ServerInstance> = emptyList()
) {
    /**
     * 计算总容量（万人）
     */
    fun getTotalCapacity(): Int {
        return servers.filter { it.isActive }.sumOf { it.getCapacity() }
    }
    
    /**
     * 计算总投入费用
     */
    fun getTotalCost(): Long {
        return servers.sumOf { it.getCost() }
    }
    
    /**
     * 获取激活的服务器数量
     */
    fun getActiveServerCount(): Int {
        return servers.count { it.isActive }
    }
    
    /**
     * 按类型统计服务器数量
     */
    fun getServerCountByType(): Map<ServerType, Int> {
        return servers.filter { it.isActive }
            .groupingBy { it.type }
            .eachCount()
    }
}
