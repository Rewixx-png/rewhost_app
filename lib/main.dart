import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';
import 'api_service.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final api = ApiService();
  await api.init();
  runApp(RewHostApp(api: api));
}

class RewHostApp extends StatelessWidget {
  final ApiService api;
  const RewHostApp({super.key, required this.api});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'RewHost',
      debugShowCheckedModeBanner: false,
      theme: ThemeData.dark().copyWith(
        scaffoldBackgroundColor: const Color(0xFF0F172A),
        primaryColor: const Color(0xFF3B82F6),
        textTheme: GoogleFonts.interTextTheme(ThemeData.dark().textTheme),
        colorScheme: const ColorScheme.dark(
          primary: Color(0xFF3B82F6),
          surface: Color(0xFF1E293B),
        ),
      ),
      home: api.isLoggedIn ? MainMenu(api: api) : LoginScreen(api: api),
    );
  }
}

// --- LOGIN SCREEN ---
class LoginScreen extends StatefulWidget {
  final ApiService api;
  const LoginScreen({super.key, required this.api});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _ctrl = TextEditingController();
  bool _loading = false;
  String? _error;

  Future<void> _doLogin() async {
    setState(() { _loading = true; _error = null; });
    try {
      await widget.api.login(_ctrl.text.trim());
      if (mounted) Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => MainMenu(api: widget.api))
      );
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      setState(() => _loading = false);
    }
  }

  void _copyError() {
    if (_error != null) {
      Clipboard.setData(ClipboardData(text: _error!));
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Текст ошибки скопирован в буфер обмена')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(32.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.dns, size: 80, color: Color(0xFF3B82F6)),
              const SizedBox(height: 24),
              Text("RewHost Access", style: GoogleFonts.jetBrainsMono(fontSize: 24, fontWeight: FontWeight.bold)),
              const SizedBox(height: 32),
              
              TextField(
                controller: _ctrl,
                decoration: InputDecoration(
                  labelText: "API Token",
                  filled: true,
                  fillColor: const Color(0xFF1E293B),
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                  prefixIcon: const Icon(Icons.key),
                ),
              ),

              // БЛОК ОШИБКИ С КОПИРОВАНИЕМ
              if (_error != null)
                Container(
                  margin: const EdgeInsets.only(top: 24),
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.redAccent.withOpacity(0.1),
                    border: Border.all(color: Colors.redAccent),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text("Ошибка подключения:", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.redAccent)),
                          IconButton(
                            icon: const Icon(Icons.copy, size: 20, color: Colors.redAccent),
                            onPressed: _copyError,
                            tooltip: "Скопировать",
                          )
                        ],
                      ),
                      const SizedBox(height: 4),
                      SelectableText(
                        _error!,
                        style: const TextStyle(color: Colors.white70, fontFamily: 'monospace', fontSize: 12),
                      ),
                    ],
                  ),
                ),

              const SizedBox(height: 24),
              
              SizedBox(
                width: double.infinity,
                height: 50,
                child: ElevatedButton(
                  onPressed: _loading ? null : _doLogin,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF3B82F6),
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12))
                  ),
                  child: _loading 
                    ? const CircularProgressIndicator(color: Colors.white) 
                    : const Text("Войти", style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}

// --- MAIN MENU ---
class MainMenu extends StatefulWidget {
  final ApiService api;
  const MainMenu({super.key, required this.api});

  @override
  State<MainMenu> createState() => _MainMenuState();
}

class _MainMenuState extends State<MainMenu> {
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
        if (mounted) Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => LoginScreen(api: widget.api))
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Scaffold(body: Center(child: CircularProgressIndicator()));

    final profile = _data!['profile'];
    final int role = profile['effective_role'] ?? 0;
    final bool isAdmin = role >= 2;

    return Scaffold(
      appBar: AppBar(
        title: const Text("RewHost"),
        centerTitle: true,
        backgroundColor: Colors.transparent,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () {
              setState(() { _loading = true; });
              _load();
            },
          )
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            // Profile Card
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                gradient: const LinearGradient(colors: [Color(0xFF1E293B), Color(0xFF0F172A)]),
                borderRadius: BorderRadius.circular(20),
                border: Border.all(color: Colors.white10)
              ),
              child: Row(
                children: [
                  CircleAvatar(radius: 25, backgroundColor: Colors.blue, child: Text(profile['first_name'][0])),
                  const SizedBox(width: 15),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(profile['first_name'], style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                      Text("${profile['balance']} RUB", style: const TextStyle(color: Colors.greenAccent, fontFamily: 'monospace')),
                    ],
                  )
                ],
              ),
            ),
            const SizedBox(height: 30),
            
            // Buttons
            _MenuBtn(
              icon: Icons.layers, 
              title: "Мои Юзерботы", 
              color: Colors.blueAccent,
              onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => UserBotsList(containers: _data!['containers'])))
            ),
            
            if (isAdmin)
              _MenuBtn(
                icon: Icons.security, 
                title: "Админ Панель", 
                color: Colors.deepPurpleAccent,
                onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => AdminMenu(api: widget.api)))
              ),

            _MenuBtn(
              icon: Icons.settings, 
              title: "Настройки", 
              color: Colors.grey,
              onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => SettingsScreen(api: widget.api)))
            ),
          ],
        ),
      ),
    );
  }
}

