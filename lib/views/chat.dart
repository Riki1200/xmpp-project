import 'package:dash_chat_2/dash_chat_2.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/chat/chat_bloc.dart';
import '../services/xmpp_services.dart';

class Chat extends StatefulWidget {
  const Chat({super.key});

  @override
  State<Chat> createState() => _ChatState();
}

class _ChatState extends State<Chat> {
  List<ChatMessage> messages = List<ChatMessage>.empty(growable: true);


  @override
  void initState() {
    super.initState();



    //call chanel in real time
    XMPPService.getReactive(
      callback: (dynamic message) {
        print('message: $message');
      },
    );

    BlocProvider.of<ChatBloc>(context).stream.listen((event) {
      print('from: ${event.from}');
      print('body: ${event.body}');

      if (event.from == 'Charles') {
        return;
      }

      if (event.body.isEmpty) {
        return;
      }

      setState(() {
        messages.add(
          ChatMessage(
            text: event.body,
            user: ChatUser(
              id: '2',
              firstName: 'Juana',

            ),
            createdAt: DateTime.now(),
          ),
        );
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return BlocBuilder<ChatBloc, ChatState>(
      builder: (context, state) {
        return Scaffold(
          body: DashChat(
            currentUser: ChatUser(
              id: '1',
              firstName: 'Charles',

            ),
            onSend: (ChatMessage message) {
              var bloc = BlocProvider.of<ChatBloc>(context);

              setState(() {
                messages.add(message);
              });

              bloc.setTo('Juanito');
              bloc.setMessage(message.text);

              bloc.sendMessage();

            },
            messages: messages

          ),
        );
      },
    );
  }
}
