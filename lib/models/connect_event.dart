import '../extensions/xmpp_connection_ext.dart';

class ConnectionEvent {
  ConnectionEvent({
    this.type,
    this.error,
  });

  XmppConnectionState? type;
  String? error;

  factory ConnectionEvent.fromJson(dynamic json) => ConnectionEvent(
    type: json['type'] != null ? json['type'].toString().toConnectionState() : XmppConnectionState.failed,
    error: json["error"] ?? '',
  );

  Map<String, dynamic> toJson() => {
    "type": type,
    "error": error,
  };
}
