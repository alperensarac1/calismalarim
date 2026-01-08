import 'dart:async';
import 'dart:math';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../entity/prefs.dart';
import '../model/floating_text.dart';
import '../model/game_state.dart';
import '../model/perk_store.dart';
import '../model/upgrade.dart';


class GameControllerState {
  final GameState game;
  final PerkStore perks;
  final List<Upgrade> upgrades;
  final List<FloatingText> floaters;

  final bool critReady;
  final int critCooldownLeft;

  const GameControllerState({
    this.game = const GameState(),
    this.perks = const PerkStore(),
    this.upgrades = const [],
    this.floaters = const [],
    this.critReady = true,
    this.critCooldownLeft = 0,
  });

  GameControllerState copyWith({
    GameState? game,
    PerkStore? perks,
    List<Upgrade>? upgrades,
    List<FloatingText>? floaters,
    bool? critReady,
    int? critCooldownLeft,
  }) {
    return GameControllerState(
      game: game ?? this.game,
      perks: perks ?? this.perks,
      upgrades: upgrades ?? this.upgrades,
      floaters: floaters ?? this.floaters,
      critReady: critReady ?? this.critReady,
      critCooldownLeft: critCooldownLeft ?? this.critCooldownLeft,
    );
  }
}

final gameControllerProvider =
StateNotifierProvider<GameController, GameControllerState>(
      (ref) => GameController(Prefs()),
);

class GameController extends StateNotifier<GameControllerState> {
  final Prefs _prefs;

  Timer? _loopTimer;
  Timer? _critTimer;

  GameController(this._prefs)
      : super(
    GameControllerState(
      upgrades: const [
        Upgrade( id: 1, title: "Otomatik Tıklayıcı", desc: "Saniyede +1", icon: "Bolt", basePrice: 50.0, cpsGain: 1.0),
        Upgrade( id: 2, title: "Hızlı Karıştırıcı", desc: "Tıklama +1", icon: "FastForward", basePrice: 75.0, tapGain: 1),
        Upgrade( id: 3, title: "Minik Fırın", desc: "Saniyede +5", icon: "LocalFireDepartment", basePrice: 250.0, cpsGain: 5.0),
        Upgrade( id: 4, title: "Çikolata Parçaları", desc: "Tıklama +3", icon: "GridView", basePrice: 300.0, tapGain: 3),
        Upgrade( id: 5, title: "Pastane", desc: "Saniyede +25", icon: "Store", basePrice: 1200.0, cpsGain: 25.0),
        Upgrade( id: 6, title: "Fabrika", desc: "Saniyede +120", icon: "Factory", basePrice: 6000.0, cpsGain: 120.0),
        Upgrade( id: 7, title: "Araştırma Lab.", desc: "Tıklama +10", icon: "Science", basePrice: 8000.0, tapGain: 10),
        Upgrade( id: 8, title: "Roket Fırın", desc: "Saniyede +750", icon: "Rocket", basePrice: 42000.0, cpsGain: 750.0),
      ],
    ),
  ) {
    _init();
  }

  Future<void> _init() async {
    final gs = await _prefs.loadGame();
    final ps = await _prefs.loadPerks();
    state = state.copyWith(game: gs, perks: ps);
    _startLoop();
  }

  // ---- Derived (Kotlin ile aynı mantık) ----
  double _totalMultiplier() {
    final prestige = 1.0 + state.game.prestigeLevel * 0.10;
    final gprod = 1.0 + state.perks.gprod * 0.05;
    return prestige * gprod;
  }

  double _discountPct() => min(state.perks.discount * 0.02, 0.50);

  int _passiveCritChance() => state.perks.crit;

  int _tapPower() => state.game.baseTap + state.game.extraTap + state.perks.tapTop;

  Future<void> _persist() async {
    await _prefs.saveGame(state.game);
    await _prefs.savePerks(state.perks);
  }

  void _startLoop() {
    _loopTimer?.cancel();
    _loopTimer = Timer.periodic(const Duration(milliseconds: 100), (_) async {
      final eff = state.game.cps * _totalMultiplier();
      final newScore = state.game.score + eff * 0.1;
      state = state.copyWith(game: state.game.copyWith(score: newScore));
      await _persist();
    });
  }

