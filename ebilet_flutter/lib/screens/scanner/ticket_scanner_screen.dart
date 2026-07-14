import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

import '../../core/api_service.dart';
import '../../core/app_colors.dart';
import '../../core/session_manager.dart';
import '../../models/ticket_model.dart';
import '../../widgets/app_button.dart';
import '../../widgets/app_text_field.dart';

class TicketScannerScreen extends StatefulWidget {
  const TicketScannerScreen({super.key});

  @override
  State<TicketScannerScreen> createState() => _TicketScannerScreenState();
}

class _TicketScannerScreenState extends State<TicketScannerScreen> {
  final TextEditingController _manualCodeController = TextEditingController();

  final MobileScannerController _scannerController = MobileScannerController(
    detectionSpeed: DetectionSpeed.noDuplicates,
    facing: CameraFacing.back,
    torchEnabled: false,
  );

  bool _isChecking = false;
  bool _isScannerStarted = false;
  bool _isStaffOrAdmin = false;

  String _fullName = '';
  String _role = 'user';

  String _statusMessage = 'QR kod okutabilir veya manuel bilet kodu girebilirsin.';

  ScannerResultType _resultType = ScannerResultType.neutral;
  String _resultTitle = 'Henüz kontrol yapılmadı';
  String _resultMessage = 'QR okutulduğunda veya manuel kod girildiğinde sonuç burada görünecek.';

  TicketModel? _checkedTicket;

  @override
  void initState() {
    super.initState();
    _loadSessionInfo();
  }

  @override
  void dispose() {
    _manualCodeController.dispose();
    _scannerController.dispose();
    super.dispose();
  }

  Future<void> _loadSessionInfo() async {
    final fullName = await SessionManager.getFullName();
    final role = await SessionManager.getRole();
    final staffOrAdmin = await SessionManager.isStaffOrAdmin();

    if (!mounted) return;

    setState(() {
      _fullName = fullName;
      _role = role;
      _isStaffOrAdmin = staffOrAdmin;
    });
  }

  Future<void> _startScanner() async {
    try {
      await _scannerController.start();

      if (!mounted) return;

      setState(() {
        _isScannerStarted = true;
        _statusMessage = 'Kamera açık. QR kodu okutabilirsin.';
      });
    } catch (e) {
      _showMessage(e.toString());
    }
  }

  Future<void> _stopScanner() async {
    try {
      await _scannerController.stop();

      if (!mounted) return;

      setState(() {
        _isScannerStarted = false;
        _statusMessage = 'Kamera kapatıldı.';
      });
    } catch (e) {
      _showMessage(e.toString());
    }
  }

  Future<void> _toggleScanner() async {
    if (_isScannerStarted) {
      await _stopScanner();
    } else {
      await _startScanner();
    }
  }

  void _onQrDetected(BarcodeCapture capture) {
    if (_isChecking) {
      return;
    }

    final barcodes = capture.barcodes;

    if (barcodes.isEmpty) {
      return;
    }

    final rawCode = barcodes.first.rawValue;

    if (rawCode == null || rawCode.trim().isEmpty) {
      return;
    }

    final cleanCode = rawCode.trim();

    _manualCodeController.text = cleanCode;

    _stopScanner();

    _checkTicket(cleanCode);
  }

  Future<void> _manualCheck() async {
    final code = _manualCodeController.text.trim();

    if (code.isEmpty) {
      _showMessage('Bilet kodu zorunludur.');
      return;
    }

    await _checkTicket(code);
  }

  Future<void> _checkTicket(String code) async {
    if (_isChecking) {
      return;
    }

    setState(() {
      _isChecking = true;
      _statusMessage = 'Bilet kontrol ediliyor...';
      _resultType = ScannerResultType.neutral;
      _resultTitle = 'Kontrol ediliyor...';
      _resultMessage = 'Bilet bilgisi backend üzerinden doğrulanıyor.';
      _checkedTicket = null;
    });

    try {
      final apiToken = await SessionManager.getApiToken();

      final response = await ApiService.checkTicket(
        apiToken: apiToken,
        ticketCode: code,
      );

      if (!mounted) return;

      setState(() {
        _isChecking = false;
        _statusMessage = 'Kontrol tamamlandı.';
      });

      if (!response.success) {
        _showFailedResult(
          title: 'Giriş Reddedildi',
          message: response.message,
        );
        return;
      }

      final ticket = response.data;

      if (ticket == null) {
        _showFailedResult(
          title: 'Bilet Kontrol Edildi',
          message: response.message,
        );
        return;
      }

      final result = ticket.result ?? 'approved';

      if (result == 'approved') {
        _showSuccessResult(
          message: response.message,
          ticket: ticket,
        );
      } else {
        _showFailedResult(
          title: 'Giriş Reddedildi',
          message: response.message,
        );
      }
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isChecking = false;
        _statusMessage = e.toString();
      });

