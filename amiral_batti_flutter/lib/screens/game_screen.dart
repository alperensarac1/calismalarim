import 'dart:convert';

import 'package:amiral_batti_flutter/screens/placement_screen.dart';
import 'package:flutter/material.dart';

import '../core/socket_manager.dart';
import '../models/board_cell.dart';
import '../models/cell_state.dart';
import '../models/error_data.dart';
import '../models/fire_result_data.dart';
import '../models/rematch_player_data.dart';
import '../models/rematch_started_data.dart';

class GameScreen extends StatefulWidget {
  final String roomCode;
  final String playerId;
  final String playerName;
  final String firstTurnPlayerId;
  final String ownBoardJson;

  const GameScreen({
    super.key,
    required this.roomCode,
    required this.playerId,
    required this.playerName,
    required this.firstTurnPlayerId,
    required this.ownBoardJson,
  });

  @override
  State<GameScreen> createState() => _GameScreenState();
}

class _GameScreenState extends State<GameScreen> implements SocketEventListener {
  final int boardSize = 10;

  late List<BoardCell> ownBoardCells;
  late List<BoardCell> enemyBoardCells;

  late String currentTurnPlayerId;

  bool isFireRequestPending = false;
  bool isRematchRequested = false;
  bool isGameOverDialogShown = false;

  String turnText = "Sıra bilgisi";
  String statusText = "Durum";

  @override
  void initState() {
    super.initState();

    currentTurnPlayerId = widget.firstTurnPlayerId;
    ownBoardCells = [];
    enemyBoardCells = [];

    buildOwnBoardFromJson();
    buildEnemyBoard();
    updateTurnText();

    statusText = "Oyun başladı";
    SocketManager.instance.setListener(this);
  }

  @override
  void dispose() {
    SocketManager.instance.clearListener(this);
    super.dispose();
  }

  void buildOwnBoardFromJson() {
    ownBoardCells.clear();

    final matrix = (jsonDecode(widget.ownBoardJson) as List)
        .map((e) => (e as List).cast<int>())
        .toList();

    for (int row = 0; row < boardSize; row++) {
      for (int col = 0; col < boardSize; col++) {
        final value = matrix[row][col];
        ownBoardCells.add(
          BoardCell(
            row: row,
            col: col,
            state: value == 1 ? CellState.ship : CellState.empty,
          ),
        );
      }
    }
  }

  void buildEnemyBoard() {
    enemyBoardCells = List.generate(
      boardSize * boardSize,
          (index) {
        final row = index ~/ boardSize;
        final col = index % boardSize;
        return BoardCell(row: row, col: col, state: CellState.empty);
      },
    );
  }

  void updateTurnText() {
    turnText = currentTurnPlayerId == widget.playerId
        ? "Sıra sende"
        : "Rakibin sırası";
  }

  void onEnemyCellTap(int row, int col) {
    if (currentTurnPlayerId != widget.playerId) {
      setState(() {
        statusText = "Sıra sende değil";
      });
      return;
    }

    if (isFireRequestPending) {
      setState(() {
        statusText = "Önce önceki atışın sonucunu bekle";
      });
      return;
    }

    final index = row * boardSize + col;
    final state = enemyBoardCells[index].state;

    if (state == CellState.hit || state == CellState.miss) {
      setState(() {
        statusText = "Bu hücreye zaten ateş ettin";
      });
      return;
    }

    SocketManager.instance.sendMap({
      "type": "FIRE",
      "data": {
        "roomCode": widget.roomCode,
        "playerId": widget.playerId,
        "row": row,
        "col": col,
      }
    });

    setState(() {
      isFireRequestPending = true;
      statusText = "Atış gönderildi...";
    });
  }

  void handleFireResult(FireResultData result) {
    final index = result.row * boardSize + result.col;

    if (index < 0 || index >= boardSize * boardSize) return;

    final shooterIsMe = result.shooterPlayerId == widget.playerId;

    if (shooterIsMe) {
      enemyBoardCells[index] = enemyBoardCells[index].copyWith(
        state: result.hit ? CellState.hit : CellState.miss,
      );
    } else {
      ownBoardCells[index] = ownBoardCells[index].copyWith(
        state: result.hit ? CellState.hit : CellState.miss,
      );
    }

    isFireRequestPending = false;
    statusText = result.message;

    if (result.gameOver) {
      final isWinner = result.winnerPlayerId == widget.playerId;
      turnText = isWinner ? "Oyun bitti: Kazandın" : "Oyun bitti: Kaybettin";
      isRematchRequested = false;

      if (!isGameOverDialogShown) {
        isGameOverDialogShown = true;
        WidgetsBinding.instance.addPostFrameCallback((_) {
          showGameOverDialog(isWinner);
        });
      }
      setState(() {});
      return;
    }

    currentTurnPlayerId = result.nextTurnPlayerId ?? "";
    updateTurnText();
    setState(() {});
  }

