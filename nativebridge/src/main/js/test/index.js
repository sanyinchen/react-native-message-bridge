/**
 * Created by sanyinchen on 19-11-24.
 *
 * @author sanyinchen
 * @version v0.1
 * @since 19-11-24
 */

import NativeModules from "../BatchedBridge/NativeModules"

global.NativeLog = NativeModules.NativeLog;
NativeLog.log("hello world ! from js test");

NativeModules.HelloCxxModule.foo((r) => {
    NativeModules.NativeLog.log(r);
});

