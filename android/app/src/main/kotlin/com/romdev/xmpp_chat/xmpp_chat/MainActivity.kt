package com.romdev.xmpp_chat.xmpp_chat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.romdev.xmpp_chat.xmpp_chat.flutter_xmpp.Connection.FlutterXmppConnection
import com.romdev.xmpp_chat.xmpp_chat.flutter_xmpp.Connection.FlutterXmppConnectionService
import com.romdev.xmpp_chat.xmpp_chat.flutter_xmpp.Enum.ConnectionState
import com.romdev.xmpp_chat.xmpp_chat.flutter_xmpp.Enum.GroupRole
import com.romdev.xmpp_chat.xmpp_chat.flutter_xmpp.Utils.Constants
import com.romdev.xmpp_chat.xmpp_chat.flutter_xmpp.Utils.Utils
import com.romdev.xmpp_chat.xmpp_chat.flutter_xmpp.managers.MAMManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity(), MethodChannel.MethodCallHandler, FlutterPlugin,
    ActivityAware, EventChannel.StreamHandler {
    val DEBUG = true

    private var id: String? = null
    private var time: String? = null
    private var body: String? = null
    private var to_jid: String? = null
    private var userJid: String? = null
    private var groupName: String? = null
    private var host = ""
    private var customString: String? = null
    private var jidList: List<String>? = null
    private var jid_user = ""
    private var password = ""
    private var event_channel: EventChannel? = null
    private var membersJid: ArrayList<String>? = null
    private var method_channel: MethodChannel? = null
    private var success_channel: EventChannel? = null
    private var error_channel: EventChannel? = null
    private var connection_channel: EventChannel? = null
    private var mBroadcastReceiver: BroadcastReceiver? = null
    private var successBroadcastReceiver: BroadcastReceiver? = null
    private var errorBroadcastReceiver: BroadcastReceiver? = null
    private var connectionBroadcastReceiver: BroadcastReceiver? = null
    private var requireSSLConnection = false
    private var autoDeliveryReceipt = false
    private var automaticReconnection = true
    private var useStreamManagement = true


    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        method_channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, Constants.CHANNEL)
        method_channel!!.setMethodCallHandler(this)
        event_channel = EventChannel(flutterEngine.dartExecutor.binaryMessenger, Constants.CHANNEL_STREAM)
        event_channel!!.setStreamHandler(this)
        success_channel = EventChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            Constants.CHANNEL_SUCCESS_EVENT_STREAM
        )

        success_channel!!.setStreamHandler(object : EventChannel.StreamHandler {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onListen(args: Any, events: EventSink) {
                if (successBroadcastReceiver == null) {
                    Utils.printLog(" adding success listener: ")
                    successBroadcastReceiver = getSuccessBroadCast(events)
                    val filter = IntentFilter()
                    filter.addAction(Constants.SUCCESS_MESSAGE)
                    activity.registerReceiver(successBroadcastReceiver, filter,
                        RECEIVER_NOT_EXPORTED
                    )
                }
            }

            override fun onCancel(o: Any) {
                if (successBroadcastReceiver != null) {
                    Utils.printLog(" cancelling success listener: ")
                    activity.unregisterReceiver(successBroadcastReceiver)
                    successBroadcastReceiver = null
                }
            }
        })

        error_channel =
            EventChannel(flutterEngine.dartExecutor.binaryMessenger, Constants.CHANNEL_ERROR_EVENT_STREAM)

        error_channel!!.setStreamHandler(object : EventChannel.StreamHandler {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onListen(args: Any, errorEvents: EventSink) {
                if (errorBroadcastReceiver == null) {
                    Utils.printLog(" adding error listener: ")
                    errorBroadcastReceiver = getErrorBroadCast(errorEvents)
                    val filter = IntentFilter()
                    filter.addAction(Constants.ERROR_MESSAGE)
                    getActivity().registerReceiver(errorBroadcastReceiver, filter,
                        RECEIVER_NOT_EXPORTED
                    )
                }
            }

            override fun onCancel(o: Any) {
                if (errorBroadcastReceiver != null) {
                    Utils.printLog(" cancelling error listener: ")
                    getActivity().unregisterReceiver(errorBroadcastReceiver)
                    errorBroadcastReceiver = null
                }
            }
        })

        connection_channel = EventChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            Constants.CHANNEL_CONNECTION_EVENT_STREAM
        )

        connection_channel!!.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(args: Any, connectionEvents: EventSink) {
                if (connectionBroadcastReceiver == null) {
                    Utils.printLog(" adding connection listener: ")
                    connectionBroadcastReceiver = getConnectionBroadCast(connectionEvents)
                    val filter = IntentFilter()
                    filter.addAction(Constants.CONNECTION_STATE_MESSAGE)
                    getActivity().registerReceiver(connectionBroadcastReceiver, filter)
                }
            }

            override fun onCancel(o: Any) {
                if (connectionBroadcastReceiver != null) {
                    Utils.printLog(" cancelling connection listener: ")
                    activity.unregisterReceiver(connectionBroadcastReceiver)
                    connectionBroadcastReceiver = null
                }
            }
        })


    }

