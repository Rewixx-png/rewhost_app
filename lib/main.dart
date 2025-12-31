import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart'; // Если используется provider, но здесь мы используем ListenableBuilder
import 'api_service.dart';
import 'theme_provider.dart';
import 'ui/screens/login_screen.dart';
import 'ui/screens/home_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Инициализация API сервиса
  final api = ApiService();
  await api.init();

  // Настройка системного UI (прозрачный статус бар)
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.light,
    systemNavigationBarColor: Color(0xFF000000),
  ));

  // Запуск приложения
  runApp(RewHostApp(api: api));
}

class RewHostApp extends StatelessWidget {
  final ApiService api;

  const RewHostApp({super.key, required this.api});

  @override
  Widget build(BuildContext context) {
    // Слушаем изменения темы через ListenableBuilder
    return ListenableBuilder(
      listenable: appSettings,
      builder: (context, _) {
        return MaterialApp(
          title: 'RewHost',
          debugShowCheckedModeBanner: false,
          
          // Получаем тему из провайдера
          theme: appSettings.getThemeData(),
          
          // Роутинг на основе состояния авторизации
          home: api.isLoggedIn ? RootScreen(api: api) : LoginScreen(api: api),
        );
      }
    );
  }
}