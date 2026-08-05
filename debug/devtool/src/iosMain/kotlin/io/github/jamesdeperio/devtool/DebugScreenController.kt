package io.github.jamesdeperio.devtool

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController

class DebugScreenController : UIViewController(nibName = null, bundle = null) {
    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = UIColor.whiteColor

        // Create the Compose view
        val composeVC =
            ComposeUIViewController {
                DebugScreen(
                    onClose = {
                        // Dismiss this controller
                        this@DebugScreenController.dismissViewControllerAnimated(flag = true, completion = null)
                    },
                )
            }

        // Add Compose view as a child controller
        addChildViewController(composeVC)
        val composeView = composeVC.view
        view.addSubview(composeView)

        // Set the frame to match parent view bounds
        composeView.setFrame(view.bounds)
        composeView.setAutoresizingMask(
            UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight,
        )

        composeVC.didMoveToParentViewController(this)
    }
}
