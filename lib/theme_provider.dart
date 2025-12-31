import 'dart:io';
import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:file_picker/file_picker.dart';

enum AppTheme { dark, light, newYear }

class AppSettings extends ChangeNotifier {
  AppTheme _theme = AppTheme.newYear;
  String _fontFamily = 'Inter';
  bool _useCustomFont = false;

  AppTheme get theme => _theme;
  String get fontFamily => _fontFamily;

  void setTheme(AppTheme theme) {
    _theme = theme;
    notifyListeners();
  }

  void setFont(String font) {
    _fontFamily = font;
    _useCustomFont = false;
    notifyListeners();
  }

  Future<void> loadCustomFont() async {
    try {
      FilePickerResult? result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['ttf', 'otf'],
      );

      if (result != null && result.files.single.path != null) {
        final File file = File(result.files.single.path!);
        final Uint8List fontData = await file.readAsBytes();

        final fontLoader = FontLoader('CustomFont');
        fontLoader.addFont(Future.value(ByteData.view(fontData.buffer)));
        await fontLoader.load();

        _fontFamily = 'CustomFont';
        _useCustomFont = true;
        notifyListeners();
      }
    } catch (e) {
      debugPrint("Error loading font: $e");
    }
  }

  ThemeData getThemeData() {
    Color bg = const Color(0xFF000000);
    Color surface = const Color(0xFF1C1C1E);
    Color primary = const Color(0xFF0A84FF);
    Brightness brightness = Brightness.dark;

    if (_theme == AppTheme.light) {
      bg = const Color(0xFFF2F2F7);
      surface = const Color(0xFFFFFFFF);
      primary = const Color(0xFF007AFF);
      brightness = Brightness.light;
    } else if (_theme == AppTheme.newYear) {
      bg = const Color(0xFF050B14);
      surface = const Color(0xFF1C1C1E);
      primary = const Color(0xFFFFD700);
      brightness = Brightness.dark;
    }

    TextTheme textTheme;
    if (_fontFamily == 'CustomFont') {
      textTheme = ThemeData.dark().textTheme.apply(fontFamily: 'CustomFont');
    } else if (_fontFamily == 'Roboto') {
      textTheme = GoogleFonts.robotoTextTheme();
    } else if (_fontFamily == 'Oswald') {
      textTheme = GoogleFonts.oswaldTextTheme();
    } else if (_fontFamily == 'Montserrat') {
      textTheme = GoogleFonts.montserratTextTheme();
    } else if (_fontFamily == 'Open Sans') {
      textTheme = GoogleFonts.openSansTextTheme();
    } else {
      textTheme = GoogleFonts.interTextTheme();
    }

    if (_theme == AppTheme.light) {
      textTheme = textTheme.apply(bodyColor: Colors.black, displayColor: Colors.black);
    } else {
      textTheme = textTheme.apply(bodyColor: Colors.white, displayColor: Colors.white);
    }

    return ThemeData(
      brightness: brightness,
      scaffoldBackgroundColor: bg,
      primaryColor: primary,
      canvasColor: surface,
      useMaterial3: true,
      textTheme: textTheme,
      colorScheme: ColorScheme.fromSeed(
        seedColor: primary,
        brightness: brightness,
        primary: primary,
        surface: surface,
        background: bg,
        error: const Color(0xFFFF453A),
      ),
    );
  }
}

final appSettings = AppSettings();