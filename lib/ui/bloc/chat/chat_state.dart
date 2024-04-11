part of 'chat_bloc.dart';

@immutable
 class ChatState extends Equatable {
  final String to;
  final String from;
  final String body;
  final String message;
  final bool isConnect;
  final bool isError;
  final List<String> messages;
  final bool isLoading;
  const ChatState({
    this.from = '', this.body = '', this.message = '',
    this.to = '',
    this.isConnect = false, this.messages = const [], this.isLoading = false,
    this.isError = false
  });

  ChatState copyWith({
    String? from, String? body, String? message, bool? isConnect, List<String>? messages, bool? isLoading,
    String? to,
    bool? isError
  }) {
    return ChatState(
      from: from ?? this.from,
      body: body ?? this.body,
      message: message ?? this.message,
      isConnect: isConnect ?? this.isConnect,
      messages: messages ?? this.messages,
      isLoading: isLoading ?? this.isLoading,
      to: to ?? this.to,
      isError: isError ?? this.isError
    );
  }




  @override
  List<Object> get props => [
    from, body, message, isConnect, messages, isLoading,
    to,
    isError
  ];
}




