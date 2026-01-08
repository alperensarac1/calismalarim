import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

import '../model/game_state.dart';
import '../model/perk_store.dart';

class Prefs {
  static const _gameKey = 'game';
  static const _perkKey = 'perks';

  Future<GameState> loadGame() async {
    final sp = await SharedPreferences.getInstance();
    final s = sp.getString(_gameKey);

    if (s == null) return GameState();

    final Map<String, dynamic> json = jsonDecode(s);
    return GameState.fromJson(json);
  }

  Future<void> saveGame(GameState gs) async {
    final sp = await SharedPreferences.getInstance();
    final jsonString = jsonEncode(gs.toJson());
    await sp.setString(_gameKey, jsonString);
  }

  Future<PerkStore> loadPerks() async {
    final sp = await SharedPreferences.getInstance();
    final s = sp.getString(_perkKey);

    if (s == null) return PerkStore();

    final Map<String, dynamic> json = jsonDecode(s);
    return PerkStore.fromJson(json);
  }

  /// PerkStore kaydet
  Future<void> savePerks(PerkStore ps) async {
    final sp = await SharedPreferences.getInstance();
    final jsonString = jsonEncode(ps.toJson());
    await sp.setString(_perkKey, jsonString);
  }
}
