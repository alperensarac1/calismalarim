import 'package:flutter/material.dart';

import '../../core/api_service.dart';
import '../../core/app_colors.dart';
import '../../core/session_manager.dart';
import '../../models/city_model.dart';
import '../../models/district_model.dart';
import '../../models/event_model.dart';

import '../../widgets/app_button.dart';
import '../../widgets/event_card.dart';
import '../auth/login_screen.dart';
import '../event/event_detail_screen.dart';
import '../scanner/ticket_scanner_screen.dart';
import '../ticket/my_tickets_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  List<CityModel> _cities = [];
  List<DistrictModel> _districts = [];
  List<EventModel> _events = [];

  CityModel? _selectedCity;
  DistrictModel? _selectedDistrict;

  String _fullName = '';
  String _role = 'user';

  String _statusMessage = 'Şehirler yükleniyor...';

  bool _isLoadingCities = false;
  bool _isLoadingDistricts = false;
  bool _isLoadingEvents = false;
  bool _isStaffOrAdmin = false;

  @override
  void initState() {
    super.initState();
    _loadSessionInfo();
    _loadCities();
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

  Future<void> _loadCities() async {
    setState(() {
      _isLoadingCities = true;
      _statusMessage = 'Şehirler yükleniyor...';
    });

    try {
      final apiToken = await SessionManager.getApiToken();

      final response = await ApiService.getCities(
        apiToken: apiToken,
      );

      if (!mounted) return;

      setState(() {
        _isLoadingCities = false;
      });

      if (!response.success) {
        setState(() {
          _statusMessage = response.message;
        });
        return;
      }

      setState(() {
        _cities = response.data ?? [];

        if (_cities.isEmpty) {
          _statusMessage = 'Aktif şehir bulunamadı.';
        } else {
          _statusMessage = 'Şehir seçiniz.';
        }
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isLoadingCities = false;
        _statusMessage = e.toString();
      });
    }
  }

  Future<void> _loadDistricts(int cityId) async {
    setState(() {
      _isLoadingDistricts = true;
      _statusMessage = 'İlçeler yükleniyor...';
      _districts = [];
      _selectedDistrict = null;
      _events = [];
    });

    try {
      final apiToken = await SessionManager.getApiToken();

      final response = await ApiService.getDistrictsByCity(
        apiToken: apiToken,
        cityId: cityId,
      );

      if (!mounted) return;

      setState(() {
        _isLoadingDistricts = false;
      });

      if (!response.success) {
        setState(() {
          _statusMessage = response.message;
        });
        return;
      }

      setState(() {
        _districts = response.data ?? [];

        if (_districts.isEmpty) {
          _statusMessage = 'Bu şehir için aktif ilçe bulunamadı.';
        } else {
          _statusMessage = 'İlçe seçip etkinlikleri listeleyebilirsin.';
        }
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isLoadingDistricts = false;
        _statusMessage = e.toString();
      });
    }
  }

  Future<void> _loadEvents() async {
    if (_selectedCity == null) {
      _showMessage('Lütfen şehir seçiniz');
      return;
    }

    if (_selectedDistrict == null) {
      _showMessage('Lütfen ilçe seçiniz');
      return;
    }

    setState(() {
      _isLoadingEvents = true;
      _statusMessage = 'Etkinlikler yükleniyor...';
    });

    try {
      final apiToken = await SessionManager.getApiToken();

      final response = await ApiService.getEventsByLocation(
        apiToken: apiToken,
        cityId: _selectedCity!.id,
        districtId: _selectedDistrict!.id,
      );

      if (!mounted) return;

      setState(() {
        _isLoadingEvents = false;
      });

      if (!response.success) {
        setState(() {
          _statusMessage = response.message;
        });
        return;
      }

      setState(() {
        _events = response.data ?? [];

        if (_events.isEmpty) {
          _statusMessage = 'Bu konum için etkinlik bulunamadı.';
        } else {
          _statusMessage = '${_events.length} etkinlik listelendi.';
        }
      });
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _isLoadingEvents = false;
        _statusMessage = e.toString();
      });
    }
  }

  Future<void> _logout() async {
    await SessionManager.logout();

    if (!mounted) return;

    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(
        builder: (_) => const LoginScreen(),
      ),
          (route) => false,
    );
  }

  void _showMessage(String message) {
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }

  void _openMyTickets() {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => const MyTicketsScreen(),
      ),
    );
  }

  void _openScanner() {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => const TicketScannerScreen(),
      ),
    );
  }

  void _openEventDetail(EventModel event) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => EventDetailScreen(
          eventId: event.id,
        ),
      ),
    );
  }

  String get _roleText {
    if (_role == 'admin') {
      return 'Admin hesabı';
    }

    if (_role == 'staff') {
      return 'Görevli hesabı';
    }

    return 'Etkinlikleri keşfet';
  }

  bool get _isAnyLoading {
    return _isLoadingCities || _isLoadingDistricts || _isLoadingEvents;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Etkinlikler'),
        backgroundColor: AppColors.background,
        foregroundColor: AppColors.darkText,
        elevation: 0,
      ),
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () async {
            await _loadCities();
          },
          child: ListView(
            padding: const EdgeInsets.all(14),
            children: [
              _buildHeaderCard(),
              const SizedBox(height: 14),
              _buildFilterCard(),
              const SizedBox(height: 12),
              _buildStatusRow(),
              const SizedBox(height: 12),
              if (_events.isEmpty && !_isAnyLoading) _buildEmptyCard(),
              ..._events.map(
                    (event) => EventCard(
                  event: event,
                  onTap: () => _openEventDetail(event),
                ),
              ),
            ],
          ),
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
          Text(
            'Hoş geldin, ${_fullName.isEmpty ? 'Kullanıcı' : _fullName}',
            style: const TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            _roleText,
            style: const TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
          const SizedBox(height: 14),
          Row(
            children: [
              Expanded(
                child: _smallButton(
                  text: 'Biletlerim',
                  color: AppColors.blue,
                  onTap: _openMyTickets,
                ),
              ),
              const SizedBox(width: 8),
              if (_isStaffOrAdmin) ...[
                Expanded(
                  child: _smallButton(
                    text: 'QR Kontrol',
                    color: AppColors.green,
                    onTap: _openScanner,
                  ),
                ),
                const SizedBox(width: 8),
              ],
              Expanded(
                child: _smallButton(
                  text: 'Çıkış',
                  color: AppColors.red,
                  onTap: _logout,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildFilterCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: _cardDecoration(),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            'Konum Seç',
            style: TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          const SizedBox(height: 6),
          const Text(
            'Önce şehir, sonra ilçe seçerek etkinlikleri listeleyebilirsin.',
            style: TextStyle(
              fontSize: 14,
              color: AppColors.grayText,
            ),
          ),
          const SizedBox(height: 14),
          _buildCityDropdown(),
          const SizedBox(height: 12),
          _buildDistrictDropdown(),
          const SizedBox(height: 16),
          AppButton(
            text: 'Etkinlikleri Listele',
            backgroundColor: AppColors.green,
            isLoading: _isLoadingEvents,
            onPressed: _loadEvents,
          ),
        ],
      ),
    );
  }

  Widget _buildCityDropdown() {
    return DropdownButtonFormField<CityModel>(
      value: _selectedCity,
      isExpanded: true,
      decoration: _dropdownDecoration('Şehir seçiniz'),
      items: _cities.map((city) {
        return DropdownMenuItem<CityModel>(
          value: city,
          child: Text(city.name),
        );
      }).toList(),
      onChanged: _isLoadingCities
          ? null
          : (city) {
        if (city == null) return;

        setState(() {
          _selectedCity = city;
        });

        _loadDistricts(city.id);
      },
    );
  }

  Widget _buildDistrictDropdown() {
    return DropdownButtonFormField<DistrictModel>(
      value: _selectedDistrict,
      isExpanded: true,
      decoration: _dropdownDecoration(
        _selectedCity == null ? 'Önce şehir seçiniz' : 'İlçe seçiniz',
      ),
      items: _districts.map((district) {
        return DropdownMenuItem<DistrictModel>(
          value: district,
          child: Text(district.name),
        );
      }).toList(),
      onChanged: _selectedCity == null || _isLoadingDistricts
          ? null
          : (district) {
        setState(() {
          _selectedDistrict = district;
        });
      },
    );
  }

  Widget _buildStatusRow() {
    return Row(
      children: [
        if (_isAnyLoading) ...[
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
    );
  }

  Widget _buildEmptyCard() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(22),
      margin: const EdgeInsets.only(bottom: 14),
      decoration: _cardDecoration(),
      child: const Column(
        children: [
          Icon(
            Icons.event_busy,
            size: 46,
            color: AppColors.grayText,
          ),
          SizedBox(height: 10),
          Text(
            'Henüz etkinlik listelenmedi',
            style: TextStyle(
              fontSize: 17,
              fontWeight: FontWeight.bold,
              color: AppColors.darkText,
            ),
          ),
          SizedBox(height: 6),
          Text(
            'Şehir ve ilçe seçtikten sonra etkinlikleri listeleyebilirsin.',
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

  Widget _smallButton({
    required String text,
    required Color color,
    required VoidCallback onTap,
  }) {
    return SizedBox(
      height: 42,
      child: ElevatedButton(
        onPressed: onTap,
        style: ElevatedButton.styleFrom(
          backgroundColor: color,
          foregroundColor: Colors.white,
          elevation: 0,
          padding: EdgeInsets.zero,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
        child: Text(
          text,
          style: const TextStyle(
            fontSize: 13,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }

  InputDecoration _dropdownDecoration(String hintText) {
    return InputDecoration(
      hintText: hintText,
      filled: true,
      fillColor: AppColors.inputBackground,
      contentPadding: const EdgeInsets.symmetric(
        horizontal: 14,
        vertical: 14,
      ),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(14),
        borderSide: BorderSide.none,
      ),
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