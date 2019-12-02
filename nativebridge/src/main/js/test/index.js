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
//NativeLog.log("Count down has finished !");
let allLeftTime = 5000;
// let timer = setInterval(() => {
//     allLeftTime -= 1000;
//     if (allLeftTime <= 0) {
//         clearInterval(timer);
//         console.log("Count down has finished !")
//         NativeLog.log("Count down has finished !");
//     } else {
//         NativeLog.log("Count down time left : " + allLeftTime + "ms");
//     }
//
// }, 1000);
JSTimers.setTimeout(function () {

    NativeLog.log("hello !");

}, 1000)

