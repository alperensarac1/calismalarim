import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:sozluk_flutter/navigation/routes.dart';

import '../entity/session_manager.dart';
import '../view/anasayfa_screen.dart';
import '../view/bugun_screen.dart';
import '../view/entrydetay_screen.dart';
import '../view/entryekle_screen.dart';
import '../view/login_screen.dart';
import '../view/profil_screen.dart';
import '../view/register_screen.dart';
import '../viewmodel/anasayfa_viewmodel.dart';
import '../viewmodel/bugun_viewmodel.dart';
import '../viewmodel/entrydetay_viewmodel.dart';
import '../viewmodel/entryekle_viewmodel.dart';
import '../viewmodel/giris_viewmodel.dart';
import '../viewmodel/kayit_viewmodel.dart';
import '../viewmodel/profil_viewmodel.dart';



class AppRouter {
  static GoRouter build(SessionManager session) {
    return GoRouter(
      initialLocation: Routes.login,

      // Auth redirect (login değilse login'e at)
      redirect: (context, state) async {
        final loggedIn = await session.isLoggedIn();
        final loc = state.matchedLocation;

        final goingToAuth = (loc == Routes.login || loc == Routes.register);

        if (!loggedIn && !goingToAuth) return Routes.login;
        if (loggedIn && goingToAuth) return Routes.homeGundem;

        return null;
      },

      routes: [
        GoRoute(
          path: Routes.login,
          builder: (context, state) {
            return ChangeNotifierProvider(
              create: (_) => GirisViewModel(),
              child: LoginScreen(
                onLoginSuccess: (userId, username) async {
                  await session.saveUserSession(userId: userId, username: username);
                  context.go(Routes.homeGundem); // popUpTo(login) gibi
                },
                onGoRegister: () => context.go(Routes.register),
              ),
            );
          },
        ),

        GoRoute(
          path: Routes.register,
          builder: (context, state) {
            return ChangeNotifierProvider(
              create: (_) => KayitViewModel(),
              child: RegisterScreen(
                onRegisterSuccess: () => context.pop(),
                onGoLogin: () => context.pop(),
              ),
            );
          },
        ),

        GoRoute(
          path: Routes.homeGundem,
          builder: (context, state) {
            return ChangeNotifierProvider(
              create: (_) => AnaSayfaViewModel(),
              child: AnasayfaScreen(
                onNavigateEntryEkle: () => context.push(Routes.entryAdd),
                onNavigateEntryDetay: (id) => context.push(Routes.entryDetailPath(id)),
                onNavigateBugun: () => context.go(Routes.homeBugun),
                onNavigateProfil: () => context.go(Routes.homeProfil),
              ),
            );
          },
        ),

        GoRoute(
          path: Routes.homeBugun,
          builder: (context, state) {
            return ChangeNotifierProvider(
              create: (_) => BugunViewModel(),
              child: BugunScreen(
                onNavigateGundem: () => context.go(Routes.homeGundem),
                onNavigateProfil: () => context.go(Routes.homeProfil),
                onNavigateEntryDetay: (id) => context.push(Routes.entryDetailPath(id)),
              ),
            );
          },
        ),

        GoRoute(
          path: Routes.homeProfil,
          builder: (context, state) {
            return ChangeNotifierProvider(
              create: (_) => ProfilViewModel(),
              child: ProfilScreen(
                session: session,
                onNavigateGundem: () => context.go(Routes.homeGundem),
                onNavigateBugun: () => context.go(Routes.homeBugun),
                onNavigateEntryDetay: (id) => context.push(Routes.entryDetailPath(id)),
                onLoggedOut: () async {
                  await session.clearSession();
                  context.go(Routes.login); // stack temiz
                },
              ),
            );
          },
        ),

        GoRoute(
          path: Routes.entryAdd,
          builder: (context, state) {
            return ChangeNotifierProvider(
              create: (_) => EntryEkleViewModel(),
              child: EntryEkleScreen(
                session: session,
                onSaved: () => context.pop(),
                onBack: () => context.pop(),
              ),
            );
          },
        ),

        GoRoute(
          path: Routes.entryDetail,
          builder: (context, state) {
            final idStr = state.pathParameters['id'] ?? '-1';
            final id = int.tryParse(idStr) ?? -1;

            return ChangeNotifierProvider(
              create: (_) => EntryDetayViewModel(),
              child: EntryDetayScreen(
                entryId: id,
                session: session,
                onBack: () => context.pop(),
              ),
            );
          },
        ),
      ],
    );
  }
}
