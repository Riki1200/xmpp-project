import '../extensions/success_event_ext.dart';

class SuccessResponseEvent {
  SuccessResponseState? type;
  String? from;

  SuccessResponseEvent({
    this.type,
    this.from,
  });

  Map<String, dynamic> toSuccessResponseData() {
    return {
      'type': type,
      'from': from,
    };
  }

  factory SuccessResponseEvent.fromJson(dynamic eventData) {
    return SuccessResponseEvent(
      type: eventData['type'] != null
          ? eventData['type'].toString().toSuccessResponseState()
          : SuccessResponseState.none,
      from: eventData['from'] ?? '',
    );
  }
}
