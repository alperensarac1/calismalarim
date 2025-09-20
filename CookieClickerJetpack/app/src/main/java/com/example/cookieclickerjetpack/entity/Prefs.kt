package com.example.cookieclickerjetpack.entity



import android.content.Context
import com.example.cookieclickerjetpack.model.GameState
import com.example.cookieclickerjetpack.model.PerkStore

import org.json.JSONObject
import kotlin.math.roundToInt

class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("cookie_prefs_compose", Context.MODE_PRIVATE)

    fun loadGame(): GameState {
        val s = sp.getString("game", null) ?: return GameState()
        val j = JSONObject(s)
        return GameState(
            score = j.optDouble("score", 0.0),
            cps = j.optDouble("cps", 0.0),
            baseTap = j.optInt("baseTap", 1),
            extraTap = j.optInt("extraTap", 0),
            prestigeLevel = j.optInt("prestigeLevel", 0)
        )
    }
    fun saveGame(gs: GameState) {
        val j = JSONObject()
        j.put("score", gs.score)
        j.put("cps", gs.cps)
        j.put("baseTap", gs.baseTap)
        j.put("extraTap", gs.extraTap)
        j.put("prestigeLevel", gs.prestigeLevel)
        sp.edit().putString("game", j.toString()).apply()
    }

    fun loadPerks(): PerkStore {
        val s = sp.getString("perks", null) ?: return PerkStore()
        val j = JSONObject(s)
        return PerkStore(
            points = j.optInt("points", 0),
            gprod = j.optInt("gprod", 0),
            crit = j.optInt("crit", 0),
            discount = j.optInt("discount", 0),
            tapTop = j.optInt("tapTop", 0)
        )
    }
    fun savePerks(ps: PerkStore) {
        val j = JSONObject()
        j.put("points", ps.points)
        j.put("gprod", ps.gprod)
        j.put("crit", ps.crit)
        j.put("discount", ps.discount)
        j.put("tapTop", ps.tapTop)
        sp.edit().putString("perks", j.toString()).apply()
    }
}
