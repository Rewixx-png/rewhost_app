import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class ApiService {
  static const String baseUrl = 'https://rewhost.rewixx.ru/api/v1';
  String? _token;

  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString('api_token');
  }

  bool get isLoggedIn => _token != null && _token!.isNotEmpty;

  Future<void> login(String token) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/user/dashboard'),
        headers: {'X-Web-Access-Token': token},
      );

      if (response.statusCode == 200) {
        final prefs = await SharedPreferences.getInstance();
        await prefs.setString('api_token', token);
        _token = token;
      } else {
        // Пытаемся распарсить ошибку от сервера
        String errorMsg = 'Ошибка ${response.statusCode}';
        try {
          final body = json.decode(utf8.decode(response.bodyBytes));
          errorMsg = body['message'] ?? body['detail'] ?? errorMsg;
        } catch (_) {
          // Если это не JSON (например, HTML ошибка от Nginx)
          errorMsg = 'Сервер вернул HTML или текст: ${response.body.substring(0, min(100, response.body.length))}...';
        }
        throw Exception(errorMsg);
      }
    } catch (e) {
      throw Exception(e.toString().replaceAll('Exception: ', ''));
    }
  }

  Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('api_token');
    _token = null;
  }

  Future<dynamic> _get(String endpoint) async {
    if (_token == null) throw Exception('No token');
    
    final response = await http.get(
      Uri.parse('$baseUrl$endpoint'),
      headers: {
        'Content-Type': 'application/json',
        'X-Web-Access-Token': _token!,
      },
    );

    if (response.statusCode == 200) {
      return json.decode(utf8.decode(response.bodyBytes))['data'];
    } else if (response.statusCode == 401 || response.statusCode == 403) {
      await logout();
      throw Exception('AUTH_EXPIRED');
    } else {
      throw Exception('Error ${response.statusCode}: ${response.body}');
    }
  }

  Future<Map<String, dynamic>> getDashboard() async {
    return await _get('/user/dashboard');
  }

  Future<List<dynamic>> getAdminUsers() async {
    final data = await _get('/admin/users');
    return data['users'];
  }

  Future<List<dynamic>> getAdminContainers() async {
    final data = await _get('/admin/containers');
    return data['containers'];
  }
  
  int min(int a, int b) => a < b ? a : b;
}
