import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../api_service.dart';
import '../../theme_provider.dart';
import '../widgets/common.dart';

class ContainerDetailScreen extends StatefulWidget {
  final Map container;
  final ApiService api;
  final VoidCallback onUpdate;
  const ContainerDetailScreen({super.key, required this.container, required this.api, required this.onUpdate});

  @override
  State<ContainerDetailScreen> createState() => _ContainerDetailScreenState();
}

class _ContainerDetailScreenState extends State<ContainerDetailScreen> {
  Timer? _timer;
  List<FlSpot> cpuPoints = [];
  double _time = 0;
  bool _processing = false;
  String _currentStatus = 'loading';

  @override
  void initState() {
    super.initState();
    _currentStatus = widget.container['status'] ?? 'unknown';
    // Инициализируем график нулями
    for(int i=0; i<15; i++) cpuPoints.add(FlSpot(i.toDouble(), 0));
    _time = 14;
    _startMonitoring();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  double _parseCpu(dynamic val) {
    if (val == null) return 0.0;
    // Если это число (напр. 1.5, как возвращает Python), просто возвращаем его
    if (val is num) return val.toDouble();
    
    // Если это строка (напр. "1.5%")
    String s = val.toString().replaceAll('%', '').replaceAll('MB', '').trim();
    // Удаляем все нечисловые символы кроме точки
    s = s.replaceAll(RegExp(r'[^0-9.]'), '');
    return double.tryParse(s) ?? 0.0;
  }

  void _startMonitoring() {
    _timer = Timer.periodic(const Duration(seconds: 3), (_) async {
      if (!mounted) return;
      try {
        // !!! FIX HERE: _get уже возвращает ['data']. 
        // Не нужно делать res['data'], используем res напрямую.
        final data = await widget.api.getContainerDetails(widget.container['id']);
        
        if (!mounted || data == null) return;

        final stats = data['stats'];
        final status = data['status'];

        double cpu = 0.0;
        if (stats != null) {
            cpu = _parseCpu(stats['cpu_usage']);
        }

        setState(() {
          _currentStatus = status ?? 'unknown';
          _time += 1;
          cpuPoints.add(FlSpot(_time, cpu));
          if (cpuPoints.length > 20) cpuPoints.removeAt(0);
        });
      } catch (e) {
        debugPrint("Error fetching container stats: $e");
      }
    });
  }

  Future<void> _action(String act) async {
    setState(() => _processing = true);
    try {
      if (act == 'reinstall') await widget.api.reinstallContainer(widget.container['id']);
      else if (act == 'delete') await widget.api.deleteContainer(widget.container['id']);
      else await widget.api.containerAction(widget.container['id'], act);

      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Успешно: $act')));
      widget.onUpdate();
      if (act == 'delete') Navigator.pop(context);
    } catch(e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('Ошибка: $e')));
    } finally {
      setState(() => _processing = false);
    }
  }

  Future<void> _rename() async {
    final controller = TextEditingController();
    final isLight = appSettings.theme == AppTheme.light;
    await showCupertinoDialog(
      context: context,
      builder: (ctx) => CupertinoAlertDialog(
        title: const Text("Переименовать"),
        content: Padding(
          padding: const EdgeInsets.only(top: 10),
          child: CupertinoTextField(controller: controller, placeholder: "Новое имя", style: TextStyle(color: isLight ? Colors.black : Colors.white)),
        ),
        actions: [
          CupertinoDialogAction(child: const Text("Отмена"), onPressed: () => Navigator.pop(ctx)),
          CupertinoDialogAction(child: const Text("Сохранить"), onPressed: () async {
            Navigator.pop(ctx);
            if (controller.text.isNotEmpty) {
               setState(() => _processing = true);
               try {
                 await widget.api.renameContainer(widget.container['id'], controller.text);
                 widget.onUpdate();
                 Navigator.pop(context);
               } catch(e) {
                 ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text("Ошибка: $e")));
                 setState(() => _processing = false);
               }
            }
          }),
        ],
      )
    );
  }

  @override
  Widget build(BuildContext context) {
    final c = widget.container;
    final isRunning = _currentStatus == 'running';
    final statusColor = isRunning ? const Color(0xFF32D74B) : const Color(0xFFFF453A);
    final statusText = isRunning ? "Активен" : "Отключен";
    final primaryColor = Theme.of(context).primaryColor;

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      appBar: AppBar(backgroundColor: Colors.transparent, title: Text(c['container_name'], style: const TextStyle(fontWeight: FontWeight.bold))),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            // Status Card
            GlassCard(
              child: Row(
                children: [
                  Container(
                    width: 60, height: 60,
                    decoration: BoxDecoration(color: statusColor.withOpacity(0.1), borderRadius: BorderRadius.circular(18), border: Border.all(color: statusColor.withOpacity(0.4))),
                    child: Icon(CupertinoIcons.cube_box_fill, color: statusColor, size: 28),
                  ),
                  const SizedBox(width: 16),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(statusText, style: TextStyle(color: statusColor, fontWeight: FontWeight.w800, fontSize: 18)),
                      const SizedBox(height: 4),
                      Text("Node: ${c['server_info']['name']}", style: const TextStyle(color: Colors.grey, fontSize: 12)),
                    ],
                  )
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Pro Graph
            GlassCard(
              padding: 0,
              child: Padding(
                padding: const EdgeInsets.fromLTRB(16, 20, 20, 10),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(left: 10, bottom: 10),
                      child: Text("НАГРУЗКА CPU (%)", style: TextStyle(color: Colors.grey, fontSize: 11, fontWeight: FontWeight.bold, letterSpacing: 1)),
                    ),
                    SizedBox(
                      height: 200,
                      child: LineChart(
                        LineChartData(
                          gridData: FlGridData(
                            show: true,
                            drawVerticalLine: false,
                            getDrawingHorizontalLine: (value) => FlLine(color: Colors.grey.withOpacity(0.1), strokeWidth: 1),
                          ),
                          titlesData: const FlTitlesData(show: false),
                          borderData: FlBorderData(show: false),
                          minY: 0, maxY: 100,
                          lineBarsData: [
                            LineChartBarData(
                              spots: cpuPoints,
                              isCurved: true,
                              curveSmoothness: 0.3,
                              color: primaryColor,
                              barWidth: 3,
                              isStrokeCapRound: true,
                              dotData: const FlDotData(show: false),
                              belowBarData: BarAreaData(
                                show: true, 
                                gradient: LinearGradient(
                                  colors: [primaryColor.withOpacity(0.3), primaryColor.withOpacity(0.0)],
                                  begin: Alignment.topCenter,
                                  end: Alignment.bottomCenter
                                )
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 24),

            // Pro Control Center Buttons
            Wrap(
              spacing: 12, runSpacing: 12,
              children: [
                _ActionBtn(icon: CupertinoIcons.play_fill, label: "Start", color: Colors.green, onTap: () => _action('start')),
                _ActionBtn(icon: CupertinoIcons.stop_fill, label: "Stop", color: Colors.red, onTap: () => _action('stop')),
                _ActionBtn(icon: CupertinoIcons.restart, label: "Restart", color: Colors.orange, onTap: () => _action('restart')),
                _ActionBtn(icon: CupertinoIcons.arrow_2_circlepath, label: "Reinstall", color: Colors.purple, onTap: () => _action('reinstall')),
                _ActionBtn(icon: CupertinoIcons.pencil, label: "Rename", color: Colors.blue, onTap: _rename),
                _ActionBtn(icon: CupertinoIcons.trash, label: "Delete", color: Colors.redAccent, onTap: () => _action('delete')),
              ],
            ),

            if (_processing) const Padding(padding: EdgeInsets.only(top: 24), child: CupertinoActivityIndicator(radius: 15))
          ],
        ),
      ),
    );
  }
}

class _ActionBtn extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final VoidCallback onTap;
  const _ActionBtn({required this.icon, required this.label, required this.color, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final isLight = appSettings.theme == AppTheme.light;
    return BouncyBtn(
      onTap: onTap,
      child: Container(
        width: 100, height: 100, // Квадратные большие кнопки
        decoration: BoxDecoration(
          color: isLight ? Colors.white : const Color(0xFF1E293B),
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: isLight ? Colors.black.withOpacity(0.05) : Colors.white10),
          boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 10, offset: const Offset(0,5))]
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(color: color.withOpacity(0.15), shape: BoxShape.circle),
              child: Icon(icon, color: color, size: 28),
            ),
            const SizedBox(height: 10),
            Text(label, style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: isLight ? Colors.black87 : Colors.white70)),
          ],
        ),
      ),
    );
  }
}