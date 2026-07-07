package com.example.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.WeightRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: WeightViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val records by viewModel.allRecords.collectAsStateWithLifecycle()
    val recordsAsc by viewModel.allRecordsAsc.collectAsStateWithLifecycle()

    // Dialog control states
    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedRecordForEdit by remember { mutableStateOf<WeightRecord?>(null) }
    var showDetailDialog by remember { mutableStateOf<WeightRecord?>(null) }

    // User settings
    var userHeight by remember { mutableStateOf(viewModel.getHeight()) }
    var userTargetWeight by remember { mutableStateOf(viewModel.getTargetWeight()) }
    var userNickname by remember { mutableStateOf(viewModel.getNickName()) }

    // Active state derived values
    val currentRecord = records.firstOrNull()
    val initialRecord = records.lastOrNull()
    val currentWeight = currentRecord?.weight ?: 0.0
    val startWeight = initialRecord?.weight ?: 0.0
    val weightLoss = if (currentWeight > 0.0 && startWeight > 0.0) {
        currentWeight - startWeight
    } else {
        0.0
    }

    // BMI calculation
    val bmiValue = if (currentWeight > 0.0 && userHeight > 0.0) {
        val heightInMeters = userHeight / 100.0
        currentWeight / (heightInMeters * heightInMeters)
    } else {
        0.0
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${userNickname}的体重记录",
                            fontWeight = FontWeight.Bold,
                            color = PinkPrimary,
                            fontSize = 20.sp
                        )
                        Icon(
                            imagePath = Icons.Default.Favorite,
                            contentDescription = "Love",
                            tint = PinkPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "设置",
                            tint = PinkPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = PinkPrimary
                ),
                modifier = Modifier.shadow(2.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedRecordForEdit = null
                    showAddDialog = true
                },
                containerColor = PinkPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .shadow(8.dp, CircleShape)
                    .testTag("add_record_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "记体重",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(PinkLight) // Soft light-pink background for that romantic layout
        ) {
            // Main scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Greeting and Motivation Banner
                GreetingBanner(userNickname, records.size)

                // Quick stats cards
                StatsSection(
                    currentWeight = currentWeight,
                    startWeight = startWeight,
                    weightLoss = weightLoss,
                    targetWeight = userTargetWeight,
                    bmi = bmiValue
                )

                // Interactive beautiful curve trend chart
                if (recordsAsc.size >= 2) {
                    WeightTrendChart(recordsAsc)
                }

                // Header for the Grid overview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp, 16.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(PinkPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "体重宫格总览",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDarkGray
                        )
                    }

                    Text(
                        text = "共 ${records.size} 次记录",
                        fontSize = 12.sp,
                        color = TextLightGray
                    )
                }

                if (records.isEmpty()) {
                    // Beautiful Empty State
                    EmptyStateCard(onImportDemo = {
                        viewModel.importDemoData()
                        Toast.makeText(context, "成功导入演示记录 🌸", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    // Elegant Grid Layout
                    // Since Compose LazyVerticalGrid cannot easily be put inside a VerticalScroll directly
                    // without a fixed height, we can chunk the records list and render them in Rows or
                    // use a custom flow layout, OR we can specify a fixed height for the grid.
                    // Even better: we can construct a grid using Columns and Rows manually to ensure perfect nesting and scrolling,
                    // which is extremely robust and prevents nesting crash!
                    GridOverviewManual(
                        records = records,
                        onRecordClick = { record ->
                            showDetailDialog = record
                        },
                        onRecordLongClick = { record ->
                            selectedRecordForEdit = record
                            showAddDialog = true
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // --- DIALOGS ---

    // 1. Add/Edit Weight Record Dialog
    if (showAddDialog) {
        AddEditWeightDialog(
            record = selectedRecordForEdit,
            onDismiss = { showAddDialog = false },
            onSave = { date, weight, fat, note ->
                viewModel.addOrUpdateRecord(date, weight, fat, note)
                showAddDialog = false
                Toast.makeText(context, "记录成功 🎉", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 2. Settings Dialog (Height, Nickname, Target Weight)
    if (showSettingsDialog) {
        SettingsDialog(
            currentName = userNickname,
            currentHeight = userHeight,
            currentTarget = userTargetWeight,
            onDismiss = { showSettingsDialog = false },
            onSave = { name, height, target ->
                userNickname = name
                userHeight = height
                userTargetWeight = target
                viewModel.saveNickName(name)
                viewModel.saveHeight(height)
                viewModel.saveTargetWeight(target)
                showSettingsDialog = false
                Toast.makeText(context, "设置已保存 ✨", Toast.LENGTH_SHORT).show()
            },
            onClearData = {
                viewModel.clearAllData()
                showSettingsDialog = false
                Toast.makeText(context, "所有数据已清空", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 3. Detail View & Quick Edit/Delete Dialog
    showDetailDialog?.let { record ->
        DetailDialog(
            record = record,
            onDismiss = { showDetailDialog = null },
            onEdit = {
                selectedRecordForEdit = record
                showDetailDialog = null
                showAddDialog = true
            },
            onDelete = {
                viewModel.deleteRecord(record)
                showDetailDialog = null
                Toast.makeText(context, "已删除记录", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun Icon(imagePath: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, tint: Color, modifier: Modifier) {
    androidx.compose.material3.Icon(
        imageVector = imagePath,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier
    )
}

// --- SUB-COMPONENTS ---

@Composable
fun GreetingBanner(nickname: String, recordsCount: Int) {
    val quote = when {
        recordsCount == 0 -> "开始记录第一笔体重，遇见更好的自己吧~ ✨"
        recordsCount < 3 -> "万事开头难，静琳已经迈出了重要的一步！🌸"
        recordsCount < 7 -> "太棒了！记录是一种好习惯，今天也要美美哒~ 💕"
        else -> "持之以恒，陈静琳是最棒的！每一天都在健康变美 🌟"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "嗨，${nickname} 👋",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PinkPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = quote,
                    fontSize = 13.sp,
                    color = TextDarkGray,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            // Beautiful stylized pink weight scale / flower decoration
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(PinkLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Love Scale",
                    tint = PinkPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun StatsSection(
    currentWeight: Double,
    startWeight: Double,
    weightLoss: Double,
    targetWeight: Double,
    bmi: Double
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // First Row: Current & Start & Change
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Current Weight
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("当前体重", fontSize = 11.sp, color = TextLightGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (currentWeight > 0) String.format("%.1f", currentWeight) else "—",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = PinkPrimary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("kg", fontSize = 11.sp, color = TextLightGray)
                    }
                }
            }

            // Target Weight
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("目标体重", fontSize = 11.sp, color = TextLightGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f", targetWeight),
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = TextDarkGray
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("kg", fontSize = 11.sp, color = TextLightGray)
                    }
                }
            }

            // Total Weight Loss
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("累计减重", fontSize = 11.sp, color = TextLightGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        val isLoss = weightLoss <= 0
                        val lossValue = Math.abs(weightLoss)
                        Text(
                            text = if (currentWeight > 0) {
                                "${if (isLoss) "-" else "+"}${String.format("%.1f", lossValue)}"
                            } else "—",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = if (isLoss && currentWeight > 0) SuccessGreen else if (currentWeight > 0) PinkPrimary else TextDarkGray
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("kg", fontSize = 11.sp, color = TextLightGray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Second Row: Progress to Target & BMI
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // BMI Info
                    val bmiLabel = when {
                        bmi == 0.0 -> "未知"
                        bmi < 18.5 -> "偏瘦 🫧"
                        bmi < 24.0 -> "标准 🌟"
                        bmi < 28.0 -> "偏胖 🍰"
                        else -> "丰满 🍎"
                    }
                    val bmiColor = when {
                        bmi < 18.5 -> PinkSecondary
                        bmi < 24.0 -> SuccessGreen
                        else -> PinkPrimary
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("BMI 指数: ", fontSize = 12.sp, color = TextLightGray)
                        Text(
                            text = if (bmi > 0) String.format("%.1f", bmi) else "—",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextDarkGray
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (bmi > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bmiColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = bmiLabel,
                                    fontSize = 10.sp,
                                    color = bmiColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Progress Text
                    if (currentWeight > 0) {
                        val distance = currentWeight - targetWeight
                        if (distance <= 0) {
                            Text("已达成目标！超棒 🎉", fontSize = 12.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        } else {
                            Text("距离目标还差 ${String.format("%.1f", distance)} kg", fontSize = 11.sp, color = PinkPrimary)
                        }
                    }
                }

                if (currentWeight > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Progress Bar
                    val progress = if (startWeight > targetWeight) {
                        val totalToLose = startWeight - targetWeight
                        val lostSoFar = startWeight - currentWeight
                        if (totalToLose > 0) {
                            (lostSoFar / totalToLose).coerceIn(0.0, 1.0).toFloat()
                        } else 1f
                    } else {
                        if (currentWeight <= targetWeight) 1f else 0f
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = PinkPrimary,
                        trackColor = PinkLight,
                    )
                }
            }
        }
    }
}

@Composable
fun WeightTrendChart(records: List<WeightRecord>) {
    // Take at most last 10 records to keep chart clean
    val chartRecords = if (records.size > 10) records.takeLast(10) else records
    val weights = chartRecords.map { it.weight }
    val maxWeight = weights.maxOrNull() ?: 0.0
    val minWeight = weights.minOrNull() ?: 0.0
    val range = maxWeight - minWeight
    val paddingFactor = if (range == 0.0) 1.0 else range * 0.15
    val yMax = maxWeight + paddingFactor
    val yMin = (minWeight - paddingFactor).coerceAtLeast(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = "Trend",
                    tint = PinkPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "体重变化趋势",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDarkGray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Curve drawing area
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val width = size.width
                val height = size.height
                val pointsCount = chartRecords.size
                val stepX = width / (pointsCount - 1).coerceAtLeast(1)

                val points = chartRecords.mapIndexed { index, record ->
                    val x = index * stepX
                    val y = if (yMax - yMin == 0.0) {
                        height / 2f
                    } else {
                        height - ((record.weight - yMin) / (yMax - yMin) * height).toFloat()
                    }
                    Offset(x, y)
                }

                // Draw background gradient under line
                if (points.isNotEmpty()) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, height)
                        lineTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            // Smooth connection using cubic bezier
                            val prev = points[i - 1]
                            val curr = points[i]
                            val conX1 = prev.x + (curr.x - prev.x) / 2f
                            cubicTo(conX1, prev.y, conX1, curr.y, curr.x, curr.y)
                        }
                        lineTo(points.last().x, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PinkPrimary.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Draw smooth trend line
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            val conX1 = prev.x + (curr.x - prev.x) / 2f
                            cubicTo(conX1, prev.y, conX1, curr.y, curr.x, curr.y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = PinkPrimary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw data points
                    points.forEachIndexed { index, point ->
                        // White circle border
                        drawCircle(
                            color = Color.White,
                            radius = 6.dp.toPx(),
                            center = point
                        )
                        // Inner pink dot
                        drawCircle(
                            color = PinkPrimary,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis Date labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                chartRecords.forEach { record ->
                    val rawDate = record.date // yyyy-MM-dd
                    val displayDate = try {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(rawDate)
                        SimpleDateFormat("MM.dd", Locale.getDefault()).format(date!!)
                    } catch (e: Exception) {
                        rawDate
                    }
                    Text(
                        text = displayDate,
                        fontSize = 10.sp,
                        color = TextLightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(36.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(onImportDemo: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(PinkLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = "Empty",
                    tint = PinkPrimary,
                    modifier = Modifier.size(42.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "还没有体重记录哦 🌸",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextDarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击下方的粉色 '+' 按钮开始记录今天的体重吧！或者一键导入演示记录体验精美界面！",
                fontSize = 12.sp,
                color = TextLightGray,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onImportDemo,
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Import",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("导入演示数据", fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

// Custom Grid implementation to prevent nesting issues inside vertical scroll
@Composable
fun GridOverviewManual(
    records: List<WeightRecord>,
    onRecordClick: (WeightRecord) -> Unit,
    onRecordLongClick: (WeightRecord) -> Unit
) {
    // Group records in rows of 3 columns
    val columns = 3
    val rows = (records.size + columns - 1) / columns

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (c in 0 until columns) {
                    val index = r * columns + c
                    if (index < records.size) {
                        val record = records[index]
                        // Calculate weight change compared to the chronologically prior record if available
                        // Since 'records' list is sorted DESC (newest first), the previous chronological record
                        // is at 'index + 1'
                        val previousRecord = if (index + 1 < records.size) records[index + 1] else null
                        val delta = if (previousRecord != null) {
                            record.weight - previousRecord.weight
                        } else {
                            null
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.9f) // Slightly taller than wide for beautiful spacing
                        ) {
                            GridItem(
                                record = record,
                                delta = delta,
                                onClick = { onRecordClick(record) },
                                onLongClick = { onRecordLongClick(record) }
                            )
                        }
                    } else {
                        // Empty spacer to occupy empty grid cells and keep alignments perfect
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun GridItem(
    record: WeightRecord,
    delta: Double?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    // Format date for display: "2026-07-07" -> "7.7" and weekday "周二"
    val dateDisplay = remember(record.date) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(record.date)
            val cal = Calendar.getInstance().apply { time = date!! }
            val monthDay = SimpleDateFormat("M/d", Locale.getDefault()).format(date)
            
            val weekdayStr = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "周日"
                Calendar.MONDAY -> "周一"
                Calendar.TUESDAY -> "周二"
                Calendar.WEDNESDAY -> "周三"
                Calendar.THURSDAY -> "周四"
                Calendar.FRIDAY -> "周五"
                Calendar.SATURDAY -> "周六"
                else -> ""
            }
            Pair(monthDay, weekdayStr)
        } catch (e: Exception) {
            Pair(record.date, "")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(record.date) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            }
            .testTag("weight_grid_item_${record.date}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PinkTertiary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Date Label Row
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = dateDisplay.first,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextDarkGray
                )
                Text(
                    text = dateDisplay.second,
                    fontSize = 10.sp,
                    color = TextLightGray
                )
            }

            // Weight Display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.1f", record.weight),
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = PinkPrimary
                )
                Text(
                    text = "kg",
                    fontSize = 9.sp,
                    color = TextLightGray
                )
            }

            // Delta change bubble
            if (delta != null) {
                val isDrop = delta <= 0
                val deltaAbs = Math.abs(delta)
                val deltaColor = if (isDrop) SuccessGreen else PinkPrimary
                val deltaBg = if (isDrop) SuccessGreen.copy(alpha = 0.12f) else PinkPrimary.copy(alpha = 0.12f)
                val symbol = if (isDrop) "↓" else "↑"

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(deltaBg)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$symbol ${String.format("%.1f", deltaAbs)}",
                        fontSize = 10.sp,
                        color = deltaColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            } else {
                // Spacer or tiny note/mood indicator if no delta
                if (!record.note.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "有日记",
                        tint = PinkSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                } else {
                    Text(
                        text = "第一天 🌸",
                        fontSize = 9.sp,
                        color = PinkSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// --- COMPLEX DIALOG IMPLEMENTATIONS ---

@Composable
fun AddEditWeightDialog(
    record: WeightRecord?,
    onDismiss: () -> Unit,
    onSave: (date: String, weight: Double, fat: Double?, note: String?) -> Unit
) {
    val context = LocalContext.current
    val isEdit = record != null

    // Date state
    var selectedDate by remember {
        mutableStateOf(record?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    // Weight input state (Double)
    var weightText by remember {
        mutableStateOf(record?.weight?.toString() ?: "52.0")
    }

    // Fat percentage input state
    var fatText by remember {
        mutableStateOf(record?.fatPercentage?.toString() ?: "")
    }

    // Note state
    var noteText by remember {
        mutableStateOf(record?.note ?: "")
    }

    // Quick emoji tags
    val emojis = listOf("🥰", "😊", "😐", "😭", "🍕", "🏃‍♀️", "🥗", "🍵")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isEdit) "修改体重记录 🌸" else "记录今日体重 🌸",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PinkPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Date Picker Button
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        // If parsing existing selected date
                        try {
                            val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)
                            if (parsedDate != null) calendar.time = parsedDate
                        } catch (e: Exception) {}

                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                                selectedDate = formattedDate
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PinkPrimary),
                    border = BorderStroke(1.dp, PinkTertiary)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = PinkPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "日期: $selectedDate")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weight Counter panel (with + and - adjustments)
                Text("体重 (kg)", fontSize = 12.sp, color = TextLightGray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Small decrement -1.0
                    IconButton(
                        onClick = {
                            val w = weightText.toDoubleOrNull() ?: 50.0
                            weightText = String.format("%.1f", (w - 1.0).coerceAtLeast(10.0))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(PinkLight, CircleShape)
                    ) {
                        Text("-1", color = PinkPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Small decrement -0.1
                    IconButton(
                        onClick = {
                            val w = weightText.toDoubleOrNull() ?: 50.0
                            weightText = String.format("%.1f", (w - 0.1).coerceAtLeast(10.0))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(PinkLight, CircleShape)
                    ) {
                        Text("-0.1", color = PinkPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }

                    // Weight Display Textfield
                    TextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = LocalTextStyle.current.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = PinkPrimary
                        ),
                        modifier = Modifier
                            .width(120.dp)
                            .testTag("weight_input_field"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = PinkPrimary,
                            unfocusedIndicatorColor = PinkTertiary
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    // Small increment +0.1
                    IconButton(
                        onClick = {
                            val w = weightText.toDoubleOrNull() ?: 50.0
                            weightText = String.format("%.1f", w + 0.1)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(PinkLight, CircleShape)
                    ) {
                        Text("+0.1", color = PinkPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Small increment +1.0
                    IconButton(
                        onClick = {
                            val w = weightText.toDoubleOrNull() ?: 50.0
                            weightText = String.format("%.1f", w + 1.0)
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(PinkLight, CircleShape)
                    ) {
                        Text("+1", color = PinkPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Body Fat Input (Optional)
                OutlinedTextField(
                    value = fatText,
                    onValueChange = { fatText = it },
                    label = { Text("体脂率 (%, 可不填)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkPrimary,
                        unfocusedBorderColor = PinkTertiary,
                        focusedLabelColor = PinkPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notes / Mood
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("今天心情/备注/饮食 (可不填)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkPrimary,
                        unfocusedBorderColor = PinkTertiary,
                        focusedLabelColor = PinkPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick emojis
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    emojis.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(PinkLight)
                                .clickable {
                                    if (!noteText.contains(emoji)) {
                                        noteText = if (noteText.isEmpty()) emoji else "$noteText $emoji"
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = emoji, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save and Cancel buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDarkGray),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            val weight = weightText.toDoubleOrNull()
                            if (weight == null || weight <= 0.0) {
                                Toast.makeText(context, "请输入有效的体重数值哦 🌸", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val fat = fatText.toDoubleOrNull()
                            onSave(selectedDate, weight, fat, noteText.trim())
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_record_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("保存记录", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    currentName: String,
    currentHeight: Double,
    currentTarget: Double,
    onDismiss: () -> Unit,
    onSave: (name: String, height: Double, target: Double) -> Unit,
    onClearData: () -> Unit
) {
    var nameText by remember { mutableStateOf(currentName) }
    var heightText by remember { mutableStateOf(currentHeight.toString()) }
    var targetText by remember { mutableStateOf(currentTarget.toString()) }
    var showConfirmClear by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "个人配置 & 目标 💖",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = PinkPrimary
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Name input
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("小名/姓名") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkPrimary,
                        unfocusedBorderColor = PinkTertiary,
                        focusedLabelColor = PinkPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Height input (needed for BMI)
                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("身高 (cm, 用于计算BMI)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkPrimary,
                        unfocusedBorderColor = PinkTertiary,
                        focusedLabelColor = PinkPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Target weight input
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("目标体重 (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PinkPrimary,
                        unfocusedBorderColor = PinkTertiary,
                        focusedLabelColor = PinkPrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDarkGray)
                    ) {
                        Text("关闭")
                    }

                    Button(
                        onClick = {
                            val height = heightText.toDoubleOrNull() ?: 165.0
                            val target = targetText.toDoubleOrNull() ?: 50.0
                            onSave(nameText.trim().ifEmpty { "陈静琳" }, height, target)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_settings_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("保存", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = DividerPink)
                Spacer(modifier = Modifier.height(16.dp))

                if (!showConfirmClear) {
                    TextButton(
                        onClick = { showConfirmClear = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("清空历史数据 ⚠️", fontSize = 12.sp)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("确定要清空全部数据吗？此操作不可逆！", color = Color.Red, fontSize = 11.sp)
                        Row {
                            TextButton(onClick = { showConfirmClear = false }) {
                                Text("取消", fontSize = 12.sp, color = TextDarkGray)
                            }
                            TextButton(
                                onClick = onClearData,
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                            ) {
                                Text("确定清空", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailDialog(
    record: WeightRecord,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large styled date header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PinkLight, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = record.date,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PinkPrimary
                        )
                        Text(
                            text = "体重详细记录",
                            fontSize = 12.sp,
                            color = TextLightGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats rows
                DetailRow(label = "体重", value = "${String.format("%.1f", record.weight)} kg")
                if (record.fatPercentage != null) {
                    DetailRow(label = "体脂率", value = "${String.format("%.1f", record.fatPercentage)} %")
                }
                
                DetailRow(
                    label = "日记/备注",
                    value = if (record.note.isNullOrBlank()) "无备注 🌸" else record.note,
                    isTextOnly = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!showConfirmDelete) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Edit, "Edit", Color.White, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("修改", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showConfirmDelete = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Delete, "Delete", Color.Red, Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("删除", color = Color.Red)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("确定删除该日记录吗？", color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { showConfirmDelete = false },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("算啦", color = TextDarkGray)
                            }
                            Button(
                                onClick = onDelete,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("确定删除", color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onDismiss) {
                    Text("关闭", color = TextLightGray)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isTextOnly: Boolean = false) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 13.sp, color = TextLightGray, fontWeight = FontWeight.Medium)
            if (!isTextOnly) {
                Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDarkGray)
            }
        }
        if (isTextOnly) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SoftWhite, RoundedCornerShape(10.dp))
                    .border(BorderStroke(1.dp, DividerPink), RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 13.sp,
                    color = TextDarkGray,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = DividerPink)
        }
    }
}
