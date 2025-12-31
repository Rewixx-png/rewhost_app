import 'dart:ui';
import 'dart:math';
import 'package:flutter/material.dart';
import '../../theme_provider.dart';

class GlassCard extends StatelessWidget {
  final Widget child;
  final double padding;
  final VoidCallback? onTap;
  final Color? color;

  const GlassCard({super.key, required this.child, this.padding = 20, this.onTap, this.color});

  @override
  Widget build(BuildContext context) {
    final theme = appSettings.theme;
    final isLight = theme == AppTheme.light;
    final isNewYear = theme == AppTheme.newYear;

    Color baseColor = color ?? (isLight ? Colors.white.withOpacity(0.7) : const Color(0xFF1C1C1E).withOpacity(0.75));
    Color borderColor = isLight ? Colors.black.withOpacity(0.05) : (isNewYear ? Colors.amber.withOpacity(0.2) : Colors.white.withOpacity(0.08));
    Color shadowColor = isLight ? Colors.black.withOpacity(0.05) : Colors.black.withOpacity(0.3);

    return ClipRRect(
      borderRadius: BorderRadius.circular(24),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 15, sigmaY: 15),
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            onTap: onTap,
            highlightColor: isLight ? Colors.black.withOpacity(0.05) : Colors.white.withOpacity(0.05),
            splashColor: isLight ? Colors.black.withOpacity(0.05) : Colors.white.withOpacity(0.05),
            child: Container(
              padding: EdgeInsets.all(padding),
              decoration: BoxDecoration(
                color: baseColor,
                borderRadius: BorderRadius.circular(24),
                border: Border.all(color: borderColor, width: 0.5),
                boxShadow: [
                  BoxShadow(color: shadowColor, blurRadius: 20, offset: const Offset(0, 10))
                ]
              ),
              child: child,
            ),
          ),
        ),
      ),
    );
  }
}

class BouncyBtn extends StatefulWidget {
  final Widget child;
  final VoidCallback? onTap;
  const BouncyBtn({super.key, required this.child, this.onTap});

  @override
  State<BouncyBtn> createState() => _BouncyBtnState();
}

class _BouncyBtnState extends State<BouncyBtn> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _scale;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(vsync: this, duration: const Duration(milliseconds: 100));
    _scale = Tween<double>(begin: 1.0, end: 0.94).animate(CurvedAnimation(parent: _controller, curve: Curves.easeInOut));
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTapDown: (_) => _controller.forward(),
      onTapUp: (_) {
        _controller.reverse();
        widget.onTap?.call();
      },
      onTapCancel: () => _controller.reverse(),
      child: ScaleTransition(scale: _scale, child: widget.child),
    );
  }
  @override
  void dispose() { _controller.dispose(); super.dispose(); }
}

class SnowOverlay extends StatefulWidget {
  const SnowOverlay({super.key});
  @override
  State<SnowOverlay> createState() => _SnowOverlayState();
}

class _SnowOverlayState extends State<SnowOverlay> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  final List<_Snowflake> _snowflakes = List.generate(40, (_) => _Snowflake());

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(vsync: this, duration: const Duration(seconds: 15))..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return IgnorePointer(
      child: RepaintBoundary(
        child: AnimatedBuilder(
          animation: _controller,
          builder: (context, child) {
            for (var flake in _snowflakes) flake.fall();
            return CustomPaint(painter: _SnowPainter(_snowflakes), size: Size.infinite);
          },
        ),
      ),
    );
  }
}

class _Snowflake {
  double x = Random().nextDouble();
  double y = Random().nextDouble();
  double speed = Random().nextDouble() * 0.001 + 0.0005;
  double size = Random().nextDouble() * 3 + 1;
  void fall() {
    y += speed;
    if (y > 1) {
      y = 0;
      x = Random().nextDouble();
    }
  }
}

class _SnowPainter extends CustomPainter {
  final List<_Snowflake> snowflakes;
  _SnowPainter(this.snowflakes);
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()..color = Colors.white.withOpacity(0.3);
    for (var flake in snowflakes) {
      canvas.drawCircle(Offset(flake.x * size.width, flake.y * size.height), flake.size, paint);
    }
  }
  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}