  // ---- Actions ----
  Future<void> onTapCookie(double x, double y) async {
    int gain = (_tapPower() * _totalMultiplier()).toInt();

    if (_passiveCritChance() > 0 && Random().nextInt(100) < _passiveCritChance()) {
      gain *= 3;
      _addFloating("CRIT +$gain", x, y, true);
    } else {
      _addFloating("+$gain", x, y, false);
    }

    state = state.copyWith(game: state.game.copyWith(score: state.game.score + gain));
    await _persist();
  }

  Future<void> doCrit(double x, double y) async {
    if (!state.critReady) return;

    state = state.copyWith(critReady: false, critCooldownLeft: 30);

    final gain = _tapPower() * 10;
    state = state.copyWith(game: state.game.copyWith(score: state.game.score + gain));
    _addFloating("CRIT +$gain", x, y, true);

    _critTimer?.cancel();
    _critTimer = Timer.periodic(const Duration(seconds: 1), (t) async {
      final left = state.critCooldownLeft - 1;
      if (left <= 0) {
        t.cancel();
        state = state.copyWith(critReady: true, critCooldownLeft: 0);
      } else {
        state = state.copyWith(critCooldownLeft: left);
      }
      await _persist();
    });

    await _persist();
  }

  Future<void> buyUpgrade(Upgrade u) async {
    final idx = state.upgrades.indexWhere((it) => it.id == u.id);
    if (idx == -1) return;

    final price = state.upgrades[idx].currentPrice() * (1.0 - _discountPct());
    if (state.game.score < price) return;

    final current = state.upgrades[idx];
    final newU = current.copyWith(level: current.level + 1);
    final newList = [...state.upgrades];
    newList[idx] = newU;

    state = state.copyWith(
      upgrades: newList,
      game: state.game.copyWith(
        score: state.game.score - price,
        cps: state.game.cps + u.cpsGain,
        extraTap: state.game.extraTap + u.tapGain,
      ),
    );

    await _persist();
  }

  Future<void> reset() async {
    state = state.copyWith(
      game: state.game.copyWith(score: 0.0, cps: 0.0, extraTap: 0),
      upgrades: state.upgrades.map((u) => u.copyWith(level: 0)).toList(),
    );
    await _persist();
  }

  Future<void> prestige() async {
    final gain = sqrt(state.game.score / 1000.0).toInt();
    if (gain <= 0) return;

    state = state.copyWith(
      perks: state.perks.copyWith(points: state.perks.points + gain),
      game: state.game.copyWith(
        prestigeLevel: state.game.prestigeLevel + gain,
        score: 0.0,
        cps: 0.0,
        extraTap: 0,
      ),
      upgrades: state.upgrades.map((u) => u.copyWith(level: 0)).toList(),
    );

    await _persist();
  }

  Future<void> buyPerk(String key, int cost, {int? maxLevel}) async {
    if (state.perks.points < cost) return;

    var cur = state.perks.copyWith(points: state.perks.points - cost);

    switch (key) {
      case "gprod":
        cur = cur.copyWith(gprod: cur.gprod + 1);
        break;
      case "crit":
        cur = cur.copyWith(crit: cur.crit + 1);
        break;
      case "discount":
        final next = cur.discount + 1;
        cur = cur.copyWith(discount: maxLevel != null ? min(next, maxLevel) : next);
        break;
      case "tapTop":
        cur = cur.copyWith(tapTop: cur.tapTop + 1);
        break;
    }

    state = state.copyWith(perks: cur);
    await _persist();
  }

  void _addFloating(String text, double x, double y, bool isCrit) {
    final id = DateTime.now().microsecondsSinceEpoch;
    final f = FloatingText(id: id, text: text, x: x, y: y, isCrit: isCrit);

    state = state.copyWith(floaters: [...state.floaters, f]);

    Future.delayed(const Duration(milliseconds: 700), () {
      // controller dispose olduysa patlamasın diye guard:
      if (!mounted) return;
      state = state.copyWith(
        floaters: state.floaters.where((it) => it.id != id).toList(),
      );
    });
  }

  @override
  void dispose() {
    _loopTimer?.cancel();
    _critTimer?.cancel();
    super.dispose();
  }
}