      _showFailedResult(
        title: 'Bağlantı Hatası',
        message: e.toString(),
      );
    }
  }

  void _showSuccessResult({
    required String message,
    required TicketModel ticket,
  }) {
    setState(() {
      _resultType = ScannerResultType.success;
      _resultTitle = 'Giriş Onaylandı';
      _resultMessage = message;
      _checkedTicket = ticket;
    });
  }

  void _showFailedResult({
    required String title,
    required String message,
  }) {
    setState(() {
      _resultType = ScannerResultType.failed;
      _resultTitle = title;
      _resultMessage = message;
      _checkedTicket = null;
    });
  }

  void _showMessage(String message) {
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }

  Color get _resultColor {
    if (_resultType == ScannerResultType.success) {
      return AppColors.green;
    }

    if (_resultType == ScannerResultType.failed) {
      return AppColors.red;
    }

    return AppColors.blue;
  }

  IconData get _resultIcon {
    if (_resultType == ScannerResultType.success) {
      return Icons.check_circle;
    }

    if (_resultType == ScannerResultType.failed) {
      return Icons.cancel;
    }

    return Icons.info;
  }

  String _buildTicketInfo(TicketModel ticket) {
    final ticketId = ticket.resolvedTicketId;
    final ticketCode = ticket.ticketCode ?? '-';
    final status = ticket.ticketStatus ?? ticket.status ?? '-';

    return 'Bilet ID: $ticketId\nBilet Kodu: $ticketCode\nDurum: $status';
  }

  String _buildUserInfo(TicketModel ticket) {
    final fullName = ticket.user?.fullName ?? '-';
    final email = ticket.user?.email ?? '-';
    final phone = ticket.user?.phone ?? '-';

    return 'Kullanıcı: $fullName\nE-posta: $email\nTelefon: $phone';
  }

  String _buildEventInfo(TicketModel ticket) {
    final eventTitle = ticket.event?.title ?? ticket.eventTitle ?? '-';
    final eventDate = ticket.event?.eventDate ?? '-';

    return 'Etkinlik: $eventTitle\nTarih: $eventDate';
  }

  String _buildLocationInfo(TicketModel ticket) {
    final cityName = ticket.location?.cityName ?? ticket.city?.name ?? '-';
    final districtName = ticket.location?.districtName ?? ticket.district?.name ?? '-';
    final venueName = ticket.location?.venueName ?? ticket.venue?.name ?? '-';
    final address = ticket.location?.venueAddress ?? ticket.venue?.address ?? '-';

    return 'Konum: $cityName / $districtName\nSahne: $venueName\nAdres: $address';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('QR Kontrol'),
        backgroundColor: AppColors.background,
        foregroundColor: AppColors.darkText,
        elevation: 0,
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(14),
          children: [
            _buildHeaderCard(),
            const SizedBox(height: 14),
            if (!_isStaffOrAdmin) _buildUnauthorizedCard(),
            if (_isStaffOrAdmin) ...[
              _buildScannerCard(),
              const SizedBox(height: 14),
              _buildManualCard(),
              const SizedBox(height: 14),
              _buildResultCard(),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildHeaderCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: _cardDecoration(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'QR Bilet Kontrol',
            style: TextStyle(
              fontSize: 26,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Görevli: ${_fullName.isEmpty ? '-' : _fullName}',
            style: const TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            'Rol: $_role',
            style: const TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
          const SizedBox(height: 10),
          Row(
            children: [
              if (_isChecking) ...[
                const SizedBox(
                  width: 18,
                  height: 18,
                  child: CircularProgressIndicator(strokeWidth: 2),
                ),
                const SizedBox(width: 8),
              ],
              Expanded(
                child: Text(
                  _statusMessage,
                  style: const TextStyle(
                    fontSize: 14,
                    color: AppColors.grayText,
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildUnauthorizedCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: AppColors.red.withOpacity(0.10),
        borderRadius: BorderRadius.circular(18),
      ),
      child: const Column(
        children: [
          Icon(
            Icons.lock,
            size: 52,
            color: AppColors.red,
          ),
          SizedBox(height: 12),
          Text(
            'Yetkisiz Erişim',
            style: TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.bold,
              color: AppColors.red,
            ),
          ),
          SizedBox(height: 8),
          Text(
            'Bu ekran sadece staff veya admin hesabıyla kullanılabilir.',
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildScannerCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: _cardDecoration(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Kamera ile QR Okut',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          const SizedBox(height: 8),
          const Text(
            'Bilet üzerindeki QR kodu kameraya göster.',
            style: TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
          const SizedBox(height: 14),
          _buildScannerArea(),
          const SizedBox(height: 14),
          AppButton(
            text: _isScannerStarted ? 'Kamerayı Kapat' : 'Kamerayı Başlat',
            backgroundColor: _isScannerStarted ? AppColors.red : AppColors.green,
            isLoading: false,
            onPressed: _toggleScanner,
          ),
        ],
      ),
    );
  }

  Widget _buildScannerArea() {
    return ClipRRect(
      borderRadius: BorderRadius.circular(18),
      child: SizedBox(
        width: double.infinity,
        height: 280,
        child: Stack(
          children: [
            if (_isScannerStarted)
              MobileScanner(
                controller: _scannerController,
                onDetect: _onQrDetected,
              )
            else
              Container(
                color: Colors.black,
                child: const Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        Icons.qr_code_scanner,
                        color: Colors.white,
                        size: 58,
                      ),
                      SizedBox(height: 10),
                      Text(
                        'Kamerayı başlatmak için butona bas',
                        style: TextStyle(
                          color: Colors.white70,
                          fontSize: 14,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            Center(
              child: Container(
                width: 210,
                height: 210,
                decoration: BoxDecoration(
                  border: Border.all(
                    color: Colors.white,
                    width: 3,
                  ),
                  borderRadius: BorderRadius.circular(18),
                ),
              ),
            ),
            Positioned(
              left: 0,
              right: 0,
              bottom: 14,
              child: Text(
                _isScannerStarted
                    ? 'QR kodu çerçevenin içine getir'
                    : 'Kamera kapalı',
                textAlign: TextAlign.center,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildManualCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: _cardDecoration(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Manuel Kod Kontrolü',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          const SizedBox(height: 8),
          const Text(
            'QR okunmazsa bilet kodunu elle girebilirsin.',
            style: TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
          const SizedBox(height: 14),
          AppTextField(
            controller: _manualCodeController,
            hintText: 'Bilet kodu',
            prefixIcon: Icons.confirmation_number_outlined,
            textInputAction: TextInputAction.done,
          ),
          const SizedBox(height: 14),
          AppButton(
            text: 'Kodu Kontrol Et',
            backgroundColor: AppColors.blue,
            isLoading: _isChecking,
            onPressed: _manualCheck,
          ),
        ],
      ),
    );
  }

  Widget _buildResultCard() {
    final ticket = _checkedTicket;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: _resultColor.withOpacity(_resultType == ScannerResultType.neutral ? 0.05 : 0.12),
        borderRadius: BorderRadius.circular(18),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.06),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                _resultIcon,
                color: _resultColor,
                size: 28,
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  _resultTitle,
                  style: TextStyle(
                    fontSize: 21,
                    fontWeight: FontWeight.bold,
                    color: _resultColor,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            _resultMessage,
            style: const TextStyle(
              fontSize: 14,
              color: AppColors.darkText,
              height: 1.35,
            ),
          ),
          if (ticket != null) ...[
            const SizedBox(height: 14),
            const Divider(),
            const SizedBox(height: 10),
            _buildInfoBlock(
              title: 'Bilet Bilgisi',
              text: _buildTicketInfo(ticket),
            ),
            const SizedBox(height: 12),
            _buildInfoBlock(
              title: 'Kullanıcı Bilgisi',
              text: _buildUserInfo(ticket),
            ),
            const SizedBox(height: 12),
            _buildInfoBlock(
              title: 'Etkinlik Bilgisi',
              text: _buildEventInfo(ticket),
            ),
            const SizedBox(height: 12),
            _buildInfoBlock(
              title: 'Konum Bilgisi',
              text: _buildLocationInfo(ticket),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildInfoBlock({
    required String title,
    required String text,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.bold,
            color: AppColors.grayText,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          text,
          style: const TextStyle(
            fontSize: 14,
            color: AppColors.darkText,
            height: 1.35,
          ),
        ),
      ],
    );
  }

  BoxDecoration _cardDecoration() {
    return BoxDecoration(
      color: AppColors.cardBackground,
      borderRadius: BorderRadius.circular(18),
      boxShadow: [
        BoxShadow(
          color: Colors.black.withOpacity(0.08),
          blurRadius: 12,
          offset: const Offset(0, 5),
        ),
      ],
    );
  }
}

enum ScannerResultType {
  neutral,
  success,
  failed,
}