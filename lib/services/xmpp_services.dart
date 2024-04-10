import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

class XMPPService {
  static const MethodChannel _channel = MethodChannel('xmpp_channel');

  static Future<bool> connect(String username, String password, {Function(bool)? callback}) async {
    try {
      await _channel.invokeMethod('login', {
        'username': username,
        'password': password,
      });


      _channel.setMethodCallHandler((MethodCall call) async {
        if (call.method == 'onLog') {
          print('onLog ${call.arguments}');
          callback?.call(call.arguments);
        }
      });

      var isConnect = await _channel.invokeMethod('onLog');
      return isConnect;
    } on PlatformException catch (_) {
      return false;
    }
  }

  static Future<void> getReactive({Function(dynamic)? callback}) async {
    try {
      _channel.setMethodCallHandler((MethodCall call) async {
        if (call.method == 'sendRealTimeMessage') {
          callback?.call(call.arguments);
        }
      });

    } on PlatformException catch (_) {

    }
  }

  static Future<void> disconnect() async {
    await _channel.invokeMethod('disconnect');
  }

  static Future<void> sendMessage(String to, String message) async {
    await _channel.invokeMethod('sendMessage', {
      'to': to,
      'message': message,
    });
  }

  static Future<List<String>> loadMessages() async {
    try {
      return await _channel.invokeMethod('loadMessages');
    } on PlatformException catch (_) {
      return [];
    }
  }

  static void onMessageReceived(Function(String from, String body) callback) {
    _channel.setMethodCallHandler((MethodCall call) async {
      if (call.method == 'onMessageReceived') {
        print('onMessageReceived ${call.arguments}');
        final String from = call.arguments['from'];
        final String body = call.arguments['body'];
        callback(from, body);
      }
      if (call.method == 'onListsMessages') {
        print('onMessageSent ${call.arguments}');
      }
    });
  }
}