// --- SETTINGS ---
class SettingsScreen extends StatelessWidget {
  final ApiService api;
  const SettingsScreen({super.key, required this.api});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Настройки")),
      body: ListView(
        children: [
          ListTile(
            leading: const Icon(Icons.logout, color: Colors.redAccent),
            title: const Text("Сменить аккаунт (Выход)"),
            onTap: () {
              api.logout();
              Navigator.of(context).pushAndRemoveUntil(
                MaterialPageRoute(builder: (_) => LoginScreen(api: api)), 
                (route) => false
              );
            },
          )
        ],
      ),
    );
  }
}

// --- USER BOTS LIST ---
class UserBotsList extends StatelessWidget {
  final List containers;
  const UserBotsList({super.key, required this.containers});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Мои Юзерботы")),
      body: containers.isEmpty 
        ? const Center(child: Text("Нет активных контейнеров"))
        : ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: containers.length,
            itemBuilder: (ctx, i) {
              final c = containers[i];
              final isRunning = c['status'] == 'running';
              return Card(
                color: const Color(0xFF1E293B),
                child: ListTile(
                  leading: Icon(Icons.dns, color: isRunning ? Colors.green : Colors.red),
                  title: Text(c['container_name']),
                  subtitle: Text("${c['image_info']['name']} • ${c['server_info']['name']}"),
                  trailing: const Icon(Icons.chevron_right),
                ),
              );
            },
        ),
    );
  }
}

// --- ADMIN MENU ---
class AdminMenu extends StatelessWidget {
  final ApiService api;
  const AdminMenu({super.key, required this.api});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Админ Панель")),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            _MenuBtn(icon: Icons.people, title: "Пользователи", color: Colors.orange, onTap: () {
              Navigator.push(context, MaterialPageRoute(builder: (_) => GenericList(
                title: "Пользователи",
                fetch: api.getAdminUsers,
                itemBuilder: (item) => ListTile(
                  title: Text("${item['first_name']} (@${item['username'] ?? 'None'})"),
                  subtitle: Text("ID: ${item['user_id']} | Bal: ${item['balance']}₽"),
                  leading: const Icon(Icons.person, color: Colors.orange),
                ),
              )));
            }),
            _MenuBtn(icon: Icons.storage, title: "Все Контейнеры", color: Colors.cyan, onTap: () {
              Navigator.push(context, MaterialPageRoute(builder: (_) => GenericList(
                title: "Все Контейнеры",
                fetch: api.getAdminContainers,
                itemBuilder: (item) => ListTile(
                  title: Text(item['container_name']),
                  subtitle: Text("User: ${item['user_id']} | Node: ${item['server_id']}"),
                  leading: const Icon(Icons.inbox, color: Colors.cyan),
                ),
              )));
            }),
          ],
        ),
      ),
    );
  }
}

// --- GENERIC LIST LOADER ---
class GenericList extends StatelessWidget {
  final String title;
  final Future<List> Function() fetch;
  final Widget Function(dynamic) itemBuilder;

  const GenericList({super.key, required this.title, required this.fetch, required this.itemBuilder});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: FutureBuilder<List>(
        future: fetch(),
        builder: (ctx, snap) {
          if (snap.connectionState == ConnectionState.waiting) return const Center(child: CircularProgressIndicator());
          if (snap.hasError) return Center(child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Text("Ошибка: ${snap.error}", textAlign: TextAlign.center, style: const TextStyle(color: Colors.red)),
          ));
          final list = snap.data ?? [];
          return ListView.separated(
            itemCount: list.length,
            separatorBuilder: (_,__) => const Divider(color: Colors.white10),
            itemBuilder: (ctx, i) => itemBuilder(list[i]),
          );
        },
      ),
    );
  }
}

class _MenuBtn extends StatelessWidget {
  final IconData icon;
  final String title;
  final Color color;
  final VoidCallback onTap;

  const _MenuBtn({required this.icon, required this.title, required this.color, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Material(
        color: const Color(0xFF1E293B),
        borderRadius: BorderRadius.circular(12),
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(12),
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 20, horizontal: 16),
            child: Row(
              children: [
                Icon(icon, color: color, size: 28),
                const SizedBox(width: 16),
                Text(title, style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                const Spacer(),
                const Icon(Icons.chevron_right, color: Colors.grey),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
