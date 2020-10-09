/**
 * Created by sanyinchen on 19-11-24.
 *
 * @author sanyinchen
 * @version v0.1
 * @since 19-11-24
 */

import NativeModules from "../BatchedBridge/NativeModules"
import JSTimers from "../Core/Timers/JSTimers"

// const Timer = new JSTimers();

global.NativeLog = NativeModules.NativeLog;
NativeLog.log("hello world !");