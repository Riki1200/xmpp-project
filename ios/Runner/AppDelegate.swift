import UIKit
import Flutter
import Foundation
import Foundation
 



@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    
    
 
    
 

    
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
      
    let controller : FlutterViewController = window?.rootViewController as! FlutterViewController

//      let xmpp_channel = FlutterMethodChannel(name: "flutter_xmpp/method",
//                                                    binaryMessenger: controller.binaryMessenger)
//      xmpp_channel.setMethodCallHandler({
//            (call: FlutterMethodCall, result: @escaping FlutterResult) -> Void in
//          
//          
//          switch call.method {
//            case "login":
//              self.doLogin(call,result: result)
//            default:
//              result(FlutterMethodNotImplemented)
//          }
//          
//          
//    
//          })
//      
//      
//     var stream = FlutterEventChannel(name: "flutter_xmpp/stream",
//                                                    binaryMessenger: controller.binaryMessenger)
//      
//      stream.setStreamHandler(self)
//      
//     var success_stream = FlutterEventChannel(name: "flutter_xmpp/success_event_stream",
//                                                    binaryMessenger: controller.binaryMessenger)
//      success_stream.setStreamHandler(self)
//     
//     var connection_stream = FlutterEventChannel(name: "flutter_xmpp/connection_event_stream",
//                                                    binaryMessenger: controller.binaryMessenger)
//      connection_stream.setStreamHandler(self)
//     var error_stream = FlutterEventChannel(name: "flutter_xmpp/error_event_stream",
//                                                    binaryMessenger: controller.binaryMessenger)
//      
//      error_stream.setStreamHandler(self)
    
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
    
    
//    private func receiveListening(result: FlutterResult) {
//        guard let eventSink = eventSink else {
//             return
//           }
//      
//    }
    
    
//    private func doLogin(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
//        
//        
//        if let args = call.arguments as? Dictionary<String, Any> {
//            let userJid = args["user_jid"] as? String
//            let password = args["password"] as? String
//            let host = args["host"] as? String
//            let port = args["port"] as? String
//            let requireSSLConnection = args["requireSSLConnection"] as? Bool
//            let autoDeliveryReceipt = args["autoDeliveryReceipt"] as? Bool
//            let useStreamManagement = args["useStreamManagement"] as? Bool
//            let automaticReconnection = args["automaticReconnection"] as? Bool
//            
//            print(args)
//            
//            var jid = JID("Juana@jix.im")!
//            
//            print(jid)
//            
//            var xmppConfig = Config(jid: jid, password: "Abcd1234@", useTLS: true)
//            xmppConfig.allowInsecure = true
//            xmppConfig.host = "jix.im"
//            xmppConfig.streamObserver = DefaultStreamObserver()
//            
//            print(xmppConfig)
//
//            let client = XMPP(config: xmppConfig)
//            
//            
//         
//
//        
//            client.connect {
//              print("Disconnected !")
//             
//            }
//            
//            
//
//            
//                
//         
//        }else {
//            result(FlutterError.init(code: "bad args", message: nil, details: nil))
//        }
//        
//        
//    }
    
 

}



