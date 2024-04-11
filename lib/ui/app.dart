import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:provider/provider.dart';
import 'package:whixp/whixp.dart';
import 'package:xmpp_chat/ui/chat.dart';
import '../utils/xmpp_connection.dart';
import 'bloc/chat/chat_bloc.dart';




class App extends StatelessWidget {
  final XmppConnection xmppConnection;
  final Whixp whixp;
  const App({super.key, required this.xmppConnection, required this.whixp});

  @override
  Widget build(BuildContext context) {
    return MultiBlocProvider(
        providers: [
          BlocProvider(
            create: (context) => ChatBloc(),
          ),
          Provider<XmppConnection>.value(value: xmppConnection),
          Provider<Whixp>.value(value: whixp),
        ],
        child: MaterialApp(
            title: 'Xmpp Chat',
            theme: ThemeData(
              colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
              useMaterial3: true,
            ),
            debugShowCheckedModeBanner: false,
            initialRoute: '/',
            routes: {
              '/': (context) => const Chat(), // Add this line
            }));
  }
}
