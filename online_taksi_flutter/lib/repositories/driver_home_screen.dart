import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';

import '../core/session_manager.dart';
import '../models/driver_models.dart';
import '../models/ride_models.dart';
import '../network/api_client.dart';
import '../repositories/driver_repository.dart';
import '../screens/login_screen.dart';
import '../socket/socket_manager.dart';

class DriverHomeScreen extends StatefulWidget {
  const DriverHomeScreen({super.key});

  @override
  State<DriverHomeScreen> createState() => _DriverHomeScreenState();
}

class _DriverHomeScreenState extends State<DriverHomeScreen> {
  final sessionManager = SessionManager();

  late final DriverRepository driverRepository;
  late final SocketManager socketManager;

  bool socketConnected = false;
  bool isOnline = false;
  bool isLoadingRides = false;
  bool isSendingLocation = false;

  String locationText = "Konum: -";
  String logText = "Hazır";

  List<AvailableRideItem> availableRides = [];
  RideResponse? activeRide;

  @override
  void initState() {
    super.initState();

    driverRepository = DriverRepository(
      apiClient: ApiClient(sessionManager: sessionManager),
    );

    socketManager = SocketManager(sessionManager: sessionManager);

    socketManager.onConnected = () {
      setState(() {
        socketConnected = true;
        logText = "Socket bağlandı";
      });
    };

    socketManager.onDisconnected = () {
      setState(() {
        socketConnected = false;
        logText = "Socket kapandı";
      });
    };

    socketManager.onError = (error) {
      _showMessage(error);
      setState(() {
        logText = "Socket hata: $error";
      });
    };

    socketManager.onMessage = (message) {
      _handleSocketMessage(message);
    };
  }

  Future<void> _connectSocket() async {
    await socketManager.connect();
  }

  Future<void> _setOnline(bool value) async {
    try {
      final response = await driverRepository.setOnline(value);

      setState(() {
        isOnline = response.isOnline;
        logText = value ? "Online oldun" : "Offline oldun";
      });

      _showMessage(value ? "Online oldun" : "Offline oldun");
    } catch (e) {
      _showMessage(e.toString());
    }
  }

  Future<void> _loadAvailableRides() async {
    setState(() => isLoadingRides = true);

    try {
      final response = await driverRepository.getAvailableRides();

      setState(() {
        availableRides = response.rides;
        logText = "Açık ride listesi güncellendi";
      });
    } catch (e) {
      _showMessage(e.toString());
    } finally {
      if (mounted) {
        setState(() => isLoadingRides = false);
      }
    }
  }

  Future<void> _acceptRide(AvailableRideItem ride) async {
    try {
      final accepted = await driverRepository.acceptRide(ride.id);

      setState(() {
        activeRide = accepted;
        availableRides.removeWhere((item) => item.id == ride.id);
        logText = "Ride kabul edildi. id=${ride.id}";
      });

      _showMessage("Ride kabul edildi");
    } catch (e) {
      _showMessage(e.toString());
    }
  }

  Future<void> _updateStatus(String status, String note) async {
    final ride = activeRide;

    if (ride == null) {
      _showMessage("Aktif ride yok");
      return;
    }

    try {
      final updated = await driverRepository.updateRideStatus(
        rideId: ride.id,
        status: status,
        note: note,
      );

      setState(() {
        activeRide = updated;
        logText = "Status güncellendi: $status";
      });

      _showMessage("Status güncellendi");
    } catch (e) {
      _showMessage(e.toString());
    }
  }

  Future<void> _sendCurrentLocation() async {
    setState(() => isSendingLocation = true);

    try {
      final permissionOk = await _ensureLocationPermission();
      if (!permissionOk) {
        _showMessage("Konum izni gerekli");
        return;
      }

      final position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      await driverRepository.updateLocation(
        lat: position.latitude,
        lng: position.longitude,
      );

      setState(() {
        locationText = "Konum: ${position.latitude}, ${position.longitude}";
        logText = "Konum gönderildi";
      });
    } catch (e) {
      _showMessage(e.toString());
    } finally {
      if (mounted) {
        setState(() => isSendingLocation = false);
      }
    }
  }

  Future<void> _startLocationStream() async {
    final permissionOk = await _ensureLocationPermission();
    if (!permissionOk) {
      _showMessage("Konum izni gerekli");
      return;
    }

    _showMessage("Canlı konum gönderimi başladı");

    Geolocator.getPositionStream(
      locationSettings: const LocationSettings(
        accuracy: LocationAccuracy.high,
        distanceFilter: 10,
      ),
    ).listen((position) async {
      try {
        await driverRepository.updateLocation(
          lat: position.latitude,
          lng: position.longitude,
        );

        if (!mounted) return;

        setState(() {
          locationText = "Konum: ${position.latitude}, ${position.longitude}";
          logText = "Canlı konum gönderildi";
        });
      } catch (e) {
        if (!mounted) return;
        setState(() {
          logText = "Konum gönderme hatası: $e";
        });
      }
    });
  }

  Future<bool> _ensureLocationPermission() async {
    final serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      _showMessage("Konum servisi kapalı");
      return false;
    }

