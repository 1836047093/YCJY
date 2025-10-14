package com.example.yjcy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yjcy.data.MonetizationConfig
import com.example.yjcy.data.MonetizationItem
import com.example.yjcy.data.MonetizationItemType

/**
 * 付费内容选择步骤
 */
@Composable
fun MonetizationSelectionStep(
    selectedTheme: GameTheme?,
    monetizationItems: List<MonetizationItem>,
    onMonetizationItemsChange: (List<MonetizationItem>) -> Unit
) {
    val recommendedItems = remember(selectedTheme) {
        selectedTheme?.let { MonetizationConfig.getRecommendedItems(it) } ?: emptyList()
    }

    // 计算是否全选
    val allSelected = recommendedItems.all { itemType ->
        monetizationItems.any { it.type == itemType && it.isEnabled }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💰 选择付费内容",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            // 全选/反选按钮
            OutlinedButton(
                onClick = {
                    if (allSelected) {
                        // 反选：清空所有
                        onMonetizationItemsChange(emptyList())
                    } else {
                        // 全选：添加所有推荐项
                        val allItems = recommendedItems.map { itemType ->
                            MonetizationItem(type = itemType, isEnabled = true)
                        }
                        onMonetizationItemsChange(allItems)
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (allSelected) Color(0xFFFF6B6B) else Color(0xFF10B981),
                    containerColor = if (allSelected) 
                        Color(0xFFFF6B6B).copy(alpha = 0.1f) 
                    else 
                        Color(0xFF10B981).copy(alpha = 0.1f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (allSelected) Color(0xFFFF6B6B) else Color(0xFF10B981)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (allSelected) "✕ 全部取消" else "✓ 全部选择",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "根据游戏主题推荐的付费方式，可设置价格或稍后设置",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recommendedItems) { itemType ->
                    val existingItem = monetizationItems.find { it.type == itemType }
                    MonetizationItemCard(
                        itemType = itemType,
                        monetizationItem = existingItem,
                        onToggle = { enabled ->
                            val updatedItems = if (enabled) {
                                // 添加或更新
                                val filtered = monetizationItems.filter { it.type != itemType }
                                filtered + MonetizationItem(type = itemType, isEnabled = true)
                            } else {
                                // 移除
                                monetizationItems.filter { it.type != itemType }
                            }
                            onMonetizationItemsChange(updatedItems)
                        },
                        onPriceChange = { price ->
                            val updatedItems = monetizationItems.map { item ->
                                if (item.type == itemType) {
                                    item.copy(price = price)
                                } else {
                                    item
                                }
                            }
                            onMonetizationItemsChange(updatedItems)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "提示：未设置价格的内容可在游戏上线后设置",
            fontSize = 12.sp,
            color = Color(0xFF10B981),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

/**
 * 单个付费内容卡片
 */
@Composable
fun MonetizationItemCard(
    itemType: MonetizationItemType,
    monetizationItem: MonetizationItem?,
    onToggle: (Boolean) -> Unit,
    onPriceChange: (Float?) -> Unit
) {
    val isEnabled = monetizationItem?.isEnabled == true
    var priceText by remember(monetizationItem?.price) {
        mutableStateOf(monetizationItem?.price?.toInt()?.toString() ?: "")
    }
    var showPriceInput by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                Color(0xFF10B981).copy(alpha = 0.2f) 
            else 
                Color.White.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = itemType.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) Color(0xFF10B981) else Color.White
                    )
                    Text(
                        text = itemType.description,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // 开关按钮
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF10B981),
                        checkedTrackColor = Color(0xFF10B981).copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )
            }

            // 价格设置区域
            if (isEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!showPriceInput) {
                        // 显示当前价格或"稍后设置"
                        OutlinedButton(
                            onClick = { showPriceInput = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (monetizationItem?.price != null) 
                                    "价格: ¥${monetizationItem.price.toInt()}" 
                                else 
                                    "点击设置价格",
                                fontSize = 13.sp
                            )
                        }
                        
                        if (monetizationItem?.price != null) {
                            IconButton(
                                onClick = {
                                    priceText = ""
                                    onPriceChange(null)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清除价格",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else {
                        // 价格输入框
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("输入价格", fontSize = 13.sp) },
                            leadingIcon = {
                                Text(
                                    "¥",
                                    fontSize = 14.sp,
                                    color = Color(0xFF10B981)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = Color.White
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                        )
                        
                        IconButton(
                            onClick = {
                                val price = priceText.toFloatOrNull()
                                if (price != null && price > 0) {
                                    onPriceChange(price)
                                }
                                showPriceInput = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "确认",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                priceText = monetizationItem?.price?.toInt()?.toString() ?: ""
                                showPriceInput = false
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "取消",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
