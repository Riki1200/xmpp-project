import 'package:dash_chat_2/dash_chat_2.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:flutter_broadcasts/flutter_broadcasts.dart';
import 'package:whixp/whixp.dart';

import '../main.dart';
import '../utils/random_guid_id.dart';
import '../utils/xmpp_connection.dart';
import 'app.dart';
import 'bloc/chat/chat_bloc.dart';
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

    receiver.messages.listen((message) {

      debugPrint("Message: $message");

      var body = message.data!['b_body'];

      var from = message.data!['b_from'];

      if(from.contains("juana")) {
        return;
      }


      if(body == null) {
        return;
      }

      if(body.contains("Authenticated") || body.contains('Connected'))  {
        return;
      }

      setState(() {
        messages.add(ChatMessage(
          text: body,
          user: ChatUser(
            id: '2',
            firstName: 'Juanito',
          ),
          createdAt: DateTime.now(),
        ));
      });

    }, onError: (error) {

      debugPrint("Error: $error");

    });


  }


  @override
  void didChangeDependencies() {
    // TODO: implement didChangeDependencies
    super.didChangeDependencies();
    print("didChangeDependencies ${ context.read<XmppConnection>().auth}");

    // context.read<XmppConnection>().readMessage("Juanito@jix.im", RandomGuidId.generate()).then((value) {
    //   print("message received : $value");
    // });







  }

  @override
  Widget build(BuildContext context) {
    return BlocConsumer<ChatBloc, ChatState>(

      builder: (context, state) {
        return Scaffold(
          appBar: AppBar(
            elevation: 10,
            leading: IconButton(
              onPressed: () {

              },
              icon: const Icon(Icons.arrow_back),
            ),
            title: const Row(
              children: [
                Icon(Icons.person),
                Column(
                  children: [
                    Text('Juana', style: TextStyle(fontSize: 12),),
                    Text('Online', style: TextStyle(fontSize: 12),),
                  ],
                ),

              ],
            ),
            actions: [
              IconButton(
                onPressed: () {

                },
                icon: const Icon(Icons.call),
              ),
              IconButton(
                onPressed: () {

                },
                icon: const Icon(Icons.video_call),
              ),

            ],
          ),
          body: DashChat(

            inputOptions: const InputOptions(
              alwaysShowSend: true,

            ),
            messageOptions:  const MessageOptions(
             showOtherUsersAvatar: true,
              showCurrentUserAvatar: true,


            ),
           messageListOptions: MessageListOptions(),


            currentUser: ChatUser(
              id: '1',
              firstName: 'Juana',
            ),
            onSend: (ChatMessage message) {
              var bloc = BlocProvider.of<ChatBloc>(context);

              setState(() {
                messages.add(message);
              });




              context.read<XmppConnection>().sendMessageWithType(
                  "Juanito@jix.im", message.text, RandomGuidId.generate(), DateTime.now().timeZoneOffset.inMilliseconds);
              context.read<Whixp>().sendMessage(JabberID("Juanito@jix.im"), messageBody: "Hola, soy Juana", messageFrom: JabberID("Juana@jix.im"));



            },
            messages: messages.reversed.toList(),

          ),
        );
      }, listener: (BuildContext context, ChatState state) {

    },
    );
  }
}
