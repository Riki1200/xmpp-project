

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../blocs/chat/chat_bloc.dart';




class Login extends StatefulWidget {
  const Login({super.key});

  @override
  State<Login> createState() => _LoginState();
}

class _LoginState extends State<Login> {
  final TextEditingController _usernameController = TextEditingController();
  final TextEditingController _passwordController = TextEditingController();



  void showSnackBar() {

    var snackBar = SnackBar(
        behavior: SnackBarBehavior.floating,
        backgroundColor:   Colors.red,
        showCloseIcon: true,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(10),
        ),

        content: Container(
          height: 80,
          width: 100,
          decoration: BoxDecoration(

            borderRadius: BorderRadius.circular(10),
          ),
          child: const Column(
            mainAxisAlignment: MainAxisAlignment.center,

            children: [
              Text('Error'),
              Text('Username or password is incorrect'),
            ],
          ),
        ));

    ScaffoldMessenger.of(context).clearSnackBars();
    ScaffoldMessenger.of(context).showSnackBar(
        snackBar
    );
  }

  @override
  Widget build(BuildContext context) {

    return BlocConsumer<ChatBloc,ChatState>(builder: (BuildContext context, ChatState state) {
      return Scaffold(
        body: Center(
          child:  Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [

              SizedBox(
                height: 100,
                child: Image.asset(
                  'assets/images/XMPP_logo.png',
                  fit: BoxFit.contain,
                ),
              ),



              Padding(
                padding: const EdgeInsets.all(16.0),
                child: TextField(
                  controller: _usernameController,
                  decoration: const InputDecoration(
                    labelText: 'Username',
                  ),
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: TextField(
                  controller: _passwordController,
                  decoration: const InputDecoration(
                    labelText: 'Password',
                  ),
                ),
              ),


              Container(
                width: 100,


                decoration: BoxDecoration(
                  color: Colors.black,
                  borderRadius: BorderRadius.circular(10),
                ),

                child: GestureDetector(
                  onTap: () {

                    final String username = _usernameController.text;
                    final String password = _passwordController.text;


                    context.read<ChatBloc>().loginChatEvent(username, password);

                  },
                  child: const Padding(padding: EdgeInsets.all(10),
                      child:   Text(
                          'Log in',
                          textAlign: TextAlign.center,

                          style: TextStyle(
                            color: Colors.white,
                            fontSize: 20,
                            fontStyle: FontStyle.italic,
                            fontWeight: FontWeight.bold,


                          )
                      )),

                ),
              ),

            ],
          ),
        ),
      );
    }, listener: (BuildContext context, ChatState state) {

      if(state.isLoading) {
        showDialog(
          context: context,
          builder: (BuildContext context) {
            return const AlertDialog(
              title: Text('Loading'),
              content: LinearProgressIndicator(),
            );
          },
        );
      }


      if(state.isConnect) {
        Navigator.pop(context);
        Navigator.pushReplacementNamed(context, '/chat');

      }

      if(state.isError){
        debugPrint('Error');
        showSnackBar();
      }
    }

    );

  }
}