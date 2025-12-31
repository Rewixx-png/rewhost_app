import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import '../../api_service.dart';
import '../../theme_provider.dart';
import '../widgets/common.dart';
import 'home_screen.dart';

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
      if (mounted) Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => RootScreen(api: widget.api)));
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isNewYear = appSettings.theme == AppTheme.newYear;
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
           Container(decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter, end: Alignment.bottomCenter,
              colors: isNewYear 
                ? [const Color(0xFF0F172A), const Color(0xFF1E1E2C)] 
                : [const Color(0xFF000000), const Color(0xFF111111)]
            )
          )),
          if (isNewYear) const SnowOverlay(),
          Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(32.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Container(
                    padding: const EdgeInsets.all(24),
                    decoration: BoxDecoration(color: Colors.white.withOpacity(0.08), shape: BoxShape.circle),
                    child: Icon(CupertinoIcons.cloud_download, size: 50, color: Theme.of(context).primaryColor),
                  ),
                  const SizedBox(height: 24),
                  Text("RewHost", style: const TextStyle(fontSize: 36, fontWeight: FontWeight.bold)),
                  const SizedBox(height: 48),

                  Container(
                    decoration: BoxDecoration(color: const Color(0xFF1C1C1E), borderRadius: BorderRadius.circular(16)),
                    child: TextField(
                      controller: _ctrl,
                      style: const TextStyle(color: Colors.white),
                      decoration: const InputDecoration(
                        hintText: "API Token",
                        prefixIcon: Icon(CupertinoIcons.lock, color: Colors.grey),
                        border: InputBorder.none,
                        contentPadding: EdgeInsets.all(18),
                        hintStyle: TextStyle(color: Colors.white30),
                      ),
                    ),
                  ),

                  if (_error != null) ...[
                    const SizedBox(height: 16),
                    GestureDetector(
                      onTap: () {
                         Clipboard.setData(ClipboardData(text: _error!));
                         ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Скопировано!')));
                      },
                      child: Container(
                        padding: const EdgeInsets.all(10),
                        decoration: BoxDecoration(color: const Color(0xFFFF453A).withOpacity(0.15), borderRadius: BorderRadius.circular(10)),
                        child: Text(_error!, style: const TextStyle(color: Color(0xFFFF453A), fontSize: 13), textAlign: TextAlign.center),
                      ),
                    )
                  ],

                  const SizedBox(height: 32),
                  SizedBox(
                    width: double.infinity,
                    height: 56,
                    child: CupertinoButton.filled(
                      onPressed: _loading ? null : _doLogin,
                      borderRadius: BorderRadius.circular(16),
                      child: _loading 
                          ? const CupertinoActivityIndicator(color: Colors.white) 
                          : const Text("Войти", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 17)),
                    ),
                  )
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}