//    public static void registerWith(Registrar registrar) {
//
//        //method channel
//        final MethodChannel method_channel = new MethodChannel(registrar.messenger(), CHANNEL);
//        method_channel.setMethodCallHandler(new FlutterXmppPlugin(registrar.context()));
//
//        //event channel
//        final EventChannel event_channel = new EventChannel(registrar.messenger(), CHANNEL_STREAM);
//        event_channel.setStreamHandler(new FlutterXmppPlugin(registrar.context()));
//
//    }

    //    public static void registerWith(Registrar registrar) {
    //
    //        //method channel
    //        final MethodChannel method_channel = new MethodChannel(registrar.messenger(), CHANNEL);
    //        method_channel.setMethodCallHandler(new FlutterXmppPlugin(registrar.context()));
    //
    //        //event channel
    //        final EventChannel event_channel = new EventChannel(registrar.messenger(), CHANNEL_STREAM);
    //        event_channel.setStreamHandler(new FlutterXmppPlugin(registrar.context()));
    //
    //    }




    private fun get_message(events: EventSink): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                when (action) {
                    Constants.CONNECTION_MESSAGE -> {
                        val connectionBuild: MutableMap<String, Any> = HashMap()
                        connectionBuild[Constants.TYPE] =
                            Constants.CONNECTION
                        connectionBuild[Constants.STATUS] =
                            Constants.connected
                        Utils.addLogInStorage("Action: sentMessageToFlutter, Content: $connectionBuild")
                        events.success(connectionBuild)
                    }

                    Constants.AUTH_MESSAGE -> {
                        val authBuild: MutableMap<String, Any> = HashMap()
                        authBuild[Constants.TYPE] =
                            Constants.CONNECTION
                        authBuild[Constants.STATUS] =
                            Constants.authenticated
                        Utils.addLogInStorage("Action: sentMessageToFlutter, Content: $authBuild")
                        events.success(authBuild)
                    }

                    Constants.RECEIVE_MESSAGE -> {
                        val from = intent.getStringExtra(Constants.BUNDLE_FROM_JID)
                        val body = intent.getStringExtra(Constants.BUNDLE_MESSAGE_BODY)
                        val msgId = intent.getStringExtra(Constants.BUNDLE_MESSAGE_PARAMS)
                        val type = intent.getStringExtra(Constants.BUNDLE_MESSAGE_TYPE)
                        val customText = intent.getStringExtra(Constants.CUSTOM_TEXT)
                        val metaInfo = intent.getStringExtra(Constants.META_TEXT)
                        val senderJid =
                            if (intent.hasExtra(Constants.BUNDLE_MESSAGE_SENDER_JID)) intent.getStringExtra(
                                Constants.BUNDLE_MESSAGE_SENDER_JID
                            ) else ""
                        val time =
                            if (intent.hasExtra(Constants.time)) intent.getStringExtra(Constants.time) else Constants.ZERO
                        val chatStateType =
                            if (intent.hasExtra(Constants.CHATSTATE_TYPE)) intent.getStringExtra(
                                Constants.CHATSTATE_TYPE
                            ) else Constants.EMPTY
                        val delayTime =
                            if (intent.hasExtra(Constants.DELAY_TIME)) intent.getStringExtra(
                                Constants.DELAY_TIME
                            ) else Constants.ZERO
                        val build: MutableMap<String, Any?> = HashMap()
                        build[Constants.TYPE] =
                            metaInfo
                        build[Constants.ID] = msgId
                        build[Constants.FROM] = from
                        build[Constants.BODY] = body
                        build[Constants.MSG_TYPE] =
                            type
                        build[Constants.SENDER_JID] =
                            senderJid
                        build[Constants.CUSTOM_TEXT] =
                            customText
                        build[Constants.time] = time
                        build[Constants.CHATSTATE_TYPE] =
                            chatStateType
                        build[Constants.DELAY_TIME] =
                            delayTime
                        Utils.addLogInStorage("Action: sentMessageToFlutter, Content: $build")
                        Log.d("TAG", " RECEIVE_MESSAGE-->> $build")
                        events.success(build)
                    }

                    Constants.OUTGOING_MESSAGE -> {
                        val to = intent.getStringExtra(Constants.BUNDLE_TO_JID)
                        val bodyTo = intent.getStringExtra(Constants.BUNDLE_MESSAGE_BODY)
                        val idOutgoing = intent.getStringExtra(Constants.BUNDLE_MESSAGE_PARAMS)
                        val typeTo = intent.getStringExtra(Constants.BUNDLE_MESSAGE_TYPE)
                        val buildTo: MutableMap<String, Any?> = HashMap()
                        buildTo[Constants.TYPE] = Constants.OUTGOING
                        buildTo[Constants.ID] =
                            idOutgoing
                        buildTo[Constants.TO] = to
                        buildTo[Constants.BODY] =
                            bodyTo
                        buildTo[Constants.MSG_TYPE] =
                            typeTo
                        events.success(buildTo)
                    }

                    Constants.PRESENCE_MESSAGE -> {
                        val jid = intent.getStringExtra(Constants.BUNDLE_FROM_JID)
                        val presenceType = intent.getStringExtra(Constants.BUNDLE_PRESENCE_TYPE)
                        val presenceMode = intent.getStringExtra(Constants.BUNDLE_PRESENCE_MODE)
                        val presenceBuild: MutableMap<String, Any?> = HashMap()
                        presenceBuild[Constants.TYPE] =
                            Constants.PRESENCE
                        presenceBuild[Constants.FROM] =
                            jid
                        presenceBuild[Constants.PRESENCE_TYPE] =
                            presenceType
                        presenceBuild[Constants.PRESENCE_MODE] =
                            presenceMode
                        Utils.printLog("presenceBuild: $presenceBuild")
                        events.success(presenceBuild)
                    }
                }
            }
        }
    }

    // Sending a message to one-one chat.
    fun sendMessage(body: String?, toUser: String?, msgId: String?, method: String, time: String?) {
        if (FlutterXmppConnectionService.getState() == ConnectionState.AUTHENTICATED) {
            if (method == Constants.SEND_GROUP_MESSAGE) {
                val intent = Intent(Constants.GROUP_SEND_MESSAGE)
                intent.putExtra(Constants.BUNDLE_MESSAGE_BODY, body)
                intent.putExtra(Constants.BUNDLE_TO, toUser)
                intent.putExtra(Constants.BUNDLE_MESSAGE_PARAMS, msgId)
                intent.putExtra(Constants.BUNDLE_MESSAGE_SENDER_TIME, time)
                activity!!.sendBroadcast(intent)
            } else {
                val intent = Intent(Constants.X_SEND_MESSAGE)
                intent.putExtra(Constants.BUNDLE_MESSAGE_BODY, body)
                intent.putExtra(Constants.BUNDLE_TO, toUser)
                intent.putExtra(Constants.BUNDLE_MESSAGE_PARAMS, msgId)
                intent.putExtra(Constants.BUNDLE_MESSAGE_SENDER_TIME, time)
                activity!!.sendBroadcast(intent)
            }
        }
    }

    fun sendCustomMessage(
        body: String?,
        toUser: String?,
        msgId: String?,
        customText: String?,
        time: String?
    ) {
        FlutterXmppConnection.sendCustomMessage(body, toUser, msgId, customText, true, time)
    }

    fun sendCustomGroupMessage(
        body: String?,
        toUser: String?,
        msgId: String?,
        customText: String?,
        time: String?
    ) {
        FlutterXmppConnection.sendCustomMessage(body, toUser, msgId, customText, false, time)
    }

    private fun getSuccessBroadCast(events: EventSink): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                when (action) {
                    Constants.SUCCESS_MESSAGE -> {
                        val successType = intent.getStringExtra(Constants.BUNDLE_SUCCESS_TYPE)
                        val from = intent.getStringExtra(Constants.FROM)
                        val successBuild: MutableMap<String, Any?> = HashMap()
                        successBuild[Constants.TYPE] =
                            successType
                        successBuild[Constants.FROM] =
                            from
                        Utils.addLogInStorage("Action: sentSuccessMessageToFlutter, Content: $successBuild")
                        events.success(successBuild)
                    }
                }
            }
        }
    }

    private fun getErrorBroadCast(errorEvents: EventSink): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                when (action) {
                    Constants.ERROR_MESSAGE -> {
                        val from = intent.getStringExtra(Constants.FROM)
                        val error = intent.getStringExtra(Constants.BUNDLE_EXCEPTION)
                        val errorType = intent.getStringExtra(Constants.BUNDLE_ERROR_TYPE)
                        val errorBuild: MutableMap<String, Any?> = HashMap()
                        errorBuild[Constants.FROM] = from
                        errorBuild[Constants.EXCEPTION] =
                            error
                        errorBuild[Constants.TYPE] =
                            errorType
                        Utils.addLogInStorage("Action: sentErrorMessageToFlutter, Content: $errorBuild")
                        errorEvents.success(errorBuild)
                    }
                }
            }
        }
    }

    private fun getConnectionBroadCast(connectionEvents: EventSink): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                when (action) {
                    Constants.CONNECTION_STATE_MESSAGE -> {
                        val connectionType = intent.getStringExtra(Constants.BUNDLE_CONNECTION_TYPE)
                        val connectionError =
                            intent.getStringExtra(Constants.BUNDLE_CONNECTION_ERROR)
                        val connectionStateBuild: MutableMap<String, Any?> = HashMap()
                        connectionStateBuild[Constants.TYPE] =
                            connectionType
                        connectionStateBuild[Constants.ERROR] =
                            connectionError
                        Utils.addLogInStorage("Action: sentConnectionMessageToFlutter, Content: $connectionStateBuild")
                        connectionEvents.success(connectionStateBuild)
                    }
                }
            }
        }
    }


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPluginBinding) {
        method_channel = MethodChannel(flutterPluginBinding.binaryMessenger, Constants.CHANNEL)
        method_channel!!.setMethodCallHandler(this)
        event_channel = EventChannel(flutterPluginBinding.binaryMessenger, Constants.CHANNEL_STREAM)
        event_channel!!.setStreamHandler(this)
        success_channel = EventChannel(
            flutterPluginBinding.binaryMessenger,
            Constants.CHANNEL_SUCCESS_EVENT_STREAM
        )
        success_channel!!.setStreamHandler(object : EventChannel.StreamHandler {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onListen(args: Any, events: EventSink) {
                if (successBroadcastReceiver == null) {
                    Utils.printLog(" adding success listener: ")
                    successBroadcastReceiver = getSuccessBroadCast(events)
                    val filter = IntentFilter()
                    filter.addAction(Constants.SUCCESS_MESSAGE)
                    activity!!.registerReceiver(successBroadcastReceiver, filter,
                        RECEIVER_NOT_EXPORTED
                    )
                }
            }

            override fun onCancel(o: Any) {
                if (successBroadcastReceiver != null) {
                    Utils.printLog(" cancelling success listener: ")
                    activity!!.unregisterReceiver(successBroadcastReceiver)
                    successBroadcastReceiver = null
                }
            }
        })
        error_channel =
            EventChannel(flutterPluginBinding.binaryMessenger, Constants.CHANNEL_ERROR_EVENT_STREAM)
        error_channel!!.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(args: Any, errorEvents: EventSink) {
                if (errorBroadcastReceiver == null) {
                    Utils.printLog(" adding error listener: ")
                    errorBroadcastReceiver = getErrorBroadCast(errorEvents)
                    val filter = IntentFilter()
                    filter.addAction(Constants.ERROR_MESSAGE)
                    activity!!.registerReceiver(errorBroadcastReceiver, filter)
                }
            }

            override fun onCancel(o: Any) {
                if (errorBroadcastReceiver != null) {
                    Utils.printLog(" cancelling error listener: ")
                    activity.unregisterReceiver(errorBroadcastReceiver)
                    errorBroadcastReceiver = null
                }
            }
        })
        connection_channel = EventChannel(
            flutterPluginBinding.binaryMessenger,
            Constants.CHANNEL_CONNECTION_EVENT_STREAM
        )
        connection_channel!!.setStreamHandler(object : EventChannel.StreamHandler {
            override fun onListen(args: Any, connectionEvents: EventSink) {
                if (connectionBroadcastReceiver == null) {
                    Utils.printLog(" adding connection listener: ")
                    connectionBroadcastReceiver = getConnectionBroadCast(connectionEvents)
                    val filter = IntentFilter()
                    filter.addAction(Constants.CONNECTION_STATE_MESSAGE)
                    activity!!.registerReceiver(connectionBroadcastReceiver, filter)
                }
            }

            override fun onCancel(o: Any) {
                if (connectionBroadcastReceiver != null) {
                    Utils.printLog(" cancelling connection listener: ")
                    activity!!.unregisterReceiver(connectionBroadcastReceiver)
                    connectionBroadcastReceiver = null
                }
            }
        })
    }

    override fun onDetachedFromActivityForConfigChanges() {
        // The Activity your plugin was associated with has been
        // destroyed due to config changes. It will be right back
        // but your plugin must clean up any references to that
        // Activity and associated resources.
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

    }


    override fun onDetachedFromActivity() {
        // Your plugin is no longer associated with an Activity.
        // You must clean up all resources and references. Your
        // plugin may, or may not ever be associated with an Activity
        // again.
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        // Your plugin is now associated with an Android Activity.
        //
        // If this method is invoked, it is always invoked after
        // onAttachedToFlutterEngine().
        //
        // You can obtain an Activity reference with


        //
        // You can listen for Lifecycle changes with
        // binding.getLifecycle()
        //
        // You can listen for Activity results, new Intents, user
        // leave hints, and state saving callbacks by using the
        // appropriate methods on the binding.
    }

    override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
        logout()
        method_channel!!.setMethodCallHandler(null)
        Utils.printLog(" onDetachedFromEngine: ")
    }

    // stream
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onListen(auth: Any?, eventSink: EventSink) {
        if (mBroadcastReceiver == null) {
            Utils.printLog(" adding listener: ")
            mBroadcastReceiver = get_message(eventSink)
            val filter = IntentFilter()
            filter.addAction(Constants.RECEIVE_MESSAGE)
            filter.addAction(Constants.OUTGOING_MESSAGE)
            filter.addAction(Constants.PRESENCE_MESSAGE)
            activity.registerReceiver(mBroadcastReceiver, filter, RECEIVER_NOT_EXPORTED)
        }
    }

    override fun onCancel(o: Any?) {
        if (mBroadcastReceiver != null) {
            Utils.printLog(" cancelling listener: ")
            activity.unregisterReceiver(mBroadcastReceiver)
            mBroadcastReceiver = null
        }
    }

    // Handles the call invocation from the flutter plugin
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        Utils.printLog(" onMethodCall call: " + call.method)
        when (call.method) {
            Constants.LOGIN -> {
                if (!call.hasArgument(Constants.USER_JID) || !call.hasArgument(Constants.PASSWORD) || !call.hasArgument(
                        Constants.HOST
                    )
                ) {
                    result.error("MISSING", "Missing auth.", null)
                }
                jid_user = call.argument<Any>(Constants.USER_JID).toString()
                password = call.argument<Any>(Constants.PASSWORD).toString()
                host = call.argument<Any>(Constants.HOST).toString()
                if (call.hasArgument(Constants.PORT)) {
                    Constants.PORT_NUMBER = call.argument<Any>(Constants.PORT).toString().toInt()
                }
                if (call.hasArgument(Constants.NAVIGATE_FILE_PATH)) {
                    Utils.logFilePath = call.argument<Any>(Constants.NAVIGATE_FILE_PATH).toString()
                }
                if (call.hasArgument(Constants.AUTO_DELIVERY_RECEIPT)) {
                    autoDeliveryReceipt = call.argument<Boolean>(Constants.AUTO_DELIVERY_RECEIPT) == true
                }
                if (call.hasArgument(Constants.REQUIRE_SSL_CONNECTION)) {
                    requireSSLConnection = call.argument(Constants.REQUIRE_SSL_CONNECTION)!!
                }
                if (call.hasArgument(Constants.AUTOMATIC_RECONNECTION)) {
                    automaticReconnection = call.argument<Boolean>(Constants.AUTOMATIC_RECONNECTION) == true
                }
                if (call.hasArgument(Constants.USER_STREAM_MANAGEMENT)) {
                    useStreamManagement = call.argument<Boolean>(Constants.USER_STREAM_MANAGEMENT) == true
                }

                // Start authentication.
                doLogin()
                result.success(Constants.SUCCESS)
            }

            Constants.LOGOUT -> {
                // Doing logout from xmpp.
                logout()
                result.success(Constants.SUCCESS)
            }

            Constants.SEND_MESSAGE, Constants.SEND_GROUP_MESSAGE -> {
                // Handle sending message.
                if (!call.hasArgument(Constants.TO_JID) || !call.hasArgument(Constants.BODY) || !call.hasArgument(
                        Constants.ID
                    )
                ) {
                    result.error("MISSING", "Missing argument to_jid / body / id chat.", null)
                }
                to_jid = call.argument(Constants.TO_JID)
                body = call.argument(Constants.BODY)
                id = call.argument(Constants.ID)
                time = Constants.ZERO
                if (call.hasArgument(Constants.time)) {
                    time = call.argument(Constants.time)
                }
                sendMessage(body, to_jid, id, call.method, time)
                result.success(Constants.SUCCESS)
            }

            Constants.JOIN_MUC_GROUPS -> {
                if (!call.hasArgument(Constants.ALL_GROUPS_IDS)) {
                    result.error("MISSING", "Missing argument all_groups_ids.", null)
                }
                val allGroupsIds = call.argument<ArrayList<String>>(Constants.ALL_GROUPS_IDS)!!
                val response = FlutterXmppConnection.joinAllGroups(allGroupsIds)
                result.success(response)
            }

            Constants.JOIN_MUC_GROUP -> {
                var isJoined = false
                if (!call.hasArgument(Constants.GROUP_ID)) {
                    result.error("MISSING", "Missing argument group_id.", null)
                }
                val group_id = call.argument<String>(Constants.GROUP_ID)
                if (!group_id!!.isEmpty()) {
                    isJoined = FlutterXmppConnection.joinGroupWithResponse(group_id)
                }
                result.success(isJoined)
            }

            Constants.CREATE_MUC -> {
                val group_name = call.argument<String>(Constants.GROUP_NAME)
                val persistent = call.argument<String>(Constants.PERSISTENT)
                val responses = FlutterXmppConnection.createMUC(group_name, persistent)
                result.success(responses)
            }

            Constants.CUSTOM_MESSAGE -> {
                // Handle sending message.
                if (!call.hasArgument(Constants.TO_JID) || !call.hasArgument(Constants.BODY) || !call.hasArgument(
                        Constants.ID
                    )
                ) {
                    result.error("MISSING", "Missing argument to_jid / body / id chat.", null)
                }
                to_jid = call.argument(Constants.TO_JID)
                body = call.argument(Constants.BODY)
                id = call.argument(Constants.ID)
                customString = call.argument(Constants.CUSTOM_TEXT)
                time = Constants.ZERO
                if (call.hasArgument(Constants.time)) {
                    time = call.argument(Constants.time)
                }
                sendCustomMessage(body, to_jid, id, customString, time)
                result.success(Constants.SUCCESS)
            }

            Constants.CUSTOM_GROUP_MESSAGE -> {
                // Handle sending message.
                if (!call.hasArgument(Constants.TO_JID) || !call.hasArgument(Constants.BODY) || !call.hasArgument(
                        Constants.ID
                    )
                ) {
                    result.error("MISSING", "Missing argument to_jid / body / id chat.", null)
                }
                to_jid = call.argument(Constants.TO_JID)
                body = call.argument(Constants.BODY)
                id = call.argument(Constants.ID)
                customString = call.argument(Constants.CUSTOM_TEXT)
                time = Constants.ZERO
                if (call.hasArgument(Constants.time)) {
                    time = call.argument(Constants.time)
                }
                sendCustomGroupMessage(body, to_jid, id, customString, time)
                result.success(Constants.SUCCESS)
            }

            Constants.SEND_DELIVERY_ACK -> {
                val toJid = call.argument<String>(Constants.TO_JID_1)
                val msgId = call.argument<String>(Constants.MESSAGE_ID)
                val receiptId = call.argument<String>(Constants.RECEIPT_ID)
                FlutterXmppConnection.send_delivery_receipt(toJid, msgId, receiptId)
                result.success(Constants.SUCCESS)
            }

            Constants.ADD_MEMBERS_IN_GROUP -> {
                groupName = call.argument(Constants.GROUP_NAME)
                membersJid = call.argument(Constants.MEMBERS_JID)
                FlutterXmppConnection.manageAddMembersInGroup(
                    GroupRole.MEMBER,
                    groupName,
                    membersJid
                )
                result.success(Constants.SUCCESS)
            }

            Constants.ADD_ADMINS_IN_GROUP -> {
                groupName = call.argument(Constants.GROUP_NAME)
                membersJid = call.argument(Constants.MEMBERS_JID)
                FlutterXmppConnection.manageAddMembersInGroup(
                    GroupRole.ADMIN,
                    groupName,
                    membersJid
                )
                result.success(Constants.SUCCESS)
            }

            Constants.REMOVE_MEMBERS_FROM_GROUP -> {
                groupName = call.argument(Constants.GROUP_NAME)
                membersJid = call.argument(Constants.MEMBERS_JID)
                FlutterXmppConnection.manageRemoveFromGroup(GroupRole.MEMBER, groupName, membersJid)
                result.success(Constants.SUCCESS)
            }

            Constants.REMOVE_ADMINS_FROM_GROUP -> {
                groupName = call.argument(Constants.GROUP_NAME)
                membersJid = call.argument(Constants.MEMBERS_JID)
                FlutterXmppConnection.manageRemoveFromGroup(GroupRole.ADMIN, groupName, membersJid)
                result.success(Constants.SUCCESS)
            }

            Constants.ADD_OWNERS_IN_GROUP -> {
                groupName = call.argument(Constants.GROUP_NAME)
                membersJid = call.argument(Constants.MEMBERS_JID)
                FlutterXmppConnection.manageAddMembersInGroup(
                    GroupRole.OWNER,
                    groupName,
                    membersJid
                )
                result.success(Constants.SUCCESS)
            }

            Constants.REMOVE_OWNERS_FROM_GROUP -> {
                groupName = call.argument(Constants.GROUP_NAME)
                membersJid = call.argument(Constants.MEMBERS_JID)
                FlutterXmppConnection.manageRemoveFromGroup(GroupRole.OWNER, groupName, membersJid)
                result.success(Constants.SUCCESS)
            }

            Constants.GET_OWNERS -> {
                groupName = call.argument(Constants.GROUP_NAME)
                jidList =
                    FlutterXmppConnection.getMembersOrAdminsOrOwners(GroupRole.OWNER, groupName)
                result.success(jidList)
            }

            Constants.GET_ADMINS -> {
                groupName = call.argument(Constants.GROUP_NAME)
                jidList =
                    FlutterXmppConnection.getMembersOrAdminsOrOwners(GroupRole.ADMIN, groupName)
                result.success(jidList)
            }

            Constants.GET_MAM -> {
                val userJid = call.argument<String>(Constants.userJid)
                val requestBefore = call.argument<String>(Constants.requestBefore)
                val requestSince = call.argument<String>(Constants.requestSince)
                val limit = call.argument<String>(Constants.limit)
                Utils.printLog("userJId $userJid Before : $requestBefore since $requestSince limit $limit")
                MAMManager.requestMAM(userJid, requestBefore, requestSince, limit)
                result.success("SUCCESS")
            }

            Constants.CHANGE_TYPING_STATUS -> {
                val typingJid = call.argument<String>(Constants.userJid)
                val typingStatus = call.argument<String>(Constants.typingStatus)
                Utils.printLog("userJId $typingJid Typing Status : $typingStatus")
                FlutterXmppConnection.updateChatState(typingJid, typingStatus)
                result.success("SUCCESS")
            }

            Constants.CHANGE_PRESENCE_TYPE -> {
                val presenceType = call.argument<String>(Constants.PRESENCE_TYPE)
                val presenceMode = call.argument<String>(Constants.PRESENCE_MODE)
                Utils.printLog("presenceType : $presenceType , Presence Mode : $presenceMode")
                FlutterXmppConnection.updatePresence(presenceType, presenceMode)
                result.success("SUCCESS")
            }

            Constants.GET_CONNECTION_STATUS -> {
                val connectionStatus = FlutterXmppConnectionService.getState()
                result.success(connectionStatus.toString())
            }

            Constants.GET_MEMBERS -> {
                groupName = call.argument(Constants.GROUP_NAME)
                jidList =
                    FlutterXmppConnection.getMembersOrAdminsOrOwners(GroupRole.MEMBER, groupName)
                result.success(jidList)
            }

            Constants.CURRENT_STATE -> {
                var state = Constants.STATE_UNKNOWN
                when (FlutterXmppConnectionService.getState()) {
                    ConnectionState.CONNECTED -> state = Constants.STATE_CONNECTED
                    ConnectionState.AUTHENTICATED -> state = Constants.STATE_AUTHENTICATED
                    ConnectionState.CONNECTING -> state = Constants.STATE_CONNECTING
                    ConnectionState.DISCONNECTING -> state = Constants.STATE_DISCONNECTING
                    ConnectionState.DISCONNECTED -> state = Constants.STATE_DISCONNECTED
                    ConnectionState.FAILED -> TODO()
                }
                result.success(state)
            }

            Constants.GET_ONLINE_MEMBER_COUNT -> {
                groupName = call.argument(Constants.GROUP_NAME)
                val occupantsSize = FlutterXmppConnection.getOnlineMemberCount(groupName)
                result.success(occupantsSize)
            }

            Constants.GET_LAST_SEEN -> {
                userJid = call.argument(Constants.USER_JID)
                val userLastActivity = FlutterXmppConnection.getLastSeen(userJid)
                result.success(userLastActivity.toString() + "")
            }

            Constants.GET_MY_ROSTERS -> {
                val getMyRosters = FlutterXmppConnection.getMyRosters()
                result.success(getMyRosters)
            }

            Constants.CREATE_ROSTER -> {
                userJid = call.argument(Constants.USER_JID)
                FlutterXmppConnection.createRosterEntry(userJid)
                result.success(Constants.SUCCESS)
            }

            else -> result.notImplemented()
        }
    }

    // login
    private fun doLogin() {
        // Check if the user is already connected or not ? if not then start login process.

        println("FlutterXmppConnectionService.getState() ${FlutterXmppConnectionService.getState()}")
        if (FlutterXmppConnectionService.getState() == ConnectionState.DISCONNECTED) {

            val i = Intent(
                activity,
                FlutterXmppConnectionService::class.java
            )
            i.putExtra(Constants.JID_USER, jid_user)
            i.putExtra(Constants.PASSWORD, password)
            i.putExtra(Constants.HOST, "jix.im")
            i.putExtra(Constants.PORT, Constants.PORT_NUMBER)
            i.putExtra(Constants.AUTO_DELIVERY_RECEIPT, autoDeliveryReceipt)
            i.putExtra(Constants.REQUIRE_SSL_CONNECTION, requireSSLConnection)
            i.putExtra(Constants.USER_STREAM_MANAGEMENT, useStreamManagement)
            i.putExtra(Constants.AUTOMATIC_RECONNECTION, automaticReconnection)
            activity.startService(i)
        }
    }

    private fun logout() {
        // Check if user is connected to xmpp ? if yes then break connection.
        if (FlutterXmppConnectionService.getState() == ConnectionState.AUTHENTICATED) {
            val i1 = Intent(
                activity,
                FlutterXmppConnectionService::class.java
            )
            activity.stopService(i1)
        }
    }
}
