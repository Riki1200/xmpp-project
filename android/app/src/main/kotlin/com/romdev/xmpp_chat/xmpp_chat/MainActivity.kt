package com.romdev.xmpp_chat.xmpp_chat

import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity() {
    private val channel = "xmpp_channel"
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        AndroidSmackInitializer.initialize(newBase);
    }


    private lateinit var connectionSubscriber: AbstractXMPPConnection
    var listOfMessages = mutableListOf<String>()


    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)



        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            channel
        ).setMethodCallHandler { call, result ->


            scope.launch {


                if (call.method == "login") {


                    val username = call.argument<String>("username")
                    val password = call.argument<String>("password")


                    val isEmpty = username.isNullOrEmpty() || password.isNullOrEmpty()



                    if (isEmpty) {
                        result.error("getXMPPresult", "Username or password is empty", null);

                    }
                    val connection: AbstractXMPPConnection = XMPPTCPConnection(
                        username, password, "jix.im"
                    )
                    try {
                        connectionSubscriber = connection.connect();
                    } catch (e: Exception) {
                        println("I am not connected ${e.message}");
                        result.success(false);
                    }

                    println("I have A connection ${connection.isConnected}");



                    try {

                        connection.login();


                        if (connection.isAuthenticated) {
                            println("I am authenticated ${connection.isAuthenticated}");
                            withContext(Dispatchers.Main) {
                                MethodChannel(
                                    flutterEngine.dartExecutor.binaryMessenger,
                                    channel
                                ).invokeMethod("onLog", connection.isAuthenticated)
                            }

                        }

                        result.success(connection.isConnected);


                    } catch (e: Exception) {

                        println(e.toString());
                        println("I am not authenticated ===");

                        result.success(false);

                    }
                }

                if (call.method == "disconnect") {
                    withContext(Dispatchers.Main) {
                        result.success(connectionSubscriber.isConnected);
                    }
                    connectionSubscriber.disconnect()

                }

                if (call.method == "sendMessage") {


                    val to = call.argument<String>("to")
                    val message = call.argument<String>("message")
                    withContext(Dispatchers.Main) {
                        val messageSanitize = connectionSubscriber.stanzaFactory
                            .buildMessageStanza()
                            .to(to)
                            .setBody(message)
                            .build();

                        connectionSubscriber.sendStanza(messageSanitize);

                        val args = hashMapOf(
                            "from" to to,
                            "body" to message
                        )

                        suspend {
                            withContext(Dispatchers.Main) {
                                MethodChannel(
                                    flutterEngine.dartExecutor.binaryMessenger,
                                    channel
                                ).invokeMethod("onMessageReceived", args)
                            }
                        }
                    }

                }


                if(connectionSubscriber.isConnected && connectionSubscriber.isAuthenticated) {
                    connectionSubscriber.addAsyncStanzaListener({ stanza ->


                        if (stanza is Message) {
                            val args = hashMapOf(
                                "from" to stanza.from.toString(),
                                "body" to stanza.body
                            )

                            scope.launch {
                                withContext(Dispatchers.Main) {
                                    MethodChannel(
                                        flutterEngine.dartExecutor.binaryMessenger,
                                        channel
                                    ).invokeMethod("sendRealTimeMessage", args)
                                }
                            }


                        }


                    }, null)
                }



                if (call.method == "subscribe") {
                    withContext(Dispatchers.Main) {
                        connectionSubscriber.addStanzaListener({ stanza ->

                            if (stanza is Message) {
                                val args = hashMapOf(
                                    "from" to stanza.from.toString(),
                                    "body" to stanza.body
                                )
                                MethodChannel(
                                    flutterEngine.dartExecutor.binaryMessenger,
                                    channel
                                ).invokeMethod("onMessageReceived", args)

                            }
                        }, null)
                    }


                }


            }


        }
    }
}