    var permission = await Geolocator.checkPermission();

    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }

    if (permission == LocationPermission.denied ||
        permission == LocationPermission.deniedForever) {
      return false;
    }

    return true;
  }

  void _handleSocketMessage(String message) {
    setState(() {
      logText = message;
    });

    try {
      final json = jsonDecode(message) as Map<String, dynamic>;
      final event = json["event"]?.toString();

      if (event == "NEW_RIDE_REQUEST") {
        final data = json["data"] as Map<String, dynamic>?;

        if (data == null) return;

        final ride = AvailableRideItem.fromJson(data);

        final alreadyExists = availableRides.any((item) => item.id == ride.id);

        if (!alreadyExists) {
          setState(() {
            availableRides.insert(0, ride);
          });
        }

        _showMessage("Yeni ride geldi");
      }
    } catch (e) {
      setState(() {
        logText = "Socket parse hata: $e";
      });
    }
  }

  Future<void> _logout() async {
    socketManager.disconnect();
    await sessionManager.clear();

    if (!mounted) return;

    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(builder: (_) => const LoginScreen()),
          (_) => false,
    );
  }

  void _showMessage(String text) {
    if (!mounted) return;

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(text)),
    );
  }

  @override
  void dispose() {
    socketManager.disconnect();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: _appBar(),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _statusSection(),
            const SizedBox(height: 12),
            _connectionButtons(),
            const SizedBox(height: 16),
            _locationButtons(),
            const SizedBox(height: 20),
            _activeRideCard(),
            const SizedBox(height: 20),
            _availableRidesSection(),
          ],
        ),
      ),
    );
  }

  AppBar _appBar() {
    return AppBar(
      title: const Text("Driver Home"),
      actions: [
        IconButton(
          onPressed: _logout,
          icon: const Icon(Icons.logout),
        ),
      ],
    );
  }

  Widget _statusSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(socketConnected ? "Socket: Bağlı" : "Socket: Bağlı değil"),
        Text(isOnline ? "Durum: Online" : "Durum: Offline"),
        Text(locationText),
        Text(
          "Log: $logText",
          maxLines: 3,
          overflow: TextOverflow.ellipsis,
        ),
      ],
    );
  }

  Widget _connectionButtons() {
    return Column(
      children: [
        SizedBox(
          width: double.infinity,
          child: FilledButton(
            onPressed: _connectSocket,
            child: const Text("Socket Bağlan"),
          ),
        ),
        Row(
          children: [
            Expanded(
              child: FilledButton(
                onPressed: () => _setOnline(true),
                child: const Text("ONLINE OL"),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: OutlinedButton(
                onPressed: () => _setOnline(false),
                child: const Text("OFFLINE OL"),
              ),
            ),
          ],
        ),
        SizedBox(
          width: double.infinity,
          child: OutlinedButton(
            onPressed: isLoadingRides ? null : _loadAvailableRides,
            child: isLoadingRides
                ? const CircularProgressIndicator()
                : const Text("Açık Ride'ları Getir"),
          ),
        ),
      ],
    );
  }

  Widget _locationButtons() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Konum İşlemleri",
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        Row(
          children: [
            Expanded(
              child: FilledButton(
                onPressed: isSendingLocation ? null : _sendCurrentLocation,
                child: isSendingLocation
                    ? const CircularProgressIndicator()
                    : const Text("Konum Gönder"),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              child: OutlinedButton(
                onPressed: _startLocationStream,
                child: const Text("Canlı Başlat"),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _activeRideCard() {
    final ride = activeRide;

    if (ride == null) {
      return const Card(
        child: Padding(
          padding: EdgeInsets.all(16),
          child: Text("Aktif ride yok"),
        ),
      );
    }

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "Aktif Ride",
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            Text("Ride ID: ${ride.id}"),
            Text("Pickup: ${ride.pickupAddress}"),
            Text("Dropoff: ${ride.dropoffAddress}"),
            Text("Durum: ${ride.status}"),
            const SizedBox(height: 12),
            _statusButton("Yoldayım", "DRIVER_ARRIVING", "Şoför müşteriye doğru yola çıktı."),
            _statusButton("Geldim", "DRIVER_ARRIVED", "Şoför alım noktasına ulaştı."),
            _statusButton("Başlat", "RIDE_STARTED", "Müşteri araca bindi."),
            _statusButton("Bitir", "RIDE_COMPLETED", "Yolculuk tamamlandı."),
          ],
        ),
      ),
    );
  }

  Widget _statusButton(String title, String status, String note) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: SizedBox(
        width: double.infinity,
        child: FilledButton(
          onPressed: () => _updateStatus(status, note),
          child: Text(title),
        ),
      ),
    );
  }

  Widget _availableRidesSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Açık Ride Listesi",
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 8),
        if (availableRides.isEmpty)
          const Text("Açık ride yok")
        else
          ...availableRides.map(_rideCard),
      ],
    );
  }

  Widget _rideCard(AvailableRideItem ride) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text("Ride ID: ${ride.id}", style: const TextStyle(fontWeight: FontWeight.bold)),
            Text("Pickup: ${ride.pickupAddress}"),
            Text("Dropoff: ${ride.dropoffAddress}"),
            Text("Tahmini Ücret: ${ride.estimatedFare ?? "-"}"),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: () => _acceptRide(ride),
                child: const Text("Kabul Et"),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
