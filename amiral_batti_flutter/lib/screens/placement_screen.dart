import 'dart:convert';

import 'package:flutter/material.dart';

import '../core/socket_manager.dart';
import '../models/board_cell.dart';
import '../models/cell_state.dart';
import '../models/error_data.dart';
import '../models/game_started_data.dart';
import '../models/ship.dart';
import '../models/ship_orientation.dart';
import 'game_screen.dart';

class PlacementScreen extends StatefulWidget {
  final String roomCode;
  final String playerId;
  final String playerName;

  const PlacementScreen({
    super.key,
    required this.roomCode,
    required this.playerId,
    required this.playerName,
  });

  @override
  State<PlacementScreen> createState() => _PlacementScreenState();
}

class _PlacementScreenState extends State<PlacementScreen> implements SocketEventListener {
  final int boardSize = 10;

  late List<BoardCell> boardCells;
  late List<Ship> shipsToPlace;

  int currentShipIndex = 0;
  ShipOrientation orientation = ShipOrientation.horizontal;

  String statusText = "Durum: Gemileri yerleştir";
  bool readyEnabled = false;

  @override
  void initState() {
    super.initState();

    shipsToPlace = [
      Ship(size: 4, placed: false),
      Ship(size: 3, placed: false),
      Ship(size: 3, placed: false),
      Ship(size: 2, placed: false),
      Ship(size: 2, placed: false),
      Ship(size: 1, placed: false),
      Ship(size: 1, placed: false),
    ];

    createBoard();
    SocketManager.instance.setListener(this);
  }

  @override
  void dispose() {
    SocketManager.instance.clearListener(this);
    super.dispose();
  }

  void createBoard() {
    boardCells = List.generate(
      boardSize * boardSize,
          (index) {
        final row = index ~/ boardSize;
        final col = index % boardSize;
        return BoardCell(row: row, col: col, state: CellState.empty);
      },
    );
  }

  void resetBoard() {
    createBoard();

    shipsToPlace = shipsToPlace
        .map((e) => e.copyWith(placed: false))
        .toList();

    currentShipIndex = 0;
    orientation = ShipOrientation.horizontal;
    readyEnabled = false;
    statusText = "Durum: Gemileri yerleştir";

    setState(() {});
  }

  bool canPlaceShip(int startRow, int startCol, int shipSize, ShipOrientation orientation) {
    final targetCells = <Map<String, int>>[];

    for (int i = 0; i < shipSize; i++) {
      final row = orientation == ShipOrientation.vertical ? startRow + i : startRow;
      final col = orientation == ShipOrientation.horizontal ? startCol + i : startCol;

      if (row >= boardSize || col >= boardSize) {
        return false;
      }

      targetCells.add({"row": row, "col": col});
    }

    for (final target in targetCells) {
      final row = target["row"]!;
      final col = target["col"]!;

      for (int r = row - 1; r <= row + 1; r++) {
        for (int c = col - 1; c <= col + 1; c++) {
          if (r < 0 || r >= boardSize || c < 0 || c >= boardSize) continue;

          final index = r * boardSize + c;
          if (boardCells[index].state == CellState.ship) {
            return false;
          }
        }
      }
    }

    return true;
  }

  void onCellTap(int row, int col) {
    if (currentShipIndex >= shipsToPlace.length) return;

    final ship = shipsToPlace[currentShipIndex];

    if (!canPlaceShip(row, col, ship.size, orientation)) {
      setState(() {
        statusText = "Durum: Gemi burada konumlanamaz";
      });
      return;
    }

    for (int i = 0; i < ship.size; i++) {
      final targetRow = orientation == ShipOrientation.vertical ? row + i : row;
      final targetCol = orientation == ShipOrientation.horizontal ? col + i : col;
      final index = targetRow * boardSize + targetCol;

      boardCells[index] = boardCells[index].copyWith(state: CellState.ship);
    }

    shipsToPlace[currentShipIndex] = ship.copyWith(placed: true);
    currentShipIndex++;

    if (currentShipIndex >= shipsToPlace.length) {
      readyEnabled = true;
      statusText = "Durum: Tüm gemiler yerleştirildi. Hazırım butonuna bas.";
    } else {
      statusText = "Durum: Gemileri yerleştir";
    }

    setState(() {});
  }

  List<List<int>> buildBoardMatrix() {
    final matrix = List.generate(
      boardSize,
          (_) => List.generate(boardSize, (_) => 0),
    );

    for (final cell in boardCells) {
      matrix[cell.row][cell.col] = cell.state == CellState.ship ? 1 : 0;
    }

    return matrix;
  }

