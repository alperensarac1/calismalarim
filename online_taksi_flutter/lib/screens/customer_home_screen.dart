import 'dart:convert';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

import '../core/session_manager.dart';
import '../models/ride_models.dart';
import '../network/api_client.dart';
import '../repositories/ride_repository.dart';
import '../socket/socket_manager.dart';
import 'login_screen.dart';

class CustomerHomeScreen extends StatefulWidget {
  const CustomerHomeScreen({super.key});

  @override
  State<CustomerHomeScreen> createState() => _CustomerHomeScreenState();
}

class _CustomerHomeScreenState extends State<CustomerHomeScreen> {
  final sessionManager = SessionManager();

  late final RideRepository rideRepository;
  late final SocketManager socketManager;

  GoogleMapController? mapController;

  final pickupLatController = TextEditingController();
  final pickupLngController = TextEditingController();
  final pickupAddressController = TextEditingController();

  final dropoffLatController = TextEditingController();
  final dropoffLngController = TextEditingController();
  final dropoffAddressController = TextEditingController();
  final Set<Polyline> polylines = {};

  String driverDistanceText = "Taksi uzaklığı: -";
  bool socketConnected = false;
  bool isCreatingRide = false;

  String rideStatus = "Aktif ride yok";
  String driverLocationText = "Taksi konumu: -";
  String lastEventText = "Son event: -";

  RideResponse? activeRide;

  final Set<Marker> markers = {};

  @override
  void initState() {
    super.initState();

    rideRepository = RideRepository(
      apiClient: ApiClient(sessionManager: sessionManager),
    );

    socketManager = SocketManager(sessionManager: sessionManager);

    socketManager.onConnected = () {
      setState(() {
        socketConnected = true;
        lastEventText = "Socket bağlandı";
      });
    };

    socketManager.onDisconnected = () {
      setState(() {
        socketConnected = false;
        lastEventText = "Socket kapandı";
      });
    };

    socketManager.onError = (error) {
      _showMessage(error);
      setState(() {
        lastEventText = "Socket hata: $error";
      });
    };

    socketManager.onMessage = (message) {
      _handleSocketMessage(message);
    };
  }

  Future<void> _connectSocket() async {
    await socketManager.connect();
  }

  Future<void> _createRide() async {
    final pickupLat = double.tryParse(pickupLatController.text.trim());
    final pickupLng = double.tryParse(pickupLngController.text.trim());
    final dropoffLat = double.tryParse(dropoffLatController.text.trim());
    final dropoffLng = double.tryParse(dropoffLngController.text.trim());

    final pickupAddress = pickupAddressController.text.trim();
    final dropoffAddress = dropoffAddressController.text.trim();

    if (pickupLat == null ||
        pickupLng == null ||
        dropoffLat == null ||
        dropoffLng == null ||
        pickupAddress.isEmpty ||
        dropoffAddress.isEmpty) {
      _showMessage("Tüm pickup/dropoff alanlarını doğru doldur");
      return;
    }

    setState(() => isCreatingRide = true);

    try {
      final ride = await rideRepository.createRide(
        CreateRideRequest(
          pickupLat: pickupLat,
          pickupLng: pickupLng,
          pickupAddress: pickupAddress,
          dropoffLat: dropoffLat,
          dropoffLng: dropoffLng,
          dropoffAddress: dropoffAddress,
        ),
      );

      activeRide = ride;
      rideStatus = ride.status;

      _setPickupDropoffMarkers(ride);

      _showMessage("Taksi çağrısı oluşturuldu");
    } catch (e) {
      _showMessage(e.toString());
    } finally {
      if (mounted) {
        setState(() => isCreatingRide = false);
      }
    }
  }

