import 'dart:ui';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../api_service.dart';
import '../../theme_provider.dart';
import '../widgets/common.dart';
import 'login_screen.dart';
import 'container_detail_screen.dart';

class RootScreen extends StatefulWidget {
  final ApiService api;
  const RootScreen({super.key, required this.api});
  @override
  State<RootScreen> createState() => _RootScreenState();
}

class _RootScreenState extends State<RootScreen> {
  int _idx = 0;
  bool _loading = true;
  Map<String, dynamic>? _data;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final data = await widget.api.getDashboard();
      if (mounted) setState(() { _data = data; _loading = false; });
    } catch (e) {
      if (e.toString().contains("AUTH_EXPIRED")) {
        if (mounted) Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => LoginScreen(api: widget.api)));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Scaffold(body: Center(child: CupertinoActivityIndicator(radius: 15)));

    final profile = _data!['profile'];
    final bool isAdmin = (profile['effective_role'] ?? 0) >= 2;
    final isNewYear = appSettings.theme == AppTheme.newYear;
    final isLight = appSettings.theme == AppTheme.light;

    final screens = [
      DashboardTab(profile: profile, containersCount: (_data!['containers'] as List).length, api: widget.api),
      UserBotsTab(containers: _data!['containers'], api: widget.api, onUpdate: _load),
      if (isAdmin) AdminTab(api: widget.api),
      SettingsTab(api: widget.api, profile: profile),
    ];

    return Scaffold(
      body: Stack(
        children: [
          Container(decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter, end: Alignment.bottomCenter,
              colors: isLight 
                ? [const Color(0xFFF2F2F7), const Color(0xFFE5E5EA)]
                : (isNewYear 
                    ? [const Color(0xFF0F172A), const Color(0xFF151515)] 
                    : [const Color(0xFF000000), const Color(0xFF111111)])
            )
          )),
          if (isNewYear) const SnowOverlay(),
          SafeArea(bottom: false, child: screens[_idx]),
        ],
      ),
      bottomNavigationBar: ClipRRect(
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 30, sigmaY: 30),
          child: Container(
            decoration: BoxDecoration(
              border: Border(top: BorderSide(color: isLight ? Colors.black.withOpacity(0.05) : Colors.white.withOpacity(0.08), width: 0.5)),
              color: Theme.of(context).scaffoldBackgroundColor.withOpacity(0.85),
            ),
            child: BottomNavigationBar(
              currentIndex: _idx,
              onTap: (i) => setState(() => _idx = i),
              items: [
                const BottomNavigationBarItem(icon: Icon(CupertinoIcons.home), label: 'Обзор'),
                const BottomNavigationBarItem(icon: Icon(CupertinoIcons.cube_box), label: 'Боты'),
                if (isAdmin) const BottomNavigationBarItem(icon: Icon(CupertinoIcons.shield_fill), label: 'Админ'),
                const BottomNavigationBarItem(icon: Icon(CupertinoIcons.settings), label: 'Настройки'),
              ],
              backgroundColor: Colors.transparent,
              selectedItemColor: Theme.of(context).primaryColor,
              unselectedItemColor: Colors.grey,
              type: BottomNavigationBarType.fixed,
              showUnselectedLabels: true,
              selectedLabelStyle: const TextStyle(fontWeight: FontWeight.w600, fontSize: 11),
              unselectedLabelStyle: const TextStyle(fontSize: 11),
              elevation: 0,
            ),
          ),
        ),
      ),
    );
  }
}

class DashboardTab extends StatelessWidget {
  final Map profile;
  final int containersCount;
  final ApiService api;

  const DashboardTab({super.key, required this.profile, required this.containersCount, required this.api});

  String _roleName(int role) {
    if (role == 5) return 'Владелец';
    if (role == 4) return 'Co-Owner';
    if (role >= 2) return 'Администратор';
    if (role == 1) return 'Jr. Admin';
    return 'Пользователь';
  }

