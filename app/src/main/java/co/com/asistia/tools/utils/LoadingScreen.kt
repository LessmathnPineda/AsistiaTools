package co.com.asistia.tools.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Window
import android.widget.ProgressBar
import android.widget.TextView
import co.com.asistia.tools.R


object LoadingScreen {
    var dialog: Dialog? = null //obj
    fun displayLoadingWithText(context: Context?, text: String?, cancelable: Boolean) { // function -- context(parent (reference))

        dialog = Dialog(context!!, android.R.style.Theme_Translucent_NoTitleBar);
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog!!.setContentView(R.layout.layout_loading_screen);
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        //dialog!!.setBackgroundDrawableResource(android.R.color.transparent);
        //dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.BLUE));
        dialog!!.setCancelable(cancelable);
        val progressBar = dialog!!.findViewById<ProgressBar>(R.id.progress_loader);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //progressBar.progressDrawable.colorFilter =
            //BlendModeColorFilter(Color.BLUE, BlendMode.SRC_IN)
        } else {
            //progressBar.progressDrawable
            //.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN)
        }

        val textView = dialog!!.findViewById<TextView>(R.id.text);
        textView.text = text;
        try {
            dialog!!.show();
        } catch (e: Exception) {
        }
    }

    fun hideLoading() {
        try {
            if (dialog != null && dialog!!.isShowing()) {
                dialog!!.dismiss();
            }
        } catch (e: Exception) {}
    }
}