  void _handleSocketMessage(String message) {
    setState(() {
      lastEventText = "Son event: $message";
    });

    try {
      final json = jsonDecode(message) as Map<String, dynamic>;
      final event = json["event"]?.toString();

      if (event == "RIDE_ACCEPTED" ||
          event == "RIDE_STATUS_CHANGED" ||
          event == "RIDE_CANCELLED") {
        final data = json["data"] as Map<String, dynamic>?;
        final status = data?["status"]?.toString();

        if (status != null) {
          setState(() {
            rideStatus = status;
          });
        }
      }

      if (event == "DRIVER_LOCATION") {
        final data = json["data"] as Map<String, dynamic>?;

        if (data == null) return;

        final lat = (data["lat"] as num).toDouble();
        final lng = (data["lng"] as num).toDouble();

        setState(() {
          driverLocationText = "Taksi konumu: $lat, $lng";
        });

        _setDriverMarker(lat, lng);
      }
    } catch (e) {
      setState(() {
        lastEventText = "Socket parse hata: $e";
      });
    }
  }

  void _setPickupDropoffMarkers(RideResponse ride) {
    markers.removeWhere(
          (m) => m.markerId.value == "pickup" || m.markerId.value == "dropoff",
    );

    markers.add(
      Marker(
        markerId: const MarkerId("pickup"),
        position: LatLng(ride.pickupLat, ride.pickupLng),
        infoWindow: InfoWindow(
          title: "Alınış Noktası",
          snippet: ride.pickupAddress,
        ),
      ),
    );

    markers.add(
      Marker(
        markerId: const MarkerId("dropoff"),
        position: LatLng(ride.dropoffLat, ride.dropoffLng),
        infoWindow: InfoWindow(
          title: "Varış Noktası",
          snippet: ride.dropoffAddress,
        ),
      ),
    );

    _updatePolylines();


    setState(() {});
    _fitMapToMarkers();
  }
  void _updatePolylines() {
    polylines.clear();

    LatLng? pickup;
    LatLng? dropoff;
    LatLng? driver;

    for (final marker in markers) {
      if (marker.markerId.value == "pickup") {
        pickup = marker.position;
      } else if (marker.markerId.value == "dropoff") {
        dropoff = marker.position;
      } else if (marker.markerId.value == "driver") {
        driver = marker.position;
      }
    }

    if (driver != null && pickup != null) {
      polylines.add(
        Polyline(
          polylineId: const PolylineId("driver_to_pickup"),
          points: [driver, pickup],
          width: 5,
        ),
      );
    }

    if (pickup != null && dropoff != null) {
      polylines.add(
        Polyline(
          polylineId: const PolylineId("pickup_to_dropoff"),
          points: [pickup, dropoff],
          width: 5,
        ),
      );
    }
  }
  void _setDriverMarker(double lat, double lng) {
    markers.removeWhere((m) => m.markerId.value == "driver");

    final driverPosition = LatLng(lat, lng);

    markers.add(
      Marker(
        markerId: const MarkerId("driver"),
        position: driverPosition,
        infoWindow: const InfoWindow(
          title: "Taksiniz",
          snippet: "Canlı konum",
        ),
      ),
    );

    _updatePolylines();
    _updateDriverDistance(driverPosition);

    setState(() {});
    _fitMapToMarkers();
  }
  void _updateDriverDistance(LatLng driverPosition) {
    LatLng? pickup;

    for (final marker in markers) {
      if (marker.markerId.value == "pickup") {
        pickup = marker.position;
        break;
      }
    }

    if (pickup == null) {
      driverDistanceText = "Taksi uzaklığı: Pickup yok";
      return;
    }

    final distanceKm = _calculateDistanceKm(
      driverPosition.latitude,
      driverPosition.longitude,
      pickup.latitude,
      pickup.longitude,
    );

    driverDistanceText = "Taksi uzaklığı: ${distanceKm.toStringAsFixed(2)} km";
  }

  double _calculateDistanceKm(
      double lat1,
      double lon1,
      double lat2,
      double lon2,
      ) {
    const earthRadiusKm = 6371.0;

    final dLat = _degreeToRadian(lat2 - lat1);
    final dLon = _degreeToRadian(lon2 - lon1);

    final a =
        sin(dLat / 2) * sin(dLat / 2) +
            cos(_degreeToRadian(lat1)) *
                cos(_degreeToRadian(lat2)) *
                sin(dLon / 2) *
                sin(dLon / 2);

    final c = 2 * atan2(sqrt(a), sqrt(1 - a));

    return earthRadiusKm * c;
  }