  @override
  Widget build(BuildContext context) {
    final double balance = double.tryParse(profile['balance'].toString()) ?? 0.0;
    final int level = profile['level_info']?['level'] ?? 1;
    final int xp = profile['level_info']?['xp'] ?? 0;
    final int nextXp = profile['level_info']?['next_level_xp'] ?? 100;
    final double progress = (xp / max(1, nextXp)).clamp(0.0, 1.0);

    final avatarUrl = api.getAvatarUrl(profile['user_id']);
    final primaryColor = Theme.of(context).primaryColor;
    final isLight = appSettings.theme == AppTheme.light;

    return ListView(
      padding: const EdgeInsets.all(24),
      children: [
        Row(
          children: [
            Container(
              decoration: BoxDecoration(shape: BoxShape.circle, border: Border.all(color: primaryColor, width: 2), boxShadow: [BoxShadow(color: primaryColor.withOpacity(0.2), blurRadius: 10)]),
              child: CircleAvatar(
                radius: 30,
                backgroundColor: isLight ? Colors.white : const Color(0xFF1C1C1E),
                backgroundImage: NetworkImage(avatarUrl),
                onBackgroundImageError: (_, __) => const Icon(CupertinoIcons.person),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    "Привет, ${profile['first_name']}!", 
                    style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: isLight ? Colors.black : Colors.white),
                    overflow: TextOverflow.ellipsis,
                    maxLines: 1,
                  ),
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                        decoration: BoxDecoration(
                          color: primaryColor.withOpacity(0.1), 
                          borderRadius: BorderRadius.circular(8),
                          border: Border.all(color: primaryColor.withOpacity(0.2))
                        ),
                        child: Text(_roleName(profile['effective_role'] ?? 0), style: TextStyle(fontSize: 11, color: primaryColor, fontWeight: FontWeight.w600)),
                      ),
                      const SizedBox(width: 8),
                      Flexible(
                        child: Text("@${profile['username'] ?? 'unknown'}", 
                          style: TextStyle(color: Colors.grey[500], fontSize: 12),
                          overflow: TextOverflow.ellipsis,
                          maxLines: 1,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            )
          ],
        ),
        const SizedBox(height: 24),

        GlassCard(
          padding: 24,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text("Общий баланс", style: TextStyle(color: Colors.grey, fontSize: 13, fontWeight: FontWeight.w500)),
              const SizedBox(height: 8),
              Row(
                crossAxisAlignment: CrossAxisAlignment.baseline,
                textBaseline: TextBaseline.alphabetic,
                children: [
                  Text(balance.toStringAsFixed(2), style: TextStyle(fontSize: 40, fontWeight: FontWeight.w800, color: isLight ? Colors.black : Colors.white)),
                  const SizedBox(width: 6),
                  const Text("RUB", style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: Color(0xFF32D74B))),
                ],
              ),
            ],
          ),
        ),
        const SizedBox(height: 16),

        Row(
          children: [
            Expanded(
              child: GlassCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(CupertinoIcons.cube_box_fill, color: Color(0xFF0A84FF), size: 28),
                    const SizedBox(height: 12),
                    Text("$containersCount", style: const TextStyle(fontSize: 26, fontWeight: FontWeight.bold)),
                    const Text("Ботов", style: TextStyle(color: Colors.grey, fontSize: 13)),
                  ],
                ),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: GlassCard(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(CupertinoIcons.bolt_fill, color: Colors.amber, size: 28),
                    const SizedBox(height: 12),
                    Text("$level", style: const TextStyle(fontSize: 26, fontWeight: FontWeight.bold)),
                    Text("Уровень", style: TextStyle(color: Colors.grey, fontSize: 13)),
                    const SizedBox(height: 8),
                    ClipRRect(
                      borderRadius: BorderRadius.circular(4),
                      child: LinearProgressIndicator(value: progress, minHeight: 4, backgroundColor: isLight ? Colors.black12 : Colors.white10, valueColor: const AlwaysStoppedAnimation(Colors.amber)),
                    )
                  ],
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }
}

class UserBotsTab extends StatelessWidget {
  final List containers;
  final ApiService api;
  final VoidCallback onUpdate;
  const UserBotsTab({super.key, required this.containers, required this.api, required this.onUpdate});

  @override
  Widget build(BuildContext context) {
    return ListView.separated(
      padding: const EdgeInsets.all(16),
      itemCount: containers.length,
      separatorBuilder: (_,__) => const SizedBox(height: 12),
      itemBuilder: (ctx, i) {
        final c = containers[i];
        final isRunning = c['status'] == 'running';
        final color = isRunning ? const Color(0xFF32D74B) : const Color(0xFFFF453A);

        return BouncyBtn(
          onTap: () => Navigator.push(context, CupertinoPageRoute(builder: (_) => ContainerDetailScreen(container: c, api: api, onUpdate: onUpdate))),
          child: GlassCard(
            padding: 16,
            child: Row(
              children: [
                Container(
                  width: 50, height: 50,
                  decoration: BoxDecoration(color: color.withOpacity(0.1), borderRadius: BorderRadius.circular(14)),
                  child: Icon(CupertinoIcons.cube_box_fill, color: color),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(c['container_name'], style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                      const SizedBox(height: 4),
                      Text("${c['image_info']['name']}", style: const TextStyle(color: Colors.grey, fontSize: 12)),
                    ],
                  ),
                ),
                const Icon(CupertinoIcons.chevron_forward, color: Colors.grey, size: 18),
              ],
            ),
          ),
        );
      },
    );
  }
}

class SettingsTab extends StatelessWidget {
  final ApiService api;
  final Map profile;
  const SettingsTab({super.key, required this.api, required this.profile});

