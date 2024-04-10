import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:path_provider/path_provider.dart';
import 'package:xmpp_chat/views/login.dart';
import 'blocs/chat/chat_bloc.dart';
import 'views/chat.dart';

const platform = MethodChannel('samples.flutter.dev/battery');

void main() async {
  WidgetsBinding widgetsBinding = WidgetsFlutterBinding.ensureInitialized();


  final directory = await getApplicationDocumentsDirectory();


  runApp(MultiBlocProvider(
    providers: [
      BlocProvider(
        create: (context) => ChatBloc(),
      ),
    ],
    child: const MyApp(),
  ));
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      debugShowCheckedModeBanner: false,
      initialRoute: '/',
      routes: {
        '/': (context) => const Login(), // Add this line
        '/chat': (context) => const Chat(),
      }


    );
  }
}
