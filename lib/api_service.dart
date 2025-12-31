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
    final response = await http.get(
      Uri.parse('$baseUrl/user/dashboard'),
      headers: {'X-Web-Access-Token': token},
    );

    if (response.statusCode == 200) {
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('api_token', token);
      _token = token;
    } else {
      String errorMsg = 'Ошибка ${response.statusCode}';
      try {
        final body = json.decode(utf8.decode(response.bodyBytes));
        errorMsg = body['message'] ?? body['detail'] ?? errorMsg;
      } catch (_) {}
      throw Exception(errorMsg);
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
      throw Exception('Error ${response.statusCode}');
    }
  }

  Future<dynamic> _post(String endpoint, [Map<String, dynamic>? body]) async {
    if (_token == null) throw Exception('No token');

    final response = await http.post(
      Uri.parse('$baseUrl$endpoint'),
      headers: {
        'Content-Type': 'application/json',
        'X-Web-Access-Token': _token!,
      },
      body: body != null ? json.encode(body) : null,
    );

    if (response.statusCode == 200) {
      return json.decode(utf8.decode(response.bodyBytes));
    } else {
      String msg = 'Error';
      try { msg = json.decode(utf8.decode(response.bodyBytes))['detail']; } catch (_) {}
      throw Exception(msg);
    }
  }

  Future<dynamic> _delete(String endpoint) async {
    if (_token == null) throw Exception('No token');
    final response = await http.delete(
      Uri.parse('$baseUrl$endpoint'),
      headers: {'X-Web-Access-Token': _token!},
    );
    if (response.statusCode != 200) throw Exception('Delete failed');
    return json.decode(utf8.decode(response.bodyBytes));
  }

  Future<Map<String, dynamic>> getDashboard() async => await _get('/user/dashboard');
  Future<List<dynamic>> getAdminUsers() async => (await _get('/admin/users'))['users'];
  Future<List<dynamic>> getAdminContainers() async => (await _get('/admin/containers'))['containers'];
  Future<Map<String, dynamic>> getContainerDetails(int id) async => await _get('/user/container/$id');
  
  Future<void> containerAction(int id, String action) async => await _post('/user/container/$id/action', {'action': action});
  Future<void> renameContainer(int id, String newName) async => await _post('/user/container/$id/rename', {'new_name': newName});
  Future<void> deleteContainer(int id) async => await _delete('/user/container/$id/delete');
  Future<void> reinstallContainer(int id) async => await _post('/user/container/$id/reinstall/v2', {});

  String getAvatarUrl(int userId) => '$baseUrl/public/user_photo/$userId';
}