  double _degreeToRadian(double degree) {
    return degree * pi / 180;
  }
  void _fitMapToMarkers() {
    if (mapController == null || markers.isEmpty) return;

    if (markers.length == 1) {
      final marker = markers.first;
      mapController!.animateCamera(
        CameraUpdate.newLatLngZoom(marker.position, 15),
      );
      return;
    }

    double minLat = markers.first.position.latitude;
    double maxLat = markers.first.position.latitude;
    double minLng = markers.first.position.longitude;
    double maxLng = markers.first.position.longitude;

    for (final marker in markers) {
      final lat = marker.position.latitude;
      final lng = marker.position.longitude;

      if (lat < minLat) minLat = lat;
      if (lat > maxLat) maxLat = lat;
      if (lng < minLng) minLng = lng;
      if (lng > maxLng) maxLng = lng;
    }

    final bounds = LatLngBounds(
      southwest: LatLng(minLat, minLng),
      northeast: LatLng(maxLat, maxLng),
    );

    mapController!.animateCamera(
      CameraUpdate.newLatLngBounds(bounds, 80),
    );
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
    pickupLatController.dispose();
    pickupLngController.dispose();
    pickupAddressController.dispose();
    dropoffLatController.dispose();
    dropoffLngController.dispose();
    dropoffAddressController.dispose();

    socketManager.disconnect();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    const defaultIstanbul = LatLng(41.0082, 28.9784);

    return Scaffold(
      appBar: AppBar(
        title: const Text("Customer Home"),
        actions: [
          IconButton(
            onPressed: _logout,
            icon: const Icon(Icons.logout),
          ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(socketConnected ? "Socket: Bağlı" : "Socket: Bağlı değil"),
            Text("Ride durumu: $rideStatus"),
            Text(driverLocationText),
            Text(driverDistanceText),
            Text(
              lastEventText,
              maxLines: 3,
              overflow: TextOverflow.ellipsis,
            ),
            const SizedBox(height: 12),

            Row(
              children: [
                Expanded(
                  child: FilledButton(
                    onPressed: _connectSocket,
                    child: const Text("Socket Bağlan"),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OutlinedButton(
                    onPressed: socketManager.sendPing,
                    child: const Text("Ping"),
                  ),
                ),
              ],
            ),

            const SizedBox(height: 16),

            SizedBox(
              height: 300,
              child: GoogleMap(
                initialCameraPosition: const CameraPosition(
                  target: defaultIstanbul,
                  zoom: 11,
                ),
                markers: markers,
                polylines: polylines,
                onMapCreated: (controller) {
                  mapController = controller;
                },
              )
            ),

            const SizedBox(height: 16),

            const Text(
              "Pickup / Dropoff Bilgileri",
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),

            const SizedBox(height: 12),

            _input(pickupLatController, "Pickup Enlem", TextInputType.number),
            _input(pickupLngController, "Pickup Boylam", TextInputType.number),
            _input(pickupAddressController, "Pickup Adres", TextInputType.text),

            const SizedBox(height: 8),

            _input(dropoffLatController, "Dropoff Enlem", TextInputType.number),
            _input(dropoffLngController, "Dropoff Boylam", TextInputType.number),
            _input(dropoffAddressController, "Dropoff Adres", TextInputType.text),

            const SizedBox(height: 12),

            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: isCreatingRide ? null : _createRide,
                child: isCreatingRide
                    ? const CircularProgressIndicator()
                    : const Text("Taksi Çağır"),
              ),
            ),

            const SizedBox(height: 20),

            if (activeRide != null) _activeRideCard(activeRide!),
          ],
        ),
      ),
    );
  }

  Widget _input(
      TextEditingController controller,
      String label,
      TextInputType type,
      ) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: TextField(
        controller: controller,
        keyboardType: type,
        decoration: InputDecoration(
          labelText: label,
          border: const OutlineInputBorder(),
        ),
      ),
    );
  }

  Widget _activeRideCard(RideResponse ride) {
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
            Text("Tahmini Ücret: ${ride.estimatedFare ?? "-"}"),
          ],
        ),
      ),
    );
  }
}
