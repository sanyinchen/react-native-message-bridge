// Copyright (c) Facebook, Inc. and its affiliates.

// This source code is licensed under the MIT license found in the
// LICENSE file in the root directory of this source tree.

package com.facebook.debug.holder;

import android.util.Log;

import com.facebook.debug.debugoverlay.model.DebugOverlayTag;

/**
 * No-op implementation of {@link Printer}.
 */
public class NoopPrinter implements Printer {

    public static final NoopPrinter INSTANCE = new NoopPrinter();

    private NoopPrinter() {
    }

    @Override
    public void logMessage(DebugOverlayTag tag, String message, Object... args) {
        Log.d("src_test", tag.name + message);
    }

    @Override
    public void logMessage(DebugOverlayTag tag, String message) {
        Log.d("src_test", tag.name + message);
    }

    @Override
    public boolean shouldDisplayLogMessage(final DebugOverlayTag tag) {
        return true;
    }
}
