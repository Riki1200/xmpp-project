import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_broadcasts/flutter_broadcasts.dart';
import 'package:hive/hive.dart';
import 'package:path_provider/path_provider.dart';
import 'package:whixp/whixp.dart';
import 'package:xmpp_chat/ui/app.dart';
import 'package:xmpp_chat/utils/credentials_xmpp.dart';
import 'package:xmpp_chat/utils/xmpp_connection.dart';


BroadcastReceiver receiver = BroadcastReceiver(
  names: ["org.xrstudio.xmpp.flutter_xmpp.receivemessage"],
);

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  Directory path = await getApplicationSupportDirectory();
  String directoryPath = '${path.path}/whixp';

  Hive.init(directoryPath);

  WidgetsFlutterBinding.ensureInitialized();

  final whixp = Whixp(
    "Juana@jix.im",
    'Abcd1234@',

    useTLS: false,
    port: 5222,
    onBadCertificateCallback: (cert) => true,
    logger: Log(enableError: true, enableWarning: true),
  );

  whixp.connect();
  whixp.addEventHandler('sessionStart', (_) {
    whixp.sendPresence();
    print("Sending message $_");
    print(whixp.credentials);
  });


  XmppConnection xmppConnection = XmppConnection(param);
  await xmppConnection.start((error) {
    if (kDebugMode) {
      debugPrint("Error: $error");
    }
  });
  await xmppConnection.login();

  receiver.start();

  runApp(App(
    xmppConnection: xmppConnection,
    whixp: whixp,
  ));
}
