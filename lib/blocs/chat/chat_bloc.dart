import 'dart:async';

import 'package:bloc/bloc.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter/cupertino.dart';
import 'package:meta/meta.dart';

import '../../services/xmpp_services.dart';

part 'chat_state.dart';

class ChatBloc extends Cubit<ChatState> {

  ChatBloc() : super(const ChatState());


  void setTo(String to) {
    emit(state.copyWith(to: to));
  }

  void setMessage(String message) {
    emit(state.copyWith(message: message));
  }


  Future<void> subscriberReceiveMessage() async {
    XMPPService.onMessageReceived((String from, String body) {


      print('from: $from');
      print('body: $body');

      emit(state.copyWith(from: from, body: body));

    });
  }


  void resetLogin() {
    emit(state.copyWith(isLoading: false));
  }



  Future<void> loginChatEvent(String username, String password) async {

      emit(state.copyWith(isLoading: true, isError:  false));
     await XMPPService.connect(username, password, callback: (bool isConnect) {
        emit(state.copyWith(isConnect: isConnect, isError: isConnect ? false : true));

          emit(state.copyWith(isLoading: false));

      });

  }




  Future<void> sendMessage() async {
    try {
      await XMPPService.sendMessage(state.to ,state.message);
    } catch (e) {
      emit(state.copyWith(isLoading: false));
    }
  }
}