  void _showFontPicker(BuildContext context) {
    final fonts = ['Inter', 'Roboto', 'Oswald', 'Montserrat', 'Open Sans'];
    showCupertinoModalPopup(
      context: context,
      builder: (ctx) => CupertinoActionSheet(
        title: const Text("Выберите шрифт"),
        actions: [
          ...fonts.map((f) => CupertinoActionSheetAction(
            onPressed: () { appSettings.setFont(f); Navigator.pop(ctx); },
            child: Text(f, style: GoogleFonts.getFont(f)),
          )),
          CupertinoActionSheetAction(
            onPressed: () async {
               Navigator.pop(ctx);
               await appSettings.loadCustomFont();
            },
            child: const Text("Загрузить свой .ttf"),
          )
        ],
        cancelButton: CupertinoActionSheetAction(onPressed: () => Navigator.pop(ctx), child: const Text("Отмена")),
      )
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = appSettings.theme;

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        GlassCard(
          padding: 0,
          child: Column(
            children: [
               ListTile(
                title: const Text("Тема оформления"),
                trailing: CupertinoSlidingSegmentedControl<AppTheme>(
                  groupValue: theme,
                  onValueChanged: (v) { if (v != null) appSettings.setTheme(v); },
                  children: const {
                    AppTheme.dark: Text('Dark'),
                    AppTheme.light: Text('Light'),
                    AppTheme.newYear: Text('🎄'),
                  },
                ),
              ),
              const Divider(height: 1, color: Colors.white10, indent: 16),
              ListTile(
                title: const Text("Шрифт"),
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(appSettings.fontFamily, style: const TextStyle(color: Colors.grey)),
                    const SizedBox(width: 8),
                    const Icon(CupertinoIcons.chevron_forward, size: 16, color: Colors.grey),
                  ],
                ),
                onTap: () => _showFontPicker(context),
              ),
              const Divider(height: 1, color: Colors.white10, indent: 16),
              ListTile(
                leading: const Icon(CupertinoIcons.square_arrow_right, color: Colors.redAccent),
                title: const Text("Выйти"),
                onTap: () {
                  api.logout();
                  Navigator.pushAndRemoveUntil(context, CupertinoPageRoute(builder: (_) => LoginScreen(api: api)), (r) => false);
                },
              ),
            ],
          ),
        )
      ],
    );
  }
}

class AdminTab extends StatelessWidget {
  final ApiService api;
  const AdminTab({super.key, required this.api});
  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(20),
      children: [
        _AdminTile(icon: CupertinoIcons.person_2_fill, title: "Пользователи", color: Colors.orange, onTap: () => _open(context, "Пользователи", api.getAdminUsers)),
        const SizedBox(height: 12),
        _AdminTile(icon: CupertinoIcons.layers_alt_fill, title: "Контейнеры", color: Colors.cyan, onTap: () => _open(context, "Контейнеры", api.getAdminContainers)),
      ],
    );
  }
  void _open(BuildContext context, String title, Future<List> Function() fetch) {
    Navigator.push(context, CupertinoPageRoute(builder: (_) => GenericList(title: title, fetch: fetch)));
  }
}

class _AdminTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final Color color;
  final VoidCallback onTap;
  const _AdminTile({required this.icon, required this.title, required this.color, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return BouncyBtn(
      onTap: onTap,
      child: GlassCard(
        padding: 16,
        child: Row(
          children: [
            Container(padding: const EdgeInsets.all(10), decoration: BoxDecoration(color: color.withOpacity(0.15), borderRadius: BorderRadius.circular(12)), child: Icon(icon, color: color)),
            const SizedBox(width: 16),
            Text(title, style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
            const Spacer(),
            const Icon(CupertinoIcons.forward, color: Colors.grey, size: 18),
          ],
        ),
      ),
    );
  }
}

class GenericList extends StatelessWidget {
  final String title;
  final Future<List> Function() fetch;
  const GenericList({super.key, required this.title, required this.fetch});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      appBar: AppBar(title: Text(title), backgroundColor: Colors.transparent),
      body: FutureBuilder<List>(
        future: fetch(),
        builder: (ctx, snap) {
          if (snap.connectionState == ConnectionState.waiting) return const Center(child: CupertinoActivityIndicator());
          final list = snap.data ?? [];
          return ListView.separated(
            padding: const EdgeInsets.all(16),
            itemCount: list.length,
            separatorBuilder: (_,__) => const SizedBox(height: 10),
            itemBuilder: (ctx, i) {
              final item = list[i];
              return GlassCard(
                padding: 16,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(item['container_name'] ?? item['first_name'] ?? 'Item', style: const TextStyle(fontWeight: FontWeight.bold)),
                    Text("ID: ${item['id'] ?? item['user_id']}", style: const TextStyle(color: Colors.grey, fontSize: 12)),
                  ],
                ),
              );
            },
          );
        },
      ),
    );
  }
}