  void requestRematch() {
    if (isRematchRequested) {
      setState(() {
        statusText = "Zaten yeniden oyun isteği gönderdin";
      });
      return;
    }

    SocketManager.instance.sendMap({
      "type": "REQUEST_REMATCH",
      "data": {
        "roomCode": widget.roomCode,
        "playerId": widget.playerId,
      }
    });

    setState(() {
      isRematchRequested = true;
      isGameOverDialogShown = false;
      statusText = "Yeniden oyun isteği gönderildi. Rakip bekleniyor...";
    });
  }

  void showGameOverDialog(bool isWinner) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          title: Text(isWinner ? "Tebrikler" : "Oyun Bitti"),
          content: Text(
            isWinner
                ? "Rakibin tüm gemilerini batırdın.\n\nYeniden oynamak ister misin?"
                : "Tüm gemilerin batırıldı.\n\nYeniden oynamak ister misin?",
          ),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).pop();
                requestRematch();
              },
              child: const Text("Yeniden Oyna"),
            ),
            TextButton(
              onPressed: () {
                Navigator.of(context).popUntil((route) => route.isFirst);
              },
              child: const Text("Lobiye Dön"),
            ),
          ],
        );
      },
    );
  }

  void showPlayerLeftDialog() {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) {
        return AlertDialog(
          title: const Text("Rakip Ayrıldı"),
          content: const Text("Rakip oyundan çıktı. Lobiye dönmek ister misin?"),
          actions: [
            TextButton(
              onPressed: () {
                Navigator.of(context).popUntil((route) => route.isFirst);
              },
              child: const Text("Lobiye Dön"),
            ),
          ],
        );
      },
    );
  }

  void navigateToPlacementForRematch() {
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(
        builder: (_) => PlacementScreen(
          roomCode: widget.roomCode,
          playerId: widget.playerId,
          playerName: widget.playerName,
        ),
      ),
    );
  }

  @override
  void onConnected() {
    if (!mounted) return;
    setState(() {
      statusText = "Bağlantı aktif";
    });
  }

  @override
  void onDisconnected() {
    if (!mounted) return;
    setState(() {
      statusText = "Bağlantı kesildi";
    });
  }

  @override
  void onError(String errorMessage) {
    if (!mounted) return;
    setState(() {
      isFireRequestPending = false;
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
      case "FIRE_RESULT":
        handleFireResult(FireResultData.fromJson(data));
        break;

      case "REMATCH_STATUS":
        final decoded = RematchStatusData.fromJson(data);
        setState(() {
          statusText = "${decoded.message}\n${decoded.players.map((e) => "${e.name}: ${e.wantsRematch ? "hazır" : "bekleniyor"}").join(" | ")}";
        });
        break;

      case "REMATCH_STARTED":
        final decoded = RematchStartedData.fromJson(data);
        setState(() {
          statusText = decoded.message;
          isGameOverDialogShown = false;
        });
        navigateToPlacementForRematch();
        break;

      case "PLAYER_LEFT":
        setState(() {
          statusText = "Rakip oyundan ayrıldı";
        });
        showPlayerLeftDialog();
        break;

      case "ERROR":
        final decoded = ErrorData.fromJson(data);
        setState(() {
          isFireRequestPending = false;
          statusText = "Hata: ${decoded.message}";
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

  Widget buildBoard(List<BoardCell> cells, bool isEnemy) {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      itemCount: cells.length,
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 10,
        crossAxisSpacing: 2,
        mainAxisSpacing: 2,
      ),
      itemBuilder: (context, index) {
        final cell = cells[index];
        return GestureDetector(
          onTap: isEnemy ? () => onEnemyCellTap(cell.row, cell.col) : null,
          child: Container(
            decoration: BoxDecoration(
              color: colorForCell(cell),
              borderRadius: BorderRadius.circular(4),
              border: Border.all(color: Colors.black12),
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Oyun"),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            const Text(
              "Oyun Ekranı",
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
                      child: Text(turnText),
                    ),
                    const SizedBox(height: 8),
                    Align(
                      alignment: Alignment.centerLeft,
                      child: Text(statusText),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            const Align(
              alignment: Alignment.centerLeft,
              child: Text(
                "Kendi Tahtan",
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
            ),
            const SizedBox(height: 8),
            buildBoard(ownBoardCells, false),
            const SizedBox(height: 20),
            const Align(
              alignment: Alignment.centerLeft,
              child: Text(
                "Rakip Tahtası",
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
              ),
            ),
            const SizedBox(height: 8),
            buildBoard(enemyBoardCells, true),
          ],
        ),
      ),
    );
  }
}
