@Composable
fun InGameSettingsScreen(navController: NavController) {
    var soundEnabled by remember { mutableStateOf(true) }
    var musicEnabled by remember { mutableStateOf(true) }
    var gameSpeed by remember { mutableStateOf(1f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            Text(
                text = "游戏设置",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 保存游戏按钮
            GameMenuButton(
                text = "保存游戏",
                onClick = {
                    // TODO: 实现保存游戏逻辑
                }
            )

            // 音效开关
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E3A8A).copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "音效",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // 音乐开关
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E3A8A).copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "音乐",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = musicEnabled,
                        onCheckedChange = { musicEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF4CAF50),
                            checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // 游戏速度设置
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E3A8A).copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "游戏速度: ${gameSpeed.toInt()}x",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Slider(
                        value = gameSpeed,
                        onValueChange = { gameSpeed = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF4CAF50),
                            activeTrackColor = Color(0xFF4CAF50),
                            inactiveTrackColor = Color.Gray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 返回游戏按钮
            GameMenuButton(
                text = "返回游戏",
                onClick = {
                    navController.popBackStack()
                }
            )

            // 返回主菜单按钮
            GameMenuButton(
                text = "返回主菜单",
                onClick = {
                    navController.navigate("main_menu") {
                        popUpTo("main_menu") { inclusive = true }
                    }
                }
            )
        }
    }
}
