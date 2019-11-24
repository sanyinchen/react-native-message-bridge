/**
 * Created by sanyinchen on 19-11-24.
 *
 * @author sanyinchen
 * @version v0.1
 * @since 19-11-24
 */

import NativeModules from "../BatchedBridge/NativeModules"

let NativeLog = NativeModules.NativeLog;
NativeLog.log("just a hello word message ");

