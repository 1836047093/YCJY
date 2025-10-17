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
    
    // 初始化所有推荐的付费内容
    LaunchedEffect(recommendedItems) {
        if (recommendedItems.isNotEmpty() && monetizationItems.isEmpty()) {
            // 将所有推荐的付费内容添加到列表中
            val initialItems = recommendedItems.map { itemType ->
                MonetizationItem(type = itemType, isEnabled = true)
            }
            onMonetizationItemsChange(initialItems)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
    ) {
        Text(
            text = "💰 选择付费内容",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
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
                    // 从列表中查找对应的付费内容项
                    val existingItem = monetizationItems.find { it.type == itemType }
                    
                    if (existingItem != null) {
                        MonetizationItemCard(
                            itemType = itemType,
                            monetizationItem = existingItem,
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
    onPriceChange: (Float?) -> Unit
) {
    var priceText by remember(monetizationItem?.price) {
        mutableStateOf(monetizationItem?.price?.toInt()?.toString() ?: "")
    }
    var showPriceInput by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF10B981).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = itemType.displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
                Text(
                    text = itemType.description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 价格设置区域
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
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
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
                                        if (price != null && price >= 6f && price <= 648f) {
                                            onPriceChange(price)
                                            showPriceInput = false
                                        }
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
                            
                            // 快捷价格选择按钮
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "快捷选择",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(6, 18, 30, 68).forEach { price ->
                                        QuickPriceButton(
                                            price = price,
                                            onClick = {
                                                priceText = price.toString()
                                                onPriceChange(price.toFloat())
                                                showPriceInput = false
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(128, 198, 328, 648).forEach { price ->
                                        QuickPriceButton(
                                            price = price,
                                            onClick = {
                                                priceText = price.toString()
                                                onPriceChange(price.toFloat())
                                                showPriceInput = false
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }
}

/**
 * 快捷价格按钮
 */
@Composable
fun QuickPriceButton(
    price: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
            contentColor = Color(0xFF10B981)
        ),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "¥$price",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
