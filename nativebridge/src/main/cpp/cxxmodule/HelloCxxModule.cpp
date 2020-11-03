#include "HelloCxxModule.h"

#include "cxxreact/Instance.h"

HelloCxxModule::HelloCxxModule() {}

std::string HelloCxxModule::getName() {
    return "HelloCxxModule";
}


auto HelloCxxModule::getMethods() -> std::vector<Method> {
    return {
            Method("foo", [](folly::dynamic args, Callback cb) {
                cb({"foo msg from cxx callback"});
                LOG(INFO) << "src_test get foo callback";
            })

    };
}

extern "C" HelloCxxModule *createHelloCxxModule() {
    return new HelloCxxModule();
}