  void sendBoardToServer() {
    SocketManager.instance.sendMap({
      "type": "SET_BOARD",
      "data": {
        "roomCode": widget.roomCode,
        "playerId": widget.playerId,
        "board": buildBoardMatrix(),
      }
    });

    setState(() {
      statusText = "Durum: Tahta gönderildi, rakip bekleniyor...";
      readyEnabled = false;
    });
  }

  String get currentShipText {
    if (currentShipIndex < shipsToPlace.length) {
      return "Seçili gemi: ${shipsToPlace[currentShipIndex].size} hücrelik gemi";
    }
    return "Seçili gemi: Tüm gemiler yerleştirildi";
  }

  String get orientationText {
    return orientation == ShipOrientation.horizontal ? "Yön: Yatay" : "Yön: Dikey";
  }

  get BoardSetData => null;

  void navigateToGame(String firstTurnPlayerId) {
    final ownBoardJson = jsonEncode(buildBoardMatrix());

    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => GameScreen(
          roomCode: widget.roomCode,
          playerId: widget.playerId,
          playerName: widget.playerName,
          firstTurnPlayerId: firstTurnPlayerId,
          ownBoardJson: ownBoardJson,
        ),
      ),
    );
  }

  @override
  void onConnected() {
    if (!mounted) return;
    setState(() {
      statusText = "Durum: Bağlantı hazır";
    });
  }

  @override
  void onDisconnected() {
    if (!mounted) return;
    setState(() {
      statusText = "Durum: Bağlantı kesildi";
    });
  }

  @override
  void onError(String errorMessage) {
    if (!mounted) return;
    setState(() {
      statusText = "Hata: $errorMessage";
    });
  }

  @override
  void onMessage(String message) {
    if (!mounted) return;

    final map = jsonDecode(message) as Map<String, dynamic>;
    final type = map["type"] as String? ?? "";
    final data = (map["data"] as Map?)?.cast<String, dynamic>() ?? {};

    switch (type) {
      case "BOARD_SET":
        final decoded = BoardSetData.fromJson(data);
        setState(() {
          statusText = decoded.message;
        });
        break;

      case "GAME_STARTED":
        final decoded = GameStartedData.fromJson(data);
        navigateToGame(decoded.firstTurnPlayerId);
        break;

      case "ERROR":
        final decoded = ErrorData.fromJson(data);
        setState(() {
          statusText = "Hata: ${decoded.message}";
          readyEnabled = true;
        });
        break;
    }
  }

  Color colorForCell(BoardCell cell) {
    switch (cell.state) {
      case CellState.empty:
        return const Color(0xFFD9EAF7);
      case CellState.ship:
        return const Color(0xFF5B7C99);
      case CellState.hit:
        return Colors.red;
      case CellState.miss:
        return Colors.white;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Placement"),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            const Text(
              "Gemi Yerleştirme",
              style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 14),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text("Oda: ${widget.roomCode}"),
                    ),
                    const SizedBox(height: 6),
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text("Oyuncu: ${widget.playerName}"),
                    ),
                    const SizedBox(height: 6),
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(currentShipText),
                    ),
                    const SizedBox(height: 6),
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(orientationText),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: () {
                  setState(() {
                    orientation = orientation == ShipOrientation.horizontal
                        ? ShipOrientation.vertical
                        : ShipOrientation.horizontal;
                  });
                },
                child: const Text("Yönü Değiştir"),
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: resetBoard,
                child: const Text("Tahtayı Sıfırla"),
              ),
            ),
            const SizedBox(height: 16),
            GridView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: boardCells.length,
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 10,
                crossAxisSpacing: 2,
                mainAxisSpacing: 2,
              ),
              itemBuilder: (context, index) {
                final cell = boardCells[index];
                return GestureDetector(
                  onTap: () => onCellTap(cell.row, cell.col),
                  child: Container(
                    decoration: BoxDecoration(
                      color: colorForCell(cell),
                      borderRadius: BorderRadius.circular(4),
                      border: Border.all(color: Colors.black12),
                    ),
                  ),
                );
              },
            ),
            const SizedBox(height: 16),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Align(
                  alignment: Alignment.centerLeft,
                  child: Text(statusText),
                ),
              ),
            ),
            const SizedBox(height: 12),
            SizedBox(
              width: double.infinity,
              child: FilledButton(
                onPressed: readyEnabled ? sendBoardToServer : null,
                child: const Text("Hazırım